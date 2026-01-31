package com.be9expensphie.expensphie_backend.dto.SettlementDTO;

import java.math.BigDecimal;

import com.be9expensphie.expensphie_backend.enums.SettlementStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettlementDTO {
    private Long id;
    private Long fromMemberId;
    private Long toMemberId;
    private Long expense_split_details_id;
    private BigDecimal amount;
    private String date;
    private String currency;
    @Enumerated(EnumType.STRING)
    private SettlementStatus status;
}
