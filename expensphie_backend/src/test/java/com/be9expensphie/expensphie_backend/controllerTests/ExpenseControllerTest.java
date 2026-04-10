package com.be9expensphie.expensphie_backend.controllerTests;

import com.be9expensphie.expensphie_backend.controller.ExpenseController;
import com.be9expensphie.expensphie_backend.dto.CursorDTO;
import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseRequestDTO;
import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseResponseDTO;
import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;
import com.be9expensphie.expensphie_backend.security.JwtRequestFilter;
import com.be9expensphie.expensphie_backend.service.ExpenseService;
import com.be9expensphie.expensphie_backend.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers= ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class ExpenseControllerTest {
    @Autowired
    private MockMvc mockMvc; //fake call http method

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    private CreateExpenseRequestDTO validRequest;
    private CreateExpenseResponseDTO validResponse;
    private Long householdId;
    private Long expenseId;

    @BeforeEach
    void setUp() {
        householdId = 1L;
        expenseId = 100L;

        validRequest = CreateExpenseRequestDTO.builder()
                .amount(new BigDecimal("150.50"))
                .description("Grocery shopping")
                .category("Food")
                .date(LocalDate.from(LocalDateTime.now()))
                .build();

        validResponse = CreateExpenseResponseDTO.builder()
                .id(expenseId)
                .amount(new BigDecimal("150.50"))
                .description("Grocery shopping")
                .category("Food")
                .status(ExpenseStatus.PENDING)
                .date(LocalDate.from(LocalDateTime.now()))
                .build();
    }

    @Test
    public void testCreateExpense_Success() throws Exception {
        //not call service logic, when controller run-> return res mock from service
        when(expenseService.createExpense(eq(householdId),any(CreateExpenseRequestDTO.class))).thenReturn(validResponse);

        mockMvc.perform(post("/households/{householdId}/expenses", householdId)
                .contentType(MediaType.APPLICATION_JSON)//body is json
                .content(objectMapper.writeValueAsString(validRequest)))//convert obj validRequest->json
                //check body if match
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.amount").value(150.50))
                .andExpect(jsonPath("$.description").value("Grocery shopping"))
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        //check if call right services, 1 time with correct variable
        verify(expenseService,times(1)).createExpense(eq(householdId),any(CreateExpenseRequestDTO.class));

    }

    @Test
    public void testCreateExpense_ValidationFailurre() throws Exception{
        //arrange
        CreateExpenseRequestDTO request =CreateExpenseRequestDTO.builder()
                .amount(null)
                .description("test")
                .category("Food")
                .date(LocalDate.from(LocalDateTime.now()))
                .build();

        //act
        mockMvc.perform(post("/households/{householdId}/expenses", householdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))//mapper to map builder-> json
                .andExpect(status().isBadRequest());

        //assert
        verify(expenseService,never()).createExpense(any(),any());
    }

    @Test
    public void testGetExpense_WithPagination() throws Exception{
        //arrange
        List<CreateExpenseResponseDTO> expense= Arrays.asList(validResponse);
        CursorDTO<CreateExpenseResponseDTO> cursorResponse=CursorDTO.<CreateExpenseResponseDTO>builder()
                .data(expense)
                .nextCursor(200L)
                .hasMore(true)
                .build();
        //eq-> must equal inside(), any-> only need to match type
        when(expenseService.getExpense(eq(householdId),eq(ExpenseStatus.PENDING),eq(10),eq(50L)))
                .thenReturn(cursorResponse);
        //act
        mockMvc.perform(get("/households/{householdId}/expenses", householdId)
                .param("status","PENDING")
                .param("limit","10")
                .param("cursor","50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.nextCursor").value(200))
                .andExpect(jsonPath("$.hasMore").value(true))
                .andExpect(jsonPath("$.data[0].id").value(expenseId));
        //assert
        verify(expenseService,times(1)).getExpense(householdId,ExpenseStatus.PENDING,10,50L);
        //if run 1 times with the variable correct with in getExpense(...)
    }

    @Test
    public void testGetSingleExpense_Success() throws Exception{
        //arrange
        when(expenseService.getSingleExpense(householdId,expenseId)).thenReturn(validResponse);
        //act
        mockMvc.perform(get("/households/{householdId}/expenses/{expenseId}",householdId,expenseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.description").value("Grocery shopping"));
        //assert
        verify(expenseService,times(1)).getSingleExpense(householdId,expenseId);
    }

    @Test
    public void testGetSingleExpense_NotFound() throws Exception{
        //arrange
        Long testId=990L;
        when(expenseService.getSingleExpense(householdId,testId)).thenThrow(new RuntimeException("Expense not found"));
        //act
        mockMvc.perform(get("/households/{householdId}/expenses/{expenseId}", householdId, testId))
                .andExpect(status().isBadRequest());
        //assert
        verify(expenseService,times(1)).getSingleExpense(householdId,testId);
    }

    @Test
    public void testApproveExpense_Success() throws Exception{
        //arrange
        CreateExpenseResponseDTO approved=CreateExpenseResponseDTO.builder()
                .id(expenseId)
                .amount(new BigDecimal("150.0"))
                .description("Grocery")
                .category("Food")
                .status(ExpenseStatus.APPROVED)
                .build();

        when(expenseService.acceptExpense(householdId,expenseId)).thenReturn(approved);
        //act
        mockMvc.perform(patch("/households/{householdId}/expenses/{expenseId}/approve",householdId,expenseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.status").value("APPROVED"));
        //assert
        verify(expenseService,times(1)).acceptExpense(householdId,expenseId);
    }

    @Test
    public void testUpdateExpense_Success() throws Exception{
        //arrange
        CreateExpenseRequestDTO updateRequest = CreateExpenseRequestDTO.builder()
                .amount(new BigDecimal("200.00"))
                .description("Updated expense")
                .category("Entertainment")
                .date(LocalDate.from(LocalDateTime.now()))
                .build();

        CreateExpenseResponseDTO updatedResponse = CreateExpenseResponseDTO.builder()
                .id(expenseId)
                .amount(new BigDecimal("200.00"))
                .description("Updated expense")
                .category("Entertainment")
                .status(ExpenseStatus.PENDING)
                .date(LocalDate.from(LocalDateTime.now()))
                .build();

        when(expenseService.updateExpense(eq(householdId), eq(expenseId), any(CreateExpenseRequestDTO.class)))
                .thenReturn(updatedResponse);

        //act
        mockMvc.perform(patch("/households/{householdId}/expenses/{expenseId}/update", householdId, expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.description").value("Updated expense"))
                .andExpect(jsonPath("$.category").value("Entertainment"));

        //assert
        verify(expenseService, times(1)).updateExpense(eq(householdId), eq(expenseId), any(CreateExpenseRequestDTO.class));
    }

    @Test
    public void testUpdateExpense_ValidationFailure() throws Exception{
        //arrange
        CreateExpenseRequestDTO invalidRequest = CreateExpenseRequestDTO.builder()
                .amount(null)  // Invalid - amount is required
                .description("Updated expense")
                .category("Entertainment")
                .date(LocalDate.from(LocalDateTime.now()))
                .build();

        //act
        mockMvc.perform(patch("/households/{householdId}/expenses/{expenseId}/update", householdId, expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        //assert
        verify(expenseService, never()).updateExpense(any(), any(), any());
    }

    @Test
    public void testRejectExpense_Success() throws Exception{
        //arrange
        doNothing().when(expenseService).rejectExpense(householdId, expenseId);

        //act
        mockMvc.perform(delete("/households/{householdId}/expenses/{expenseId}/reject", householdId, expenseId))
                .andExpect(status().isNoContent());

        //assert
        verify(expenseService, times(1)).rejectExpense(householdId, expenseId);
    }

    @Test
    public void testRejectExpense_NotFound() throws Exception{
        //arrange
        Long notFoundId = 999L;
        doThrow(new RuntimeException("Expense not found"))
                .when(expenseService).rejectExpense(householdId, notFoundId);

        //act
        mockMvc.perform(delete("/households/{householdId}/expenses/{expenseId}/reject", householdId, notFoundId))
                .andExpect(status().isBadRequest());

        //assert
        verify(expenseService, times(1)).rejectExpense(householdId, notFoundId);
    }

    @Test
    public void testGetRangeExpense_Success() throws Exception{
        //arrange
        CreateExpenseResponseDTO expense1 = CreateExpenseResponseDTO.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .description("Expense 1")
                .category("Food")
                .status(ExpenseStatus.APPROVED)
                .build();

        CreateExpenseResponseDTO expense2 = CreateExpenseResponseDTO.builder()
                .id(2L)
                .amount(new BigDecimal("150.00"))
                .description("Expense 2")
                .category("Transport")
                .status(ExpenseStatus.APPROVED)
                .build();

        List<CreateExpenseResponseDTO> expenses = Arrays.asList(expense1, expense2);

        when(expenseService.getExpenseByPeriod(any(), eq(householdId), any()))
                .thenReturn(expenses);

        //act
        mockMvc.perform(get("/households/{householdId}/expenses/{range}/{status}", householdId, "MONTHLY", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        //assert
        verify(expenseService, times(1)).getExpenseByPeriod(any(), eq(householdId), any());
    }

    @Test
    public void testGetLastMonthExpense_Success() throws Exception{
        //arrange
        CreateExpenseResponseDTO expense1 = CreateExpenseResponseDTO.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .description("Last month expense 1")
                .category("Food")
                .status(ExpenseStatus.APPROVED)
                .build();

        CreateExpenseResponseDTO expense2 = CreateExpenseResponseDTO.builder()
                .id(2L)
                .amount(new BigDecimal("200.00"))
                .description("Last month expense 2")
                .category("Shopping")
                .status(ExpenseStatus.PENDING)
                .build();

        List<CreateExpenseResponseDTO> expenses = Arrays.asList(expense1, expense2);

        when(expenseService.getExpenseLastMonth(householdId)).thenReturn(expenses);

        //act
        mockMvc.perform(get("/households/{householdId}/expenses/last-month", householdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description").value("Last month expense 1"))
                .andExpect(jsonPath("$[1].description").value("Last month expense 2"));

        //assert
        verify(expenseService, times(1)).getExpenseLastMonth(householdId);
    }

    @Test
    public void testGetLastMonthExpense_EmptyList() throws Exception{
        //arrange
        when(expenseService.getExpenseLastMonth(householdId)).thenReturn(List.of());

        //act
        mockMvc.perform(get("/households/{householdId}/expenses/last-month", householdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        //assert
        verify(expenseService, times(1)).getExpenseLastMonth(householdId);
    }

    @Test
    public void testCreateExpenseAi_Success() throws Exception{
        //arrange
        String paragraph = "I spent 50 dollars on groceries today at Walmart";
        
        CreateExpenseResponseDTO aiResponse = CreateExpenseResponseDTO.builder()
                .id(expenseId)
                .amount(new BigDecimal("50.00"))
                .description("groceries at Walmart")
                .category("Food")
                .status(ExpenseStatus.PENDING)
                .date(LocalDate.from(LocalDateTime.now()))
                .build();

        when(expenseService.createExpenseAI(eq(householdId), any(String.class)))
                .thenReturn(aiResponse);

        //act
        mockMvc.perform(post("/households/{householdId}/expenses/ai", householdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(paragraph))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.description").value("groceries at Walmart"))
                .andExpect(jsonPath("$.category").value("Food"));

        //assert
        verify(expenseService, times(1)).createExpenseAI(eq(householdId), any(String.class));
    }

    @Test
    public void testCreateExpenseAi_EmptyParagraph() throws Exception{
        //arrange
        String emptyParagraph = "";

        //act - empty paragraph gets rejected by Spring before reaching the service
        mockMvc.perform(post("/households/{householdId}/expenses/ai", householdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyParagraph))
                .andExpect(status().isBadRequest());

        //assert - service should never be called
        verify(expenseService, never()).createExpenseAI(any(), any());
    }
}
