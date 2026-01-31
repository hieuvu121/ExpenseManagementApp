package com.be9expensphie.expensphie_backend.dto.SettlementDTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateSettlementRequestDTO {
    @NotNull(message = "Expense ID is required")
    private Long expenseId;

    @NotNull(message = "Expense split details ID is required")
    private Long expenseSplitDetailsId;

    @NotNull(message = "From member ID is required")
    private Long fromMemberId;

    @NotNull(message = "To member ID is required")
    private Long toMemberId;
}
