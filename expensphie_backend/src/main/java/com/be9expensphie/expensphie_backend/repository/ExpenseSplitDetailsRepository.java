package com.be9expensphie.expensphie_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.be9expensphie.expensphie_backend.entity.ExpenseEntity;
import com.be9expensphie.expensphie_backend.entity.ExpenseSplitDetailsEntity;
import com.be9expensphie.expensphie_backend.entity.HouseholdMember;

public interface ExpenseSplitDetailsRepository extends JpaRepository<ExpenseSplitDetailsEntity, Long> {

    Optional<ExpenseSplitDetailsEntity> findByExpenseAndMember(ExpenseEntity expense, HouseholdMember member);

}
