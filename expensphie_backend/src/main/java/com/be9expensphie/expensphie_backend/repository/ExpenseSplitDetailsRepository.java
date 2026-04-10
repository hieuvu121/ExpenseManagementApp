package com.be9expensphie.expensphie_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.be9expensphie.expensphie_backend.entity.ExpenseEntity;
import com.be9expensphie.expensphie_backend.entity.ExpenseSplitDetailsEntity;
import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseSplitDetailsRepository extends JpaRepository<ExpenseSplitDetailsEntity, Long> {

    Optional<ExpenseSplitDetailsEntity> findByExpenseAndMember(ExpenseEntity expense, HouseholdMember member);

    @Query("select split from ExpenseSplitDetailsEntity split " +
            "left join fetch split.member " +
            "where split.expense= :expense ")
    List<ExpenseSplitDetailsEntity> findByExpenseWithMember(@Param("expense") ExpenseEntity expense);
}
