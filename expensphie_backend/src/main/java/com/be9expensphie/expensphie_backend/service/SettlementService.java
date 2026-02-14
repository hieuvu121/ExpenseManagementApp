package com.be9expensphie.expensphie_backend.service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import com.be9expensphie.expensphie_backend.entity.SettlementEntity;
import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;
import com.be9expensphie.expensphie_backend.enums.SettlementStatus;
import com.be9expensphie.expensphie_backend.entity.UserEntity;
import com.be9expensphie.expensphie_backend.dto.SettlementDTO.SettlementDTO;
import com.be9expensphie.expensphie_backend.entity.ExpenseSplitDetailsEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.be9expensphie.expensphie_backend.repository.ExpenseRepository;
import com.be9expensphie.expensphie_backend.repository.ExpenseSplitDetailsRepository;
import com.be9expensphie.expensphie_backend.repository.HouseholdMemberRepository;
import com.be9expensphie.expensphie_backend.repository.HouseholdRepository;
import com.be9expensphie.expensphie_backend.repository.SettlementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final UserService userService;
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdRepository householdRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitDetailsRepository expenseSplitDetailsRepository;

    @SuppressWarnings("null")
    public List<SettlementDTO> getSettlementsForCurrentUser(Long memberId, Long householdId) {
        try {
            UserEntity user = userService.getCurrentUser();
            HouseholdMember householdMember = householdMemberRepository
                    .findByUserAndHousehold(user, householdRepository.findById(householdId)
                            .orElseThrow(() -> new NoSuchElementException("Household not found")))
                    .orElseThrow(() -> new NoSuchElementException("Household member not found"));

            if (!householdMember.getId().equals(memberId)) {
                throw new IllegalArgumentException("Unauthorized access to settlements");
            }

            List<SettlementEntity> settlements = settlementRepository.findByMemberAndExpenseStatus(householdMember,
                    ExpenseStatus.APPROVED);

            return settlements.stream().map(this::toDTO).collect(Collectors.toList());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Failed to get settlement: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to get settlement: " + e.getMessage());
        }
    }

    // Only done by corresponding fromMember
    @SuppressWarnings("null")
    public SettlementDTO toggleSettlementStatus(Long settlementId, Long memberId) {
        try {
            UserEntity user = userService.getCurrentUser();
            HouseholdMember householdMember = householdMemberRepository
                    .findById(memberId)
                    .orElseThrow(() -> new NoSuchElementException("Household member not found"));
            if (!householdMember.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Unauthorized access to toggle settlement status");
            }
            SettlementEntity settlement = settlementRepository.findById(settlementId)
                    .orElseThrow(() -> new NoSuchElementException("Settlement not found"));
            if (!settlement.getFromMember().getId().equals(memberId)) {
                throw new IllegalArgumentException("Unmatch member for toggling settlement status");
            }

            switch (settlement.getStatus()) {
                case PENDING:
                    settlement.setStatus(SettlementStatus.COMPLETED);
                    break;
                case COMPLETED:
                    settlement.setStatus(SettlementStatus.PENDING);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid settlement status");
            }

            SettlementEntity newSettlement = settlementRepository.save(settlement);
            return toDTO(newSettlement);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Failed to toggle settlement status: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to toggle settlement status: " + e.getMessage());
        }
    }

    @SuppressWarnings("null")
    public Map<String, Object> getCurrentMonthSettlementStatisticsForMember(Long memberId, Long householdId) {
        try {
            UserEntity user = userService.getCurrentUser();
            HouseholdMember householdMember = householdMemberRepository
                    .findByUserAndHousehold(user, householdRepository.findById(householdId)
                            .orElseThrow(() -> new NoSuchElementException("Household not found")))
                    .orElseThrow(() -> new NoSuchElementException("Household member not found"));
            if (!householdMember.getId().equals(memberId)) {
                throw new IllegalArgumentException("Unauthorized access to settlement statistics");
            }
            if (!householdId.equals(householdMember.getHousehold().getId())) {
                throw new IllegalArgumentException("Household member does not belong to the specified household");
            }
            List<SettlementEntity> currentMonthPendingSettlements = settlementRepository.findCurrentMonthPendingSettlementsForMember(householdMember);
            BigDecimal totalPendingAmount = settlementRepository.findCurrentMonthTotalPendingAmountForMember(householdMember);
            return Map.of(
                    "pendingSettlements", currentMonthPendingSettlements.stream().map(this::toDTO).collect(Collectors.toList()),
                    "totalPendingAmount", totalPendingAmount != null ? totalPendingAmount : BigDecimal.ZERO
            );
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Failed to get settlement statistics: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to get settlement statistics: " + e.getMessage());
        }
    }

    @SuppressWarnings("null")
    public Map<String, Object> getLastThreeMonthsSettlementStatisticsForMember(Long memberId, Long householdId) {
        try {
            UserEntity user = userService.getCurrentUser();
            HouseholdMember householdMember = householdMemberRepository
                    .findByUserAndHousehold(user, householdRepository.findById(householdId)
                            .orElseThrow(() -> new NoSuchElementException("Household not found")))
                    .orElseThrow(() -> new NoSuchElementException("Household member not found"));
            if (!householdMember.getId().equals(memberId)) {
                throw new IllegalArgumentException("Unauthorized access to settlement statistics");
            }
            if (!householdId.equals(householdMember.getHousehold().getId())) {
                throw new IllegalArgumentException("Household member does not belong to the specified household");
            }
            List<SettlementEntity> lastThreeMonthsPendingSettlements = settlementRepository.findLastThreeMonthsPendingSettlementsForMember(householdMember);
            BigDecimal totalPendingAmount = settlementRepository.findLastThreeMonthsTotalPendingAmountForMember(householdMember);
            return Map.of(
                    "pendingSettlements", lastThreeMonthsPendingSettlements.stream().map(this::toDTO).collect(Collectors.toList()),
                    "totalPendingAmount", totalPendingAmount != null ? totalPendingAmount : BigDecimal.ZERO
            );
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Failed to get settlement statistics: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to get settlement statistics: " + e.getMessage());
        }
    }

    public SettlementDTO toDTO(SettlementEntity settlementEntity) {
        String toMemberName = null;
        String expenseCategory = null;

        if (settlementEntity.getToMember() != null
                && settlementEntity.getToMember().getUser() != null) {
            toMemberName = settlementEntity.getToMember().getUser().getFullName();
        }

        if (settlementEntity.getExpenseSplitDetails() != null
                && settlementEntity.getExpenseSplitDetails().getExpense() != null) {
            expenseCategory = settlementEntity.getExpenseSplitDetails().getExpense().getCategory();
        }

        return SettlementDTO.builder()
                .id(settlementEntity.getId())
                .amount(settlementEntity.getAmount())
                .fromMemberId(settlementEntity.getFromMember().getId())
                .toMemberId(settlementEntity.getToMember().getId())
                .toMemberName(toMemberName)
                .currency(settlementEntity.getCurrency())
                .expense_split_details_id(settlementEntity.getExpenseSplitDetails().getId())
                .expenseCategory(expenseCategory)
                .date(settlementEntity.getDate() != null ? settlementEntity.getDate().toString() : null)
                .status(settlementEntity.getStatus())
                .build();
    }

    public SettlementEntity toEntity(SettlementDTO settlementDTO, HouseholdMember fromMember,
            HouseholdMember toMember, ExpenseSplitDetailsEntity expenseSplitDetails) {
        SettlementEntity.SettlementEntityBuilder builder = SettlementEntity.builder()
                .id(settlementDTO.getId())
                .amount(settlementDTO.getAmount())
                .currency(settlementDTO.getCurrency())
                .fromMember(fromMember)
                .toMember(toMember)
                .expenseSplitDetails(expenseSplitDetails)
                .status(settlementDTO.getStatus());

        if (settlementDTO.getDate() != null) {
            builder.date(LocalDate.parse(settlementDTO.getDate()));
        }

        return builder.build();
    }

    @SuppressWarnings("null")
    public SettlementDTO createSettlement(Long expenseId, Long expenseSplitDetailsId, Long fromMemberId,
            Long toMemberId) {
        try {
            var expense = expenseRepository.findById(expenseId)
                    .orElseThrow(() -> new NoSuchElementException("Expense not found"));

            var splitDetails = expenseSplitDetailsRepository.findById(expenseSplitDetailsId)
                    .orElseThrow(() -> new NoSuchElementException("Expense split details not found"));

            if (!splitDetails.getExpense().getId().equals(expense.getId())) {
                throw new IllegalArgumentException("Expense split does not belong to provided expense");
            }

            if (expense.getStatus() != ExpenseStatus.APPROVED) {
                throw new IllegalArgumentException("Cannot create settlement for non-approved expense");
            }

            HouseholdMember fromMember = householdMemberRepository.findById(fromMemberId)
                    .orElseThrow(() -> new NoSuchElementException("From member not found"));

            HouseholdMember toMember = householdMemberRepository.findById(toMemberId)
                    .orElseThrow(() -> new NoSuchElementException("To member not found"));

            SettlementEntity settlement = SettlementEntity.builder()
                    .fromMember(fromMember)
                    .toMember(toMember)
                    .expenseSplitDetails(splitDetails)
                    .amount(splitDetails.getAmount())
                    .currency(expense.getCurrency())
                    .build();

            SettlementEntity saved = settlementRepository.save(settlement);
            return toDTO(saved);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Failed to create settlement: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to create settlement: " + e.getMessage());
        }
    }
}