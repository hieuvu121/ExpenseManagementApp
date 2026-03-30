package com.be9expensphie.expensphie_backend.serviceTests;

import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseRequestDTO;
import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseResponseDTO;
import com.be9expensphie.expensphie_backend.dto.SplitDTO.SplitRequestDTO;
import com.be9expensphie.expensphie_backend.entity.ExpenseEntity;
import com.be9expensphie.expensphie_backend.entity.Household;
import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import com.be9expensphie.expensphie_backend.entity.UserEntity;
import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;
import com.be9expensphie.expensphie_backend.enums.HouseholdRole;
import com.be9expensphie.expensphie_backend.enums.Method;
import com.be9expensphie.expensphie_backend.repository.ExpenseRepository;
import com.be9expensphie.expensphie_backend.repository.ExpenseSplitDetailsRepository;
import com.be9expensphie.expensphie_backend.repository.HouseholdMemberRepository;
import com.be9expensphie.expensphie_backend.repository.HouseholdRepository;
import com.be9expensphie.expensphie_backend.repository.SettlementRepository;
import com.be9expensphie.expensphie_backend.security.HouseholdSecurity;
import com.be9expensphie.expensphie_backend.service.AiService;
import com.be9expensphie.expensphie_backend.service.ExpenseService;
import com.be9expensphie.expensphie_backend.service.HouseholdMemberService;
import com.be9expensphie.expensphie_backend.service.SettlementService;
import com.be9expensphie.expensphie_backend.service.UserService;
import com.be9expensphie.expensphie_backend.validation.ExpenseValidation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTests {
    @Mock
    private ExpenseRepository expenseRepo;
    @Mock
    private UserService userService;
    @Mock
    private HouseholdRepository householdRepo;
    @Mock
    private HouseholdMemberRepository householdMemberRepo;
    @Mock
    private HouseholdSecurity householdSecurity;
    @Mock
    private SettlementService settlementService;
    @Mock
    private HouseholdMemberService householdMemberService;
    @Mock
    private ExpenseValidation expenseValidation;
    @Mock
    private AiService aiService;
    @Mock
    private ExpenseSplitDetailsRepository expenseSplitDetailsRepo;
    @Mock
    private SettlementRepository settlementRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ObjectMapper mapper;
    @InjectMocks
    private ExpenseService expenseService;

    @Test
    public void createExpense_ByAdmin_ShouldAutoApprove(){
        //arrange
        UserEntity admin =createUser(1L,"User Admin");
        UserEntity member=createUser(2L,"Member 2");

        Household household=new Household();
        household.setId(1L);

        HouseholdMember memberAdmin=createHouseholdMember(HouseholdRole.ROLE_ADMIN, household,admin.getId(),admin);
        HouseholdMember member2=createHouseholdMember(HouseholdRole.ROLE_MEMBER,household,member.getId(),member);

        CreateExpenseRequestDTO request=createExpenseRequest(memberAdmin.getId(),member2.getId());

        //when call service-> return mock obj create above
        when(userService.getCurrentUser()).thenReturn(admin);
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(householdMemberRepo.findByHouseholdAndRole(household,HouseholdRole.ROLE_ADMIN)).thenReturn(Optional.of(memberAdmin));
        when(householdMemberRepo.findByUserAndHousehold(admin,household)).thenReturn(Optional.of(memberAdmin));
        
        // Mock household member lookups for splits
        when(householdMemberRepo.findById(1L)).thenReturn(Optional.of(memberAdmin));
        when(householdMemberRepo.findById(2L)).thenReturn(Optional.of(member2));

        ExpenseEntity expense=createExpense(request,memberAdmin,memberAdmin,household,ExpenseStatus.APPROVED);

        // act
        CreateExpenseResponseDTO result = expenseService.createExpense(1L, request);

        // assert
        ArgumentCaptor<ExpenseEntity> expenseCaptor = ArgumentCaptor.forClass(ExpenseEntity.class);
        verify(expenseRepo).save(expenseCaptor.capture());
        ExpenseEntity capturedExpense = expenseCaptor.getValue();
        assertThat(capturedExpense.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
        verify(settlementService).createSettlementsForExpense(expense);
    }

    @Test
    public void createExpense_ByMember_ShouldPending(){
        //arrange
        Household household=new Household();
        household.setId(1L);

        UserEntity user=createUser(1L,"Member");
        UserEntity user2=createUser(2L,"Member 2");

        HouseholdMember member=createHouseholdMember(HouseholdRole.ROLE_MEMBER,household,user.getId(),user);
        HouseholdMember member2=createHouseholdMember(HouseholdRole.ROLE_ADMIN,household,user2.getId(),user2);
        CreateExpenseRequestDTO request = createExpenseRequest(member.getId(),member2.getId());

        //return mock obj
        when(userService.getCurrentUser()).thenReturn(user);
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(householdMemberRepo.findByHouseholdAndRole(household,
                HouseholdRole.ROLE_ADMIN)).thenReturn(Optional.of(member2));
        when(householdMemberRepo.findByUserAndHousehold(user, household)).thenReturn(Optional.of(member));

        // Mock household member lookups for splits
        when(householdMemberRepo.findById(1L)).thenReturn(Optional.of(member));
        when(householdMemberRepo.findById(2L)).thenReturn(Optional.of(member2));

        ExpenseEntity expense=createExpense(request,member2,member,household,ExpenseStatus.PENDING);
        //act
        CreateExpenseResponseDTO res= expenseService.createExpense(1L,request);

        //assert
        //create box to cap obj from expenseRepo.save
        ArgumentCaptor<ExpenseEntity> expenseCaptor=ArgumentCaptor.forClass(ExpenseEntity.class);
        verify(expenseRepo).save(expenseCaptor.capture());
        ExpenseEntity capturedExpense=expenseCaptor.getValue();
        assertThat(capturedExpense.getStatus()).isEqualTo(ExpenseStatus.PENDING);
    }

    @Test
    public void accceptExpense_ShouldCreateSettlements(){
        //arrange
        UserEntity user=createUser(1L,"Member");
        UserEntity admin=createUser(2L,"Admin");

        Household household=new Household();
        household.setId(1L);

        HouseholdMember member=createHouseholdMember(HouseholdRole.ROLE_MEMBER,household,user.getId(),user);
        HouseholdMember memberAdmin=createHouseholdMember(HouseholdRole.ROLE_ADMIN,household, admin.getId(),admin);
        CreateExpenseRequestDTO request = createExpenseRequest(member.getId(),memberAdmin.getId());

        ExpenseEntity expense=createExpense(request,memberAdmin,member,household,ExpenseStatus.PENDING);
        when(householdRepo.findById(household.getId())).thenReturn(Optional.of(household));
        when(expenseRepo.findByIdAndHousehold(expense.getId(), household)).thenReturn(Optional.of(expense));
        when(householdSecurity.isAdmin(household.getId())).thenReturn(true);

        //act
        CreateExpenseResponseDTO response=expenseService.acceptExpense(household.getId(),expense.getId());
        //assert
        ArgumentCaptor<ExpenseEntity> captorExpense=ArgumentCaptor.forClass(ExpenseEntity.class);
        verify(expenseRepo).save(captorExpense.capture());
        ExpenseEntity capturedExpense=captorExpense.getValue();
        assertThat(capturedExpense.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
        verify(settlementService).createSettlementsForExpense(expense);
    }

    @Test
    public void testUpdateExpense_ShouldRecalculateSettlements() {
        // arrange
        UserEntity admin = createUser(1L, "Admin User");
        UserEntity member = createUser(2L, "Member User");
        
        Household household = new Household();
        household.setId(1L);
        
        HouseholdMember adminMember = createHouseholdMember(HouseholdRole.ROLE_ADMIN, household, admin.getId(), admin);
        HouseholdMember regularMember = createHouseholdMember(HouseholdRole.ROLE_MEMBER, household, member.getId(), member);
        
        // Create existing expense
        CreateExpenseRequestDTO originalRequest = createExpenseRequest(adminMember.getId(), regularMember.getId());
        ExpenseEntity expense = createExpense(originalRequest, adminMember, adminMember, household, ExpenseStatus.APPROVED);
        
        // Create update request with different amounts
        CreateExpenseRequestDTO updateRequest = CreateExpenseRequestDTO.builder()
                .amount(BigDecimal.valueOf(200))
                .splits(List.of(
                        new SplitRequestDTO(adminMember.getId(), BigDecimal.valueOf(100)),
                        new SplitRequestDTO(regularMember.getId(), BigDecimal.valueOf(100))
                ))
                .build();
        
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(expenseRepo.findByIdAndHousehold(1L, household)).thenReturn(Optional.of(expense));
        when(householdMemberRepo.findById(adminMember.getId())).thenReturn(Optional.of(adminMember));
        when(householdMemberRepo.findById(regularMember.getId())).thenReturn(Optional.of(regularMember));
        
        // act
        CreateExpenseResponseDTO result = expenseService.updateExpense(1L, 1L, updateRequest);
        
        // assert
        ArgumentCaptor<ExpenseEntity> expenseCaptor = ArgumentCaptor.forClass(ExpenseEntity.class);
        verify(expenseRepo).save(expenseCaptor.capture());
        ExpenseEntity capturedExpense = expenseCaptor.getValue();
        assertThat(capturedExpense.getAmount()).isEqualTo(BigDecimal.valueOf(200));
    }

    @Test
    public void testAcceptExpense_ByNonAdmin_ShouldThrowException() {
        // arrange
        UserEntity regularUser = createUser(1L, "Regular User");
        UserEntity admin = createUser(2L, "Admin");
        
        Household household = new Household();
        household.setId(1L);
        
        HouseholdMember member = createHouseholdMember(HouseholdRole.ROLE_MEMBER, household, regularUser.getId(), regularUser);
        HouseholdMember adminMember = createHouseholdMember(HouseholdRole.ROLE_ADMIN, household, admin.getId(), admin);
        
        CreateExpenseRequestDTO request = createExpenseRequest(member.getId(), adminMember.getId());
        
        // Create expense without stubbing save
        ExpenseEntity expense = new ExpenseEntity();
        expense.setId(1L);
        expense.setStatus(ExpenseStatus.PENDING);
        expense.setAmount(request.getAmount());
        expense.setCreated_by(member);
        expense.setReviewed_by(adminMember);
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setDate(request.getDate());
        expense.setMethod(request.getMethod());
        expense.setCurrency(request.getCurrency());
        expense.setHousehold(household);
        
        // Mock that current user is not admin
        when(householdSecurity.isAdmin(household.getId())).thenReturn(false);
        
        // act & assert
        assertThrows(AccessDeniedException.class, () -> {
            expenseService.acceptExpense(household.getId(), expense.getId());
        });
    }

    @Test
    public void testAcceptExpense_AlreadyApproved_ShouldThrowException() {
        // arrange
        UserEntity admin = createUser(1L, "Admin");
        UserEntity member = createUser(2L, "Member");
        
        Household household = new Household();
        household.setId(1L);
        
        HouseholdMember adminMember = createHouseholdMember(HouseholdRole.ROLE_ADMIN, household, admin.getId(), admin);
        HouseholdMember regularMember = createHouseholdMember(HouseholdRole.ROLE_MEMBER, household, member.getId(), member);
        
        CreateExpenseRequestDTO request = createExpenseRequest(adminMember.getId(), regularMember.getId());
        
        // Create expense without stubbing save
        ExpenseEntity expense = new ExpenseEntity();
        expense.setId(1L);
        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setAmount(request.getAmount());
        expense.setCreated_by(regularMember);
        expense.setReviewed_by(adminMember);
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setDate(request.getDate());
        expense.setMethod(request.getMethod());
        expense.setCurrency(request.getCurrency());
        expense.setHousehold(household);
        
        when(householdSecurity.isAdmin(household.getId())).thenReturn(true);
        when(householdRepo.findById(household.getId())).thenReturn(Optional.of(household));
        when(expenseRepo.findByIdAndHousehold(expense.getId(), household)).thenReturn(Optional.of(expense));
        
        // act & assert
        assertThrows(RuntimeException.class, () -> {
            expenseService.acceptExpense(household.getId(), expense.getId());
        });
    }

    @Test
    public void testCreateExpense_NonExistentMemberInSplit_ShouldThrowException() {
        // arrange
        UserEntity admin = createUser(1L, "Admin");
        
        Household household = new Household();
        household.setId(1L);
        
        HouseholdMember adminMember = createHouseholdMember(HouseholdRole.ROLE_ADMIN, household, admin.getId(), admin);
        
        // Create request with non-existent member ID
        CreateExpenseRequestDTO request = CreateExpenseRequestDTO.builder()
                .amount(BigDecimal.valueOf(100))
                .category("Food")
                .description("Test")
                .date(LocalDate.now())
                .currency("USD")
                .method(Method.EQUAL)
                .splits(List.of(
                        new SplitRequestDTO(adminMember.getId(), BigDecimal.valueOf(50)),
                        new SplitRequestDTO(999L, BigDecimal.valueOf(50)) // Non-existent member
                ))
                .build();
        
        when(userService.getCurrentUser()).thenReturn(admin);
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(householdMemberRepo.findByHouseholdAndRole(household, HouseholdRole.ROLE_ADMIN))
                .thenReturn(Optional.of(adminMember));
        when(householdMemberRepo.findByUserAndHousehold(admin, household))
                .thenReturn(Optional.of(adminMember));
        when(householdMemberRepo.findById(adminMember.getId())).thenReturn(Optional.of(adminMember));
        when(householdMemberRepo.findById(999L)).thenReturn(Optional.empty()); // Non-existent member
        
        // act & assert
        assertThrows(RuntimeException.class, () -> {
            expenseService.createExpense(1L, request);
        });
    }

    @Test
    public void testCreateExpense_UserNotInHousehold_ShouldThrowException() {
        // arrange
        UserEntity outsider = createUser(1L, "Outsider");
        UserEntity admin = createUser(2L, "Admin");
        
        Household household = new Household();
        household.setId(1L);
        
        HouseholdMember adminMember = createHouseholdMember(HouseholdRole.ROLE_ADMIN, household, admin.getId(), admin);
        
        CreateExpenseRequestDTO request = createExpenseRequest(adminMember.getId(), adminMember.getId());
        
        when(userService.getCurrentUser()).thenReturn(outsider);
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(householdMemberRepo.findByHouseholdAndRole(household, HouseholdRole.ROLE_ADMIN))
                .thenReturn(Optional.of(adminMember));
        when(householdMemberRepo.findByUserAndHousehold(outsider, household))
                .thenReturn(Optional.empty()); // User not in household
        
        // act & assert
        assertThrows(RuntimeException.class, () -> {
            expenseService.createExpense(1L, request);
        });
    }

    private UserEntity createUser(Long id,String name){
        UserEntity user=new UserEntity();
        user.setId(id);
        user.setFullName(name);
        return user;
    }
    private HouseholdMember createHouseholdMember(HouseholdRole role, Household household,Long id,UserEntity userMember){
        HouseholdMember member=new HouseholdMember();
        member.setId(id);
        member.setHousehold(household);
        member.setRole(role);
        member.setUser(userMember);

        return member;
    }
    private CreateExpenseRequestDTO createExpenseRequest(Long id1,Long id2){
        return CreateExpenseRequestDTO.builder()
                .amount(BigDecimal.valueOf(100))
                .category("Food")
                .description("Test")
                .date(LocalDate.now())
                .currency("USD")
                .method(Method.EQUAL)
                .splits(List.of(
                        new SplitRequestDTO(id1, BigDecimal.valueOf(50)),
                        new SplitRequestDTO(id2, BigDecimal.valueOf(50))
                ))
                .build();
    }
    private ExpenseEntity createExpense(CreateExpenseRequestDTO request, HouseholdMember memberAdmin,HouseholdMember memberCreated,Household household, ExpenseStatus status){
        ExpenseEntity expense=new ExpenseEntity();
        expense.setId(1L);
        expense.setStatus(status);
        expense.setAmount(request.getAmount());
        expense.setCreated_by(memberCreated);
        expense.setReviewed_by(memberAdmin);
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setDate(request.getDate());
        expense.setMethod(request.getMethod());
        expense.setCurrency(request.getCurrency());
        expense.setHousehold(household);
        when(expenseRepo.save(any(ExpenseEntity.class))).thenReturn(expense);
        return expense;
    }
}
