package com.be9expensphie.expensphie_backend.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import com.be9expensphie.expensphie_backend.entity.SettlementEntity;
import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;

public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {

    @Query("select s from SettlementEntity s where s.fromMember = :member and s.expenseSplitDetails.expense.status = :status order by s.date desc")

    List<SettlementEntity> findByMemberAndExpenseStatus(@Param("member") HouseholdMember member,
            @Param("status") ExpenseStatus status);

    @Query("select s from SettlementEntity s where s.fromMember = :member "
            + "and s.status = 'PENDING' "
            + "and FUNCTION('MONTH', s.date) = FUNCTION('MONTH', CURRENT_DATE) "
            + "and FUNCTION('YEAR', s.date) = FUNCTION('YEAR', CURRENT_DATE)")
    List<SettlementEntity> findCurrentMonthPendingSettlementsForMember(@Param("member") HouseholdMember member);

    @Query("select SUM(s.amount) from SettlementEntity s where s.fromMember = :member "
            + "and s.status = 'PENDING' "
            + "and FUNCTION('MONTH', s.date) = FUNCTION('MONTH', CURRENT_DATE) "
            + "and FUNCTION('YEAR', s.date) = FUNCTION('YEAR', CURRENT_DATE)")
    BigDecimal findCurrentMonthTotalPendingAmountForMember(@Param("member") HouseholdMember member);

    
    @Query(value = "SELECT * FROM settlements s WHERE s.from_member_id = :#{#member.id} "
        + "AND s.status = 'PENDING'"
        + "AND s.date >= DATE_SUB(CURRENT_DATE, INTERVAL 3 MONTH)", nativeQuery = true)
    List<SettlementEntity> findLastThreeMonthsPendingSettlementsForMember(@Param("member") HouseholdMember member);


    @Query(value = "SELECT SUM(s.amount) FROM settlements s WHERE s.from_member_id = :#{#member.id} "
        + "AND s.status = 'PENDING'"
        + "AND s.date >= DATE_SUB(CURRENT_DATE, INTERVAL 3 MONTH)", nativeQuery = true)
    BigDecimal findLastThreeMonthsTotalPendingAmountForMember(@Param("member") HouseholdMember member);
}