package com.be9expensphie.expensphie_backend.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.be9expensphie.expensphie_backend.entity.*;
import com.be9expensphie.expensphie_backend.enums.SettlementStatus;
import com.be9expensphie.expensphie_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;
import com.be9expensphie.expensphie_backend.enums.HouseholdRole;
import com.be9expensphie.expensphie_backend.enums.TimeRange;
import com.be9expensphie.expensphie_backend.Exception.AiExpenseParseException;
import com.be9expensphie.expensphie_backend.dto.MemberDTO;
import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseRequestDTO;
import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseResponseDTO;
import com.be9expensphie.expensphie_backend.dto.SplitDTO.SplitRequestDTO;
import com.be9expensphie.expensphie_backend.security.HouseholdSecurity;
import com.be9expensphie.expensphie_backend.validation.ExpenseValidation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {
	private final ExpenseRepository expenseRepo;
	private final UserService userService;
	private final HouseholdRepository householdRepo;
	private final HouseholdMemberRepository householdMemberRepo;
	private final HouseholdSecurity householdSecurity;
	private final SettlementService settlementService;
	private final HouseholdMemberService householdMemberService;
	private final ExpenseValidation expenseValidation;
	private final AiService aiService;
	private final ExpenseSplitDetailsRepository expenseSplitDetailsRepo;
	private final SettlementRepository settlementRepository;
	@Autowired
	private final ObjectMapper mapper;

	@Transactional
	public CreateExpenseResponseDTO createExpense(Long householdId, CreateExpenseRequestDTO createRequest) {
		UserEntity currentUser = userService.getCurrentUser();

		// find household to retrieve member
		Household household = householdRepo.findById(householdId)
				.orElseThrow(() -> new RuntimeException("Household not found"));

		Optional<HouseholdMember> adminOptional = householdMemberRepo.findByHouseholdAndRole(household,
				HouseholdRole.ROLE_ADMIN);
		HouseholdMember admin = adminOptional.get();
		// find member
		Optional<HouseholdMember> memberOptional = householdMemberRepo.findByUserAndHousehold(currentUser, household);
		if (memberOptional.isEmpty()) {
			throw new RuntimeException("User is not in this household");
		}

		expenseValidation.validateExpense(createRequest, householdId);

		HouseholdMember member = memberOptional.get();

		// get role for permissions
		HouseholdRole role = member.getRole();
		ExpenseStatus status = (role == HouseholdRole.ROLE_ADMIN) ? ExpenseStatus.APPROVED : ExpenseStatus.PENDING;

		// build expense and save
		ExpenseEntity expense = ExpenseEntity.builder()
				.amount(createRequest.getAmount())
				.category(createRequest.getCategory())
				.description(createRequest.getDescription())
				.created_by(member)
				.method(createRequest.getMethod())
				.status(status)
				.household(household)
				.reviewed_by(admin)
				.date(createRequest.getDate())
				.currency(createRequest.getCurrency())
				.build();

		// take splits and store;
		for (SplitRequestDTO split : createRequest.getSplits()) {
			HouseholdMember memberPaid = householdMemberRepo.findById(split.getMemberId())
					.orElseThrow(() -> new RuntimeException("Member not found"));
			ExpenseSplitDetailsEntity splitDetails = ExpenseSplitDetailsEntity.builder()
					.amount(split.getAmount())
					.member(memberPaid)
					.expense(expense)
					.build();
			expense.getSplitDetails().add(splitDetails);
		}
		ExpenseEntity savedExpense = expenseRepo.save(expense);
		if (savedExpense.getStatus() == ExpenseStatus.APPROVED) {
			settlementService.createSettlementsForExpense(savedExpense);
		}
		return toDTO(savedExpense);
	}

	// convert entity to dto
	private CreateExpenseResponseDTO toDTO(ExpenseEntity expense) {
		return CreateExpenseResponseDTO.builder()
				.id(expense.getId())
				.amount(expense.getAmount())
				.category(expense.getCategory())
				.description(expense.getDescription())
				.status(expense.getStatus())
				.date(expense.getDate())
				.method(expense.getMethod())
				.currency(expense.getCurrency())
				.createdBy(expense.getCreated_by().getUser().getFullName())
				.build();
	}

	//get all expense or approved
	public List<CreateExpenseResponseDTO> getExpense(Long householdId,ExpenseStatus status) {
		UserEntity currentUser=userService.getCurrentUser();
		List<ExpenseEntity> expenses;
		//find household and check if user in this household
		Household household=householdRepo.findById(householdId)
				.orElseThrow(()->new RuntimeException("Household not found"));

		HouseholdMember member = householdMemberRepo.findByUserAndHousehold(currentUser, household)
				.orElseThrow(() -> new RuntimeException("User not in household"));

		//query entity and return dto response
		if(status==null) {
			expenses=expenseRepo.findByHousehold(household);
		}else {
			expenses=expenseRepo.findApprovedHousehold(householdId, status);
		}
		return expenses.stream()
				.map(this::toDTO)
				.toList();
	}


	public CreateExpenseResponseDTO getSingleExpense(Long householdId, Long expenseId) {
		UserEntity currentUser = userService.getCurrentUser();

		// check household exist
		Household household = householdRepo.findById(householdId)
				.orElseThrow(() -> new RuntimeException("No household found"));

		// check user belong to group
		HouseholdMember member = householdMemberRepo
				.findByUserAndHousehold(currentUser, household)
				.orElseThrow(() -> new RuntimeException("User not in this group"));

		ExpenseEntity expense = expenseRepo.findByIdAndHousehold(expenseId, household)
				.orElseThrow(() -> new RuntimeException("Expense not found"));

		return CreateExpenseResponseDTO.builder()
				.id(expense.getId())
				.amount(expense.getAmount())
				.category(expense.getCategory())
				.description(expense.getDescription())
				.date(expense.getDate())
				.status(expense.getStatus())
				.method(expense.getMethod())
				.currency(expense.getCurrency())
				.build();
	}

	public CreateExpenseResponseDTO updateExpense(Long householdId, Long expenseId,CreateExpenseRequestDTO request){
		Household household = householdRepo.findById(householdId)
				.orElseThrow(() -> new RuntimeException("No household found"));

		ExpenseEntity expense=expenseRepo.findByIdAndHousehold(expenseId,household)
				.orElseThrow(()-> new RuntimeException("Expense not found"));


		//update expense
		if(request.getAmount()!=null){
			expense.setAmount(request.getAmount());
		}
		if(request.getMethod()!=null){
			expense.setMethod(request.getMethod());
		}
		if(request.getDate()!=null){
			expense.setDate(request.getDate());
		}
		if(request.getCurrency()!=null){
			expense.setCurrency(request.getCurrency());
		}
		if(request.getDescription()!=null){
			expense.setDescription(request.getDescription());
		}
		if(request.getCategory()!=null){
			expense.setCategory(request.getCategory());
		}


		//check with each split if exist->use that, if not-> create
		if(request.getSplits()!=null && !request.getSplits().isEmpty()) {
		for (ExpenseSplitDetailsEntity split : request.getSplits().stream()
				.map(splitRequest -> {
					HouseholdMember member = householdMemberRepo.findById(splitRequest.getMemberId())
							.orElseThrow(() -> new RuntimeException("Member not found"));
					Optional<ExpenseSplitDetailsEntity> optionalSplit=
							expenseSplitDetailsRepo.findByExpenseAndMember(expense,member);
					if(optionalSplit.isEmpty()){
						return ExpenseSplitDetailsEntity.builder()
								.expense(expense)
								.member(member)
								.amount(splitRequest.getAmount())
								.build();
					}else{
						ExpenseSplitDetailsEntity existSplit = optionalSplit.get();
						existSplit.setAmount(splitRequest.getAmount());
						return existSplit;
					}
				}).toList()) {
			if(split.getId()==null) {
				expense.getSplitDetails().add(split);
			}

			//update settlement
			//if settlement new
			if(expense.getStatus()==ExpenseStatus.APPROVED&&!expense.getCreated_by().equals(split.getMember())){
				Optional<SettlementEntity> optionalSettlement=settlementRepository.findByExpenseSplitDetails(split);
				//current settlement
				if(optionalSettlement.isPresent()){
					//take settlement out if not null
					SettlementEntity settlement=optionalSettlement.get();
					if(settlement.getStatus()==SettlementStatus.COMPLETED){
						//if completed-> create new settlement
						SettlementEntity newSettlement=SettlementEntity.builder()
								.fromMember(split.getMember())
								.toMember(expense.getCreated_by())
								.expenseSplitDetails(split)
								.amount(split.getAmount())
								.date(expense.getDate())
								.currency(expense.getCurrency())
								.status(SettlementStatus.PENDING)
								.build();
						settlementRepository.save(newSettlement);
					}else{
						//else, change amount
						settlement.setAmount(split.getAmount());
						settlement.setCurrency(expense.getCurrency());
						settlementRepository.save(settlement);
					}
					//new settlement
				}else{
					SettlementEntity newSettlement=SettlementEntity.builder()
							.fromMember(split.getMember())
							.toMember(expense.getCreated_by())
							.expenseSplitDetails(split)
							.amount(split.getAmount())
							.date(expense.getDate())
							.currency(expense.getCurrency())
							.status(SettlementStatus.PENDING)
							.build();
					settlementRepository.save(newSettlement);
				}

			}
		}
		} // end if splits not null

		ExpenseEntity savedExpense=expenseRepo.save(expense);
		return toDTO(savedExpense);
	}

	// helper
	private void checkAdmin(Long householdId) {
		if (!householdSecurity.isAdmin(householdId)) {
			throw new AccessDeniedException("Only admin can perform this action");
		}
	}

	public ExpenseEntity findExpense(Long householdId, Long expenseId) {
		Household household = householdRepo.findById(householdId)
				.orElseThrow(() -> new RuntimeException("No household found"));

		return expenseRepo.findByIdAndHousehold(expenseId, household)
				.orElseThrow(() -> new RuntimeException("No expense found"));
	}

	// accept logic
	@Transactional
	public CreateExpenseResponseDTO acceptExpense(Long householdId, Long expenseId) {
		checkAdmin(householdId);
		ExpenseEntity expense = findExpense(householdId, expenseId);

		if (expense.getStatus() != ExpenseStatus.PENDING) {
			throw new RuntimeException("Only pending expense can be approved");
		}

		expense.setStatus(ExpenseStatus.APPROVED);
		expenseRepo.save(expense);
		settlementService.createSettlementsForExpense(expense);

		return toDTO(expense);
	}

	// reject
	@Transactional
	public void rejectExpense(Long householdId, Long expenseId) {
		checkAdmin(householdId);
		ExpenseEntity expense = findExpense(householdId, expenseId);
		if(expense.getStatus()!=ExpenseStatus.PENDING) {
			throw new RuntimeException("Only pending expense can be rejected");
		}
		expense.setStatus(ExpenseStatus.REJECTED);
		expenseRepo.save(expense);
	}

	//filter query
	public List<CreateExpenseResponseDTO> getExpenseByPeriod(ExpenseStatus status,Long householdId,TimeRange range){
		LocalDate now=LocalDate.now();
		LocalDate start;
		LocalDate end;

		switch(range) {
			case DAILY:
				start=now.with(DayOfWeek.MONDAY);
				end=start.plusWeeks(1);
				break;
			case WEEKLY:
				start=now.minusWeeks(8).with(DayOfWeek.MONDAY);
				end=now.plusDays(1);
				break;
			case MONTHLY:
				start=now.withDayOfMonth(1);
				end=start.plusMonths(1);
				break;
			default:
				throw new RuntimeException("Invalid range of time");
		}
		List<ExpenseEntity> expenses = expenseRepo.findExpenseInRange(
				householdId,
				status,
				start,
				end);
		return expenses.stream()
				.map(this::toDTO)
				.toList();
	}

	//create expense with ai
	@Transactional
	public CreateExpenseResponseDTO createExpenseAI(Long householdId, String paragraph) {
		//send all member id for splits
		List<MemberDTO> member=householdMemberService.getMembers(householdId);

		String prompt=buildPrompt(paragraph,member);
		String aiResponse=aiService.chat(prompt);

		//check response ai for debug
		System.out.println("AI response"+aiResponse);

		try {
			//use mapper to map response to dto
			CreateExpenseRequestDTO request=
					mapper.readValue(aiResponse, CreateExpenseRequestDTO.class);
			return createExpense(householdId,request);
		}catch(JsonProcessingException e) {
			throw new AiExpenseParseException("Please check make sure to fill all required category!");
		}
	}

	private String buildPrompt(String paragraph,List<MemberDTO> member) {
		String today = LocalDate.now()
				.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

		return
				"""
				today is(format: dd/mm/yyyy)""" + today +
						"""
                        according to this memberList: 
                        """+member+
						""" 
                        extract information from the paragraph below.
                        return ONLY valid JSON in this format
                        Do not include markdown.
                        Do not wrap in ```json.
                        If the paragraph didnt provide information about these attributes: amount,category,method and split(who paid what),
                        You are NOT allowed to assume or add any missing information.
                        If missing any information just LEAVE IT BLANK
                        {
                          "amount": number type,
                          "date": "yyyy-MM-dd"(set today is local date),
                          "category": "string type"(write in enum format:ELECTRICITY/FOOD/...->NOT NULL),
                          "description": "string type"(description for that expense,if not mention, LEAVE BLANK),
                          "method": "EQUAL|AMOUNT" (EQUAL:bills split equally, AMOUNT: bills splits customized->NOT NULL)
                          "currency: "AUD|USD|VND"
                          "splits": [
                            { "memberId": number, "amount": number },
                            ....
                          ]
                        }
                        """+paragraph;
	}


}