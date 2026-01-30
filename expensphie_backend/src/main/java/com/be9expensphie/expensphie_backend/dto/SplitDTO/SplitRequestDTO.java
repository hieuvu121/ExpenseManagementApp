package com.be9expensphie.expensphie_backend.dto.SplitDTO;


import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SplitRequestDTO {
    private Long memberId;
    private BigDecimal amount;
}
