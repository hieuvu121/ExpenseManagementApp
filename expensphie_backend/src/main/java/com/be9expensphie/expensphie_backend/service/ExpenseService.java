package com.be9expensphie.expensphie_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;
import com.be9expensphie.expensphie_backend.enums.HouseholdRole;
import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseRequestDTO;
import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseResponseDTO;
import com.be9expensphie.expensphie_backend.dto.SplitDTO.SplitRequestDTO;
import com.be9expensphie.expensphie_backend.entity.ExpenseEntity;
import com.be9expensphie.expensphie_backend.entity.ExpenseSplitDetailsEntity;
import com.be9expensphie.expensphie_backend.entity.Household;
import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import com.be9expensphie.expensphie_backend.entity.UserEntity;
import com.be9expensphie.expensphie_backend.repository.ExpenseRepository;
import com.be9expensphie.expensphie_backend.repository.HouseholdMemberRepository;
import com.be9expensphie.expensphie_backend.repository.HouseholdRepository;
import com.be9expensphie.expensphie_backend.security.HouseholdSecurity;

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
	
	@Transactional
	public CreateExpenseResponseDTO createExpense(Long householdId,CreateExpenseRequestDTO createRequest) {
		UserEntity currentUser=userService.getCurrentUser();
		
		//find household to retrieve member 
		Household household = householdRepo.findById(householdId)
			    .orElseThrow(() -> new RuntimeException("Household not found"));
		
		Optional<HouseholdMember> adminOptional =householdMemberRepo.findByHouseholdAndRole(household,HouseholdRole.ROLE_ADMIN );
		HouseholdMember admin =adminOptional.get();
		//find member 
		Optional<HouseholdMember> memberOptional =householdMemberRepo.findByUserAndHousehold(currentUser,household);
		if(memberOptional.isEmpty()) {throw new RuntimeException("User is not in this household");}
		HouseholdMember member=memberOptional.get();
		
		//get role for permissions
		HouseholdRole role=member.getRole();
		ExpenseStatus status=(role==HouseholdRole.ROLE_ADMIN)?ExpenseStatus.APPROVED:ExpenseStatus.PENDING;
		
		//build expense and save
		ExpenseEntity expense=ExpenseEntity.builder()
				.amount(createRequest.getAmount())
				.category(createRequest.getCategory())
				.created_by(member)
				.method(createRequest.getMethod())
				.status(status)
				.household(household)
				.reviewed_by(admin)
				.date(createRequest.getDate())
				.currency(createRequest.getCurrency())
				.build();
		
		//take splits and store;  
		for(SplitRequestDTO split:createRequest.getSplits()) {
			HouseholdMember memberPaid=householdMemberRepo.findById(split.getMemberId())
					.orElseThrow(() -> new RuntimeException("Member not found"));
			ExpenseSplitDetailsEntity splitDetails=ExpenseSplitDetailsEntity.builder()
					.amount(split.getAmount())
					.member(memberPaid)
					.expense(expense)
					.build();
			expense.getSplitDetails().add(splitDetails); 
		}
		ExpenseEntity savedExpense=expenseRepo.save(expense);
		return toDTO(savedExpense);
	}
	
	//convert entity to dto
	private CreateExpenseResponseDTO toDTO(ExpenseEntity expense) {
	    return CreateExpenseResponseDTO.builder()
	            .id(expense.getId())
	            .amount(expense.getAmount())
	            .category(expense.getCategory())
	            .status(expense.getStatus())
	            .date(expense.getDate())
	            .method(expense.getMethod())
	            .currency(expense.getCurrency())
	            .createdBy(expense.getCreated_by().getUser().getFullName())
	            .build();
	}


	public List<CreateExpenseResponseDTO> getExpense(Long householdId) {
		UserEntity currentUser=userService.getCurrentUser();
		//find household and check if user in this household
		Household household=householdRepo.findById(householdId)
				.orElseThrow(()->new RuntimeException("Household not found"));
		
		HouseholdMember member = householdMemberRepo.findByUserAndHousehold(currentUser, household)
	            .orElseThrow(() -> new RuntimeException("User not in household"));
		
		//query entity and return dto response
		List<ExpenseEntity> expenses=expenseRepo.findByHousehold(household);
		return expenses.stream()
				.map(this::toDTO)
				.toList();
	}

	public CreateExpenseResponseDTO getSingleExpense(Long householdId, Long expenseId) {
		UserEntity currentUser=userService.getCurrentUser();
		
		//check household exist
		Household household=householdRepo.findById(householdId)
				.orElseThrow(()->new RuntimeException("No household found"));
		
		//check user belong to group
		HouseholdMember member=householdMemberRepo
				.findByUserAndHousehold(currentUser,household)
				.orElseThrow(()->new RuntimeException("User not in this group"));
		
		ExpenseEntity expense=expenseRepo.findByIdAndHousehold(expenseId, household)
				.orElseThrow(()->new RuntimeException("Expense not found"));
		
		return CreateExpenseResponseDTO.builder()
				.id(expense.getId())
	            .amount(expense.getAmount())
	            .category(expense.getCategory())
	            .date(expense.getDate())
	            .status(expense.getStatus())
	            .method(expense.getMethod())
	            .currency(expense.getCurrency())
				.build()
				;
	}
	
	
	
	//helper
	private void checkAdmin(Long householdId) {
		if(!householdSecurity.isAdmin(householdId)) {
			throw new AccessDeniedException("Only admin can perform this action");
		}
	}
	
	public ExpenseEntity findExpense(Long householdId,Long expenseId) {
		Household household=householdRepo.findById(householdId)
				.orElseThrow(()-> new RuntimeException("No household found"));
		
		return expenseRepo.findByIdAndHousehold(expenseId,household)
				.orElseThrow(()-> new RuntimeException("No expense found"));
	}
	
	//accept logic
	@Transactional
	public CreateExpenseResponseDTO acceptExpense(Long householdId, Long expenseId) {
		checkAdmin(householdId);
		ExpenseEntity expense=findExpense(householdId,expenseId);
		
		if(expense.getStatus()!=ExpenseStatus.PENDING) {
			 throw new RuntimeException("Only pending expense can be approved");
		}
		
		expense.setStatus(ExpenseStatus.APPROVED);
		expenseRepo.save(expense);
		
		return toDTO(expense);
	}
	
	//rollback
	@Transactional
	public CreateExpenseResponseDTO rollback(Long householdId, Long expenseId) {
		checkAdmin(householdId);
		ExpenseEntity expense=findExpense(householdId,expenseId);
		
		if(expense.getStatus()!=ExpenseStatus.APPROVED) {
			throw new RuntimeException("Only Approved expense can be rollback");
		}
		
		expense.setStatus(ExpenseStatus.PENDING);
		expenseRepo.save(expense);
		
		return toDTO(expense);
	}
	
	//reject
	@Transactional
	public void rejectExpense(Long householdId, Long expenseId) {
		checkAdmin(householdId);
		ExpenseEntity expense=findExpense(householdId,expenseId);
		expenseRepo.delete(expense);
	}
}
