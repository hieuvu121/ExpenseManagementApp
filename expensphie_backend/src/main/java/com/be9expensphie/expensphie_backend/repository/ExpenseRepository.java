package com.be9expensphie.expensphie_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.be9expensphie.expensphie_backend.entity.ExpenseEntity;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

}
