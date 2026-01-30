package com.be9expensphie.expensphie_backend.dto.ExpenseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;
import com.be9expensphie.expensphie_backend.enums.Method;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateExpenseResponseDTO {
    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String category;
    private ExpenseStatus status;
    private Method method;
}

