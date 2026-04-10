package com.be9expensphie.expensphie_backend.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import com.be9expensphie.expensphie_backend.entity.SettlementEntity;
import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;
import com.be9expensphie.expensphie_backend.enums.SettlementStatus;
import com.be9expensphie.expensphie_backend.entity.ExpenseSplitDetailsEntity;

public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {
    @Query("select s from SettlementEntity s where s.fromMember = :member and s.expenseSplitDetails.expense.status = :status order by s.date desc")
    List<SettlementEntity> findByMemberAndExpenseStatus(@Param("member") HouseholdMember member,
            @Param("status") ExpenseStatus status);

    @Query("select s from SettlementEntity s where s.fromMember = :member "
            + "and (s.status = 'PENDING' or s.status = 'AWAITING_APPROVAL') "
            + "and s.date >= :start and s.date < :end")
    List<SettlementEntity> findCurrentMonthPendingSettlementsForMember(
            @Param("member") HouseholdMember member,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("select SUM(s.amount) from SettlementEntity s where s.fromMember = :member "
            + "and (s.status = 'PENDING' or s.status = 'AWAITING_APPROVAL') "
            + "and s.date >= :start and s.date < :end")
    BigDecimal findCurrentMonthTotalPendingAmountForMember(
            @Param("member") HouseholdMember member,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query(value = "SELECT * FROM settlements s WHERE s.from_member_id = :#{#member.id} "
            + "AND (s.status = 'PENDING' OR s.status = 'AWAITING_APPROVAL') "
            + "AND s.date >= DATE_SUB(CURRENT_DATE, INTERVAL 3 MONTH)", nativeQuery = true)
    List<SettlementEntity> findLastThreeMonthsPendingSettlementsForMember(@Param("member") HouseholdMember member);

    @Query(value = "SELECT SUM(s.amount) FROM settlements s WHERE s.from_member_id = :#{#member.id} "
            + "AND (s.status = 'PENDING' OR s.status = 'AWAITING_APPROVAL')"
            + "AND s.date >= DATE_SUB(CURRENT_DATE, INTERVAL 3 MONTH)", nativeQuery = true)
    BigDecimal findLastThreeMonthsTotalPendingAmountForMember(@Param("member") HouseholdMember member);

    List<SettlementEntity> findByToMemberAndStatus(HouseholdMember member, SettlementStatus status);

    boolean existsByExpenseSplitDetails(ExpenseSplitDetailsEntity expenseSplitDetails);

    Optional<SettlementEntity> findByExpenseSplitDetails(ExpenseSplitDetailsEntity expenseSplitDetails);

    @Query("select s from SettlementEntity s where s.id<:cursor and s.fromMember = :member and s.expenseSplitDetails.expense.status = :status")
    List<SettlementEntity> findNextSettlement(
            @Param("cursor") Long cursor,
            @Param("member") HouseholdMember householdMember,
            @Param("status") ExpenseStatus expenseStatus,
            Pageable pageable);

    List<SettlementEntity> findByExpenseSplitDetailsIn(List<ExpenseSplitDetailsEntity> splits);
}
