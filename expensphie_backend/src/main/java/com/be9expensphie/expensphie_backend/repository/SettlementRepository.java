package com.be9expensphie.expensphie_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import com.be9expensphie.expensphie_backend.entity.SettlementEntity;
import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;

public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {

    @Query("select s from SettlementEntity s where (s.fromMember = :member or s.toMember = :member) "
            + "and s.expenseSplitDetails.expense.status = :status")
    List<SettlementEntity> findByMemberAndExpenseStatus(@Param("member") HouseholdMember member,
            @Param("status") ExpenseStatus status);

}