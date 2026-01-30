package com.be9expensphie.expensphie_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.be9expensphie.expensphie_backend.entity.ExpenseEntity;
import com.be9expensphie.expensphie_backend.entity.Household;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity,Long>{
	//get all expense in household
	List<ExpenseEntity> findByHousehold(Household household);
	
}
