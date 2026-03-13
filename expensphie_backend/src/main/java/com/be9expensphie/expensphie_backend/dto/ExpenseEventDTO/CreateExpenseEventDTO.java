package com.be9expensphie.expensphie_backend.dto.ExpenseEventDTO;

import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateExpenseEventDTO {
    private String type;
    private CreateExpenseResponseDTO data;
    private Long householdId;
}
