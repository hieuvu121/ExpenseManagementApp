package com.be9expensphie.expensphie_backend.controller;

import java.util.Map;
import java.util.NoSuchElementException;

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
    public ResponseEntity<?> getSettlementsForCurrentUser(
            @PathVariable Long memberId, @PathVariable Long householdId) {
        try {
            return ResponseEntity.ok(Map.of(
                    "error", false,
                    "settlements", settlementService.getSettlementsForCurrentUser(memberId, householdId)
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of(
                        "error", true,
                        "message", e.getMessage()
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of(
                        "error", true,
                        "message", e.getMessage()
                    )
            );
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSettlement(
            @Valid @RequestBody CreateSettlementRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "error", false,
                    "settlement", settlementService.createSettlement(
                            request.getExpenseId(),
                            request.getExpenseSplitDetailsId(),
                            request.getFromMemberId(),
                            request.getToMemberId()
                    )
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of(
                        "error", true,
                        "message", e.getMessage()
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of(
                        "error", true,
                        "message", e.getMessage()
                    )
            );
        }
    }

    @PutMapping("/{settlementId}/toggle/{memberId}")
    public ResponseEntity<?> toggleSettlementStatus(
            @PathVariable Long settlementId, @PathVariable Long memberId) {
        try {
            SettlementDTO updatedSettlement = settlementService.toggleSettlementStatus(settlementId, memberId);
            return ResponseEntity.ok(Map.of(
                    "error", false,
                    "settlement", updatedSettlement
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of(
                        "error", true,
                        "message", e.getMessage()
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of(
                        "error", true,
                        "message", e.getMessage()
                    )
            );
        }
    }

    @GetMapping("/pending/{memberId}/{householdId}/current-month")
    public ResponseEntity<Map<String, Object>> getCurrentMonthPendingSettlements(
            @PathVariable Long memberId, @PathVariable Long householdId) {
        try {
            Map<String, Object> result = settlementService.getCurrentMonthSettlementStatisticsForMember(memberId, householdId);
            return ResponseEntity.ok(Map.of(
                    "error", false,
                    "data", result
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of(
                        "error", true,
                        "message", e.getMessage()
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of(
                        "error", true,
                        "message", e.getMessage()
                    )
            );
        }
    }
}
