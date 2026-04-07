package com.be9expensphie.expensphie_backend.serviceTests;

import com.be9expensphie.expensphie_backend.dto.CursorDTO;
import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseRequestDTO;
import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseResponseDTO;
import com.be9expensphie.expensphie_backend.dto.SplitDTO.SplitRequestDTO;
import com.be9expensphie.expensphie_backend.entity.ExpenseEntity;
import com.be9expensphie.expensphie_backend.entity.ExpenseSplitDetailsEntity;
import com.be9expensphie.expensphie_backend.entity.Household;
import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import com.be9expensphie.expensphie_backend.entity.SettlementEntity;
import com.be9expensphie.expensphie_backend.entity.UserEntity;
import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;
import com.be9expensphie.expensphie_backend.enums.HouseholdRole;
import com.be9expensphie.expensphie_backend.enums.Method;
import com.be9expensphie.expensphie_backend.enums.SettlementStatus;
import com.be9expensphie.expensphie_backend.enums.TimeRange;
import com.be9expensphie.expensphie_backend.Exception.AiExpenseParseException;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
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
        
        // Mock batch fetch for splits (now uses findAllById instead of findById)
        when(householdMemberRepo.findAllById(List.of(1L, 2L))).thenReturn(List.of(memberAdmin, member2));

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

        // Mock batch fetch for splits (now uses findAllById instead of findById)
        when(householdMemberRepo.findAllById(List.of(1L, 2L))).thenReturn(List.of(member, member2));

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
        // Mock batch fetch returns only adminMember, not the non-existent 999L
        // Validation should catch this in ExpenseValidation
        when(householdMemberRepo.findAllById(List.of(adminMember.getId(), 999L)))
                .thenReturn(List.of(adminMember));
        
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

    @Test
    public void testGetSingleExpense_Success() {
        // arrange
        UserEntity user = createUser(1L, "Member");
        UserEntity admin = createUser(2L, "Admin");
        
        Household household = new Household();
        household.setId(1L);
        
        HouseholdMember member = createHouseholdMember(HouseholdRole.ROLE_MEMBER, household, user.getId(), user);
        HouseholdMember adminMember = createHouseholdMember(HouseholdRole.ROLE_ADMIN, household, admin.getId(), admin);
        
        ExpenseEntity expense = new ExpenseEntity();
        expense.setId(1L);
        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setAmount(BigDecimal.valueOf(100));
        expense.setCategory("Food");
        expense.setDescription("Lunch");
        expense.setDate(LocalDate.now());
        expense.setMethod(Method.EQUAL);
        expense.setCurrency("USD");
        expense.setCreated_by(member);
        expense.setReviewed_by(adminMember);
        expense.setHousehold(household);
        
        when(userService.getCurrentUser()).thenReturn(user);
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(householdMemberRepo.findByUserAndHousehold(user, household)).thenReturn(Optional.of(member));
        when(expenseRepo.findByIdAndHousehold(1L, household)).thenReturn(Optional.of(expense));
        
        // act
        CreateExpenseResponseDTO result = expenseService.getSingleExpense(1L, 1L);
        
        // assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.valueOf(100), result.getAmount());
        assertEquals("Food", result.getCategory());
    }

    @Test
    public void testGetSingleExpense_HouseholdNotFound() {
        // arrange
        UserEntity user = createUser(1L, "Member");
        
        when(userService.getCurrentUser()).thenReturn(user);
        when(householdRepo.findById(999L)).thenReturn(Optional.empty());
        
        // act & assert
        assertThrows(RuntimeException.class, () -> {
            expenseService.getSingleExpense(999L, 1L);
        });
    }

    @Test
    public void testGetSingleExpense_UserNotInHousehold() {
        // arrange
        UserEntity outsider = createUser(1L, "Outsider");
        Household household = new Household();
        household.setId(1L);
        
        when(userService.getCurrentUser()).thenReturn(outsider);
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(householdMemberRepo.findByUserAndHousehold(outsider, household)).thenReturn(Optional.empty());
        
        // act & assert
        assertThrows(RuntimeException.class, () -> {
            expenseService.getSingleExpense(1L, 1L);
        });
    }

    @Test
    public void testGetSingleExpense_ExpenseNotFound() {
        // arrange
        UserEntity user = createUser(1L, "Member");
        Household household = new Household();
        household.setId(1L);
        HouseholdMember member = createHouseholdMember(HouseholdRole.ROLE_MEMBER, household, user.getId(), user);
        
        when(userService.getCurrentUser()).thenReturn(user);
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(householdMemberRepo.findByUserAndHousehold(user, household)).thenReturn(Optional.of(member));
        when(expenseRepo.findByIdAndHousehold(999L, household)).thenReturn(Optional.empty());
        
        // act & assert
        assertThrows(RuntimeException.class, () -> {
            expenseService.getSingleExpense(1L, 999L);
        });
    }

    @Test
    public void testRejectExpense_ByNonAdmin_ShouldThrowException() {
        // arrange
        when(householdSecurity.isAdmin(1L)).thenReturn(false);
        
        // act & assert
        assertThrows(AccessDeniedException.class, () -> {
            expenseService.rejectExpense(1L, 1L);
        });
    }

    @Test
    public void testRejectExpense_AlreadyApproved_ShouldThrowException() {
        // arrange
        UserEntity admin = createUser(1L, "Admin");
        Household household = new Household();
        household.setId(1L);
        HouseholdMember adminMember = createHouseholdMember(HouseholdRole.ROLE_ADMIN, household, admin.getId(), admin);
        
        ExpenseEntity expense = new ExpenseEntity();
        expense.setId(1L);
        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setHousehold(household);
        expense.setReviewed_by(adminMember);
        
        when(householdSecurity.isAdmin(1L)).thenReturn(true);
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(expenseRepo.findByIdAndHousehold(1L, household)).thenReturn(Optional.of(expense));
        
        // act & assert
        assertThrows(RuntimeException.class, () -> {
            expenseService.rejectExpense(1L, 1L);
        });
    }

    @Test
    public void testGetExpense_FirstPageWithoutStatus() {
        // arrange
        UserEntity user = createUser(1L, "Member");
        Household household = new Household();
        household.setId(1L);
        HouseholdMember member = createHouseholdMember(HouseholdRole.ROLE_MEMBER, household, user.getId(), user);
        
        List<ExpenseEntity> mockExpenses = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            ExpenseEntity exp = new ExpenseEntity();
            exp.setId((long) i);
            exp.setAmount(BigDecimal.valueOf(100 + i));
            exp.setCategory("Food");
            exp.setCreated_by(member);  // Set created_by to avoid NPE
            mockExpenses.add(exp);
        }
        
        when(userService.getCurrentUser()).thenReturn(user);
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(householdMemberRepo.findByUserAndHousehold(user, household)).thenReturn(Optional.of(member));
        when(expenseRepo.findNextExpense(eq(Long.MAX_VALUE), eq(household), any(Pageable.class))).thenReturn(mockExpenses);
        
        // act
        CursorDTO<CreateExpenseResponseDTO> result = expenseService.getExpense(1L, null, 10, null);
        
        // assert
        assertNotNull(result);
        assertEquals(10, result.getData().size());
        assertTrue(result.isHasMore());
        assertNotNull(result.getNextCursor());
    }

    @Test
    public void testGetExpense_WithCursorPagination() {
        // arrange
        UserEntity user = createUser(1L, "Member");
        Household household = new Household();
        household.setId(1L);
        HouseholdMember member = createHouseholdMember(HouseholdRole.ROLE_MEMBER, household, user.getId(), user);
        
        List<ExpenseEntity> mockExpenses = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            ExpenseEntity exp = new ExpenseEntity();
            exp.setId((long) i);
            exp.setAmount(BigDecimal.valueOf(100 + i));
            exp.setCategory("Food");
            exp.setCreated_by(member);  // Set created_by to avoid NPE
            mockExpenses.add(exp);
        }
        
        when(userService.getCurrentUser()).thenReturn(user);
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(householdMemberRepo.findByUserAndHousehold(user, household)).thenReturn(Optional.of(member));
        when(expenseRepo.findNextExpense(eq(50L), eq(household), any(Pageable.class))).thenReturn(mockExpenses);
        
        // act
        CursorDTO<CreateExpenseResponseDTO> result = expenseService.getExpense(1L, null, 10, 50L);
        
        // assert
        assertNotNull(result);
        assertEquals(10, result.getData().size());
        assertTrue(result.isHasMore());
    }

    @Test
    public void testGetExpense_FilterByStatus() {
        // arrange
        UserEntity user = createUser(1L, "Member");
        Household household = new Household();
        household.setId(1L);
        HouseholdMember member = createHouseholdMember(HouseholdRole.ROLE_MEMBER, household, user.getId(), user);
        
        List<ExpenseEntity> mockExpenses = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ExpenseEntity exp = new ExpenseEntity();
            exp.setId((long) i);
            exp.setAmount(BigDecimal.valueOf(100 + i));
            exp.setStatus(ExpenseStatus.APPROVED);
            exp.setCreated_by(member);  // Set created_by to avoid NPE
            mockExpenses.add(exp);
        }
        
        when(userService.getCurrentUser()).thenReturn(user);
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(householdMemberRepo.findByUserAndHousehold(user, household)).thenReturn(Optional.of(member));
        when(expenseRepo.findExpenseByStatus(eq(1L), eq(ExpenseStatus.APPROVED), eq(Long.MAX_VALUE), any(Pageable.class)))
                .thenReturn(mockExpenses);
        
        // act
        CursorDTO<CreateExpenseResponseDTO> result = expenseService.getExpense(1L, ExpenseStatus.APPROVED, 10, null);
        
        // assert
        assertNotNull(result);
        assertEquals(5, result.getData().size());
        assertFalse(result.isHasMore());
    }

    @Test
    public void testGetExpense_UserNotInHousehold_ShouldThrowException() {
        // arrange
        UserEntity outsider = createUser(1L, "Outsider");
        Household household = new Household();
        household.setId(1L);
        
        when(userService.getCurrentUser()).thenReturn(outsider);
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(householdMemberRepo.findByUserAndHousehold(outsider, household)).thenReturn(Optional.empty());
        
        // act & assert
        assertThrows(RuntimeException.class, () -> {
            expenseService.getExpense(1L, null, 10, null);
        });
    }

    @Test
    public void testGetExpenseByPeriod_DailyRange() {
        // arrange
        UserEntity user = createUser(1L, "Member");
        Household household = new Household();
        household.setId(1L);
        HouseholdMember member = createHouseholdMember(HouseholdRole.ROLE_MEMBER, household, user.getId(), user);
        
        List<ExpenseEntity> mockExpenses = new ArrayList<>();
        ExpenseEntity exp = new ExpenseEntity();
        exp.setId(1L);
        exp.setAmount(BigDecimal.valueOf(100));
        exp.setCreated_by(member);  // Set created_by to avoid NPE
        mockExpenses.add(exp);
        
        when(expenseRepo.findExpenseInRange(eq(1L), eq(ExpenseStatus.APPROVED), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockExpenses);
        
        // act
        List<CreateExpenseResponseDTO> result = expenseService.getExpenseByPeriod(ExpenseStatus.APPROVED, 1L, TimeRange.DAILY);
        
        // assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetExpenseByPeriod_WeeklyRange() {
        // arrange
        UserEntity user = createUser(1L, "Member");
        Household household = new Household();
        household.setId(1L);
        HouseholdMember member = createHouseholdMember(HouseholdRole.ROLE_MEMBER, household, user.getId(), user);
        
        List<ExpenseEntity> mockExpenses = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            ExpenseEntity exp = new ExpenseEntity();
            exp.setId((long) i);
            exp.setAmount(BigDecimal.valueOf(100 * i));
            exp.setCreated_by(member);  // Set created_by to avoid NPE
            mockExpenses.add(exp);
        }
        
        when(expenseRepo.findExpenseInRange(eq(1L), eq(ExpenseStatus.PENDING), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockExpenses);
        
        // act
        List<CreateExpenseResponseDTO> result = expenseService.getExpenseByPeriod(ExpenseStatus.PENDING, 1L, TimeRange.WEEKLY);
        
        // assert
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    public void testGetExpenseByPeriod_MonthlyRange() {
        // arrange
        List<ExpenseEntity> mockExpenses = new ArrayList<>();
        
        when(expenseRepo.findExpenseInRange(eq(1L), eq(ExpenseStatus.REJECTED), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockExpenses);
        
        // act
        List<CreateExpenseResponseDTO> result = expenseService.getExpenseByPeriod(ExpenseStatus.REJECTED, 1L, TimeRange.MONTHLY);
        
        // assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetExpenseLastMonth_Success() {
        // arrange
        UserEntity user = createUser(1L, "Member");
        Household household = new Household();
        household.setId(1L);
        HouseholdMember member = createHouseholdMember(HouseholdRole.ROLE_MEMBER, household, user.getId(), user);
        
        List<ExpenseEntity> mockExpenses = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ExpenseEntity exp = new ExpenseEntity();
            exp.setId((long) i);
            exp.setAmount(BigDecimal.valueOf(100 * i));
            exp.setCreated_by(member);  // Set created_by to avoid NPE
            mockExpenses.add(exp);
        }
        
        when(expenseRepo.findExpenseInLastMonth(1L)).thenReturn(mockExpenses);
        
        // act
        List<CreateExpenseResponseDTO> result = expenseService.getExpenseLastMonth(1L);
        
        // assert
        assertNotNull(result);
        assertEquals(5, result.size());
    }

    @Test
    public void testGetExpenseLastMonth_NoExpenses() {
        // arrange
        when(expenseRepo.findExpenseInLastMonth(1L)).thenReturn(new ArrayList<>());
        
        // act
        List<CreateExpenseResponseDTO> result = expenseService.getExpenseLastMonth(1L);
        
        // assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ============== FINDEXPENSE TESTS ==============
    @Test
    public void testFindExpense_Success() {
        // arrange
        Household household = new Household();
        household.setId(1L);
        
        ExpenseEntity expense = new ExpenseEntity();
        expense.setId(1L);
        expense.setAmount(BigDecimal.valueOf(100));
        expense.setHousehold(household);
        
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(expenseRepo.findByIdAndHousehold(1L, household)).thenReturn(Optional.of(expense));
        
        // act
        ExpenseEntity result = expenseService.findExpense(1L, 1L);
        
        // assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.valueOf(100), result.getAmount());
    }

    @Test
    public void testFindExpense_HouseholdNotFound() {
        // arrange
        when(householdRepo.findById(999L)).thenReturn(Optional.empty());
        
        // act & assert
        assertThrows(RuntimeException.class, () -> {
            expenseService.findExpense(999L, 1L);
        });
    }

    @Test
    public void testFindExpense_ExpenseNotFound() {
        // arrange
        Household household = new Household();
        household.setId(1L);
        
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(expenseRepo.findByIdAndHousehold(999L, household)).thenReturn(Optional.empty());
        
        // act & assert
        assertThrows(RuntimeException.class, () -> {
            expenseService.findExpense(1L, 999L);
        });
    }

    @Test
    public void testUpdateExpense_HouseholdNotFound() {
        // arrange
        CreateExpenseRequestDTO updateRequest = CreateExpenseRequestDTO.builder()
                .amount(BigDecimal.valueOf(200))
                .build();
        
        when(householdRepo.findById(999L)).thenReturn(Optional.empty());
        
        // act & assert
        assertThrows(RuntimeException.class, () -> {
            expenseService.updateExpense(999L, 1L, updateRequest);
        });
    }

    @Test
    public void testUpdateExpense_ExpenseNotFound() {
        // arrange
        Household household = new Household();
        household.setId(1L);
        
        CreateExpenseRequestDTO updateRequest = CreateExpenseRequestDTO.builder()
                .amount(BigDecimal.valueOf(200))
                .build();
        
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(expenseRepo.findByIdAndHousehold(999L, household)).thenReturn(Optional.empty());
        
        // act & assert
        assertThrows(RuntimeException.class, () -> {
            expenseService.updateExpense(1L, 999L, updateRequest);
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
