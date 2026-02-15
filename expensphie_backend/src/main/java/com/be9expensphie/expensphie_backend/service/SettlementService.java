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
import com.be9expensphie.expensphie_backend.entity.ExpenseEntity;
import com.be9expensphie.expensphie_backend.dto.SettlementDTO.SettlementDTO;
import com.be9expensphie.expensphie_backend.entity.ExpenseSplitDetailsEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

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
                    settlement.setStatus(SettlementStatus.AWAITING_APPROVAL);
                    break;
                case AWAITING_APPROVAL:
                    settlement.setStatus(SettlementStatus.PENDING);
                    break;
                case COMPLETED:
                    throw new IllegalArgumentException("Completed settlements require receiver approval to change");
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
    public List<SettlementDTO> getAwaitingApprovalForReceiver(Long memberId, Long householdId) {
        try {
            UserEntity user = userService.getCurrentUser();
            HouseholdMember householdMember = householdMemberRepository
                    .findByUserAndHousehold(user, householdRepository.findById(householdId)
                            .orElseThrow(() -> new NoSuchElementException("Household not found")))
                    .orElseThrow(() -> new NoSuchElementException("Household member not found"));

            if (!householdMember.getId().equals(memberId)) {
                throw new IllegalArgumentException("Unauthorized access to approval queue");
            }

            List<SettlementEntity> settlements = settlementRepository.findByToMemberAndStatus(
                    householdMember, SettlementStatus.AWAITING_APPROVAL);

            return settlements.stream().map(this::toDTO).collect(Collectors.toList());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Failed to get approvals: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to get approvals: " + e.getMessage());
        }
    }

    @SuppressWarnings("null")
    public SettlementDTO approveSettlement(Long settlementId, Long memberId) {
        try {
            UserEntity user = userService.getCurrentUser();
            HouseholdMember householdMember = householdMemberRepository
                    .findById(memberId)
                    .orElseThrow(() -> new NoSuchElementException("Household member not found"));
            if (!householdMember.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Unauthorized access to approve settlement");
            }

            SettlementEntity settlement = settlementRepository.findById(settlementId)
                    .orElseThrow(() -> new NoSuchElementException("Settlement not found"));
            if (!settlement.getToMember().getId().equals(memberId)) {
                throw new IllegalArgumentException("Only receiver can approve settlement");
            }
            if (settlement.getStatus() != SettlementStatus.AWAITING_APPROVAL) {
                throw new IllegalArgumentException("Settlement is not awaiting approval");
            }

            settlement.setStatus(SettlementStatus.COMPLETED);
            SettlementEntity updated = settlementRepository.save(settlement);
            return toDTO(updated);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Failed to approve settlement: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to approve settlement: " + e.getMessage());
        }
    }

    @SuppressWarnings("null")
    public SettlementDTO rejectSettlement(Long settlementId, Long memberId) {
        try {
            UserEntity user = userService.getCurrentUser();
            HouseholdMember householdMember = householdMemberRepository
                    .findById(memberId)
                    .orElseThrow(() -> new NoSuchElementException("Household member not found"));
            if (!householdMember.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Unauthorized access to reject settlement");
            }

            SettlementEntity settlement = settlementRepository.findById(settlementId)
                    .orElseThrow(() -> new NoSuchElementException("Settlement not found"));
            if (!settlement.getToMember().getId().equals(memberId)) {
                throw new IllegalArgumentException("Only receiver can reject settlement");
            }
            if (settlement.getStatus() != SettlementStatus.AWAITING_APPROVAL) {
                throw new IllegalArgumentException("Settlement is not awaiting approval");
            }

            settlement.setStatus(SettlementStatus.PENDING);
            SettlementEntity updated = settlementRepository.save(settlement);
            return toDTO(updated);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Failed to reject settlement: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to reject settlement: " + e.getMessage());
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
            List<SettlementEntity> currentMonthPendingSettlements = settlementRepository
                    .findCurrentMonthPendingSettlementsForMember(householdMember);
            BigDecimal totalPendingAmount = settlementRepository
                    .findCurrentMonthTotalPendingAmountForMember(householdMember);
            return Map.of(
                    "pendingSettlements",
                    currentMonthPendingSettlements.stream().map(this::toDTO).collect(Collectors.toList()),
                    "totalPendingAmount", totalPendingAmount != null ? totalPendingAmount : BigDecimal.ZERO);
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
            List<SettlementEntity> lastThreeMonthsPendingSettlements = settlementRepository
                    .findLastThreeMonthsPendingSettlementsForMember(householdMember);
            BigDecimal totalPendingAmount = settlementRepository
                    .findLastThreeMonthsTotalPendingAmountForMember(householdMember);
            return Map.of(
                    "pendingSettlements",
                    lastThreeMonthsPendingSettlements.stream().map(this::toDTO).collect(Collectors.toList()),
                    "totalPendingAmount", totalPendingAmount != null ? totalPendingAmount : BigDecimal.ZERO);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Failed to get settlement statistics: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to get settlement statistics: " + e.getMessage());
        }
    }

    public SettlementDTO toDTO(SettlementEntity settlementEntity) {
        String fromMemberName = null;
        String toMemberName = null;
        String expenseCategory = null;

        if (settlementEntity.getFromMember() != null
                && settlementEntity.getFromMember().getUser() != null) {
            fromMemberName = settlementEntity.getFromMember().getUser().getFullName();
        }

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
                .fromMemberName(fromMemberName)
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
    public void createSettlementsForExpense(ExpenseEntity expense) {
        if (expense.getStatus() != ExpenseStatus.APPROVED) {
            throw new IllegalArgumentException("Expense is not approved");
        }

        HouseholdMember receiver = expense.getCreated_by();
        for (ExpenseSplitDetailsEntity splitDetails : expense.getSplitDetails()) {
            if (splitDetails.getMember().getId().equals(receiver.getId())) {
                continue;
            }
            if (settlementRepository.existsByExpenseSplitDetails(splitDetails)) {
                continue;
            }

            SettlementEntity settlement = SettlementEntity.builder()
                    .fromMember(splitDetails.getMember())
                    .toMember(receiver)
                    .expenseSplitDetails(splitDetails)
                    .amount(splitDetails.getAmount())
                    .currency(expense.getCurrency())
                    .status(SettlementStatus.PENDING)
                    .build();

            settlementRepository.save(settlement);
        }
    }
}