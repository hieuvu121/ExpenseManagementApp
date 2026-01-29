package com.be9expensphie.expensphie_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import com.be9expensphie.expensphie_backend.entity.SettlementEntity;
import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;
import com.be9expensphie.expensphie_backend.entity.UserEntity;
import com.be9expensphie.expensphie_backend.dto.SettlementDTO.SettlementDTO;
import com.be9expensphie.expensphie_backend.entity.ExpenseSplitDetailsEntity;
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
        UserEntity user = userService.getCurrentUser();
        HouseholdMember householdMember = householdMemberRepository
                .findByUserAndHousehold(user, householdRepository.findById(householdId)
                        .orElseThrow(() -> new IllegalArgumentException("Household not found")))
                .orElseThrow(() -> new IllegalArgumentException("Household member not found"));

        if (!householdMember.getId().equals(memberId)) {
            throw new IllegalArgumentException("Unauthorized access to settlements");
        }

        List<SettlementEntity> settlements = settlementRepository.findByMemberAndExpenseStatus(householdMember,
                ExpenseStatus.APPROVED);

        return settlements.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public SettlementDTO toDTO(SettlementEntity settlementEntity) {
        return SettlementDTO.builder()
                .id(settlementEntity.getId())
                .amount(settlementEntity.getAmount())
                .fromMemberId(settlementEntity.getFromMember().getId())
                .toMemberId(settlementEntity.getToMember().getId())
                .currency(settlementEntity.getCurrency())
                .expense_split_details_id(settlementEntity.getExpenseSplitDetails().getId())
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
        var expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        var splitDetails = expenseSplitDetailsRepository.findById(expenseSplitDetailsId)
                .orElseThrow(() -> new IllegalArgumentException("Expense split details not found"));

        if (!splitDetails.getExpense().getId().equals(expense.getId())) {
            throw new IllegalArgumentException("Expense split does not belong to provided expense");
        }

        if (expense.getStatus() != ExpenseStatus.APPROVED) {
            throw new IllegalArgumentException("Cannot create settlement for non-approved expense");
        }

        HouseholdMember fromMember = householdMemberRepository.findById(fromMemberId)
                .orElseThrow(() -> new IllegalArgumentException("From member not found"));

        HouseholdMember toMember = householdMemberRepository.findById(toMemberId)
                .orElseThrow(() -> new IllegalArgumentException("To member not found"));

        SettlementEntity settlement = SettlementEntity.builder()
                .fromMember(fromMember)
                .toMember(toMember)
                .expenseSplitDetails(splitDetails)
                .amount(splitDetails.getAmount())
                .currency(expense.getCurrency())
                .build();

        SettlementEntity saved = settlementRepository.save(settlement);
        return toDTO(saved);
    }
}
