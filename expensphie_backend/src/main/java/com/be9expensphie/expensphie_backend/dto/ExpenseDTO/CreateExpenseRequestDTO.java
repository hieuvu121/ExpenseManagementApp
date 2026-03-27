package com.be9expensphie.expensphie_backend.dto.ExpenseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.be9expensphie.expensphie_backend.dto.SplitDTO.SplitRequestDTO;
import com.be9expensphie.expensphie_backend.enums.Method;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateExpenseRequestDTO {
    @NotNull
    public BigDecimal amount;
    public LocalDate date;
    @NotBlank
    public String category;
    public String description;
    public Method method;
    public String currency;
    public List<SplitRequestDTO> splits;


}
