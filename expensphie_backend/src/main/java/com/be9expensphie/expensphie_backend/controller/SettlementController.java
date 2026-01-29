package com.be9expensphie.expensphie_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.be9expensphie.expensphie_backend.dto.SettlementDTO.CreateSettlementRequestDTO;
import com.be9expensphie.expensphie_backend.dto.SettlementDTO.SettlementDTO;
import com.be9expensphie.expensphie_backend.service.SettlementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/{memberId}/{householdId}")
    public ResponseEntity<List<SettlementDTO>> getSettlementsForCurrentUser(
            @PathVariable Long memberId, @PathVariable Long householdId) {
        return ResponseEntity.ok(
                settlementService.getSettlementsForCurrentUser(memberId, householdId));
    }

    @PostMapping("/create")
    public ResponseEntity<SettlementDTO> createSettlement(
            @Valid @RequestBody CreateSettlementRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                settlementService.createSettlement(
                        request.getExpenseId(),
                        request.getExpenseSplitDetailsId(),
                        request.getFromMemberId(),
                        request.getToMemberId()));
    }

    @PutMapping("/{settlementId}/toggle/{memberId}")
    public ResponseEntity<SettlementDTO> toggleSettlementStatus(
            @PathVariable Long settlementId, @PathVariable Long memberId) {
        SettlementDTO updatedSettlement = settlementService.toggleSettlementStatus(settlementId, memberId);
        return ResponseEntity.ok(updatedSettlement);
    }
}
