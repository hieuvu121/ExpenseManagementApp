package com.be9expensphie.expensphie_backend.dto.ExpenseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.be9expensphie.expensphie_backend.dto.SplitDTO.SplitRequestDTO;
import com.be9expensphie.expensphie_backend.enums.Method;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateExpenseRequestDTO {
    @NotNull
    private BigDecimal amount;
    private LocalDate date;
    @NotBlank
    private String category;
    private Method method;
    private String currency;
    private List<SplitRequestDTO> splits;
}
