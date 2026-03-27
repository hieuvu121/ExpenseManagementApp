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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        UserEntity admin =new UserEntity();
        admin.setId(1L);
        admin.setFullName("Admin User");

        Household household=new Household();
        household.setId(1L);

        HouseholdMember memberAdmin=new HouseholdMember();
        memberAdmin.setId(1L);
        memberAdmin.setUser(admin);
        memberAdmin.setRole(HouseholdRole.ROLE_ADMIN);

        HouseholdMember member2=new HouseholdMember();
        member2.setId(2L);
        UserEntity user2 = new UserEntity();
        user2.setId(2L);
        member2.setUser(user2);

        CreateExpenseRequestDTO request = CreateExpenseRequestDTO.builder()
                .amount(BigDecimal.valueOf(100))
                .category("Food")
                .description("Test")
                .date(LocalDate.now())
                .currency("USD")
                .method(Method.EQUAL)
                .splits(List.of(
                        new SplitRequestDTO(1L, BigDecimal.valueOf(50)),
                        new SplitRequestDTO(2L, BigDecimal.valueOf(50))
                ))
                .build();

        //when call service-> return mock obj create above
        when(userService.getCurrentUser()).thenReturn(admin);
        when(householdRepo.findById(1L)).thenReturn(Optional.of(household));
        when(householdMemberRepo.findByHouseholdAndRole(household,HouseholdRole.ROLE_ADMIN)).thenReturn(Optional.of(memberAdmin));
        when(householdMemberRepo.findByUserAndHousehold(admin,household)).thenReturn(Optional.of(memberAdmin));
        
        // Mock household member lookups for splits
        when(householdMemberRepo.findById(1L)).thenReturn(Optional.of(memberAdmin));
        when(householdMemberRepo.findById(2L)).thenReturn(Optional.of(member2));

        ExpenseEntity expense=new ExpenseEntity();
        expense.setId(1L);
        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setAmount(request.getAmount());
        expense.setCreated_by(memberAdmin);
        expense.setReviewed_by(memberAdmin);
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setDate(request.getDate());
        expense.setMethod(request.getMethod());
        expense.setCurrency(request.getCurrency());
        expense.setHousehold(household);
        when(expenseRepo.save(any(ExpenseEntity.class))).thenReturn(expense);

        // act
        CreateExpenseResponseDTO result = expenseService.createExpense(1L, request);

        // assert
        ArgumentCaptor<ExpenseEntity> expenseCaptor = ArgumentCaptor.forClass(ExpenseEntity.class);
        verify(expenseRepo).save(expenseCaptor.capture());
        ExpenseEntity capturedExpense = expenseCaptor.getValue();
        assertThat(capturedExpense.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
        verify(settlementService).createSettlementsForExpense(expense);
    }
}
