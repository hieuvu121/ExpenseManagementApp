package com.be9expensphie.expensphie_backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.be9expensphie.expensphie_backend.entity.ExpenseEntity;
import com.be9expensphie.expensphie_backend.entity.Household;
import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity,Long>{
	//get all expense in household
	List<ExpenseEntity> findByHousehold(Household household);
	
	//get approved expense
	@Query(
			"select e from ExpenseEntity e "
			+ "where e.household.id= :householdId "
			+ "and e.status = :status "
			)
	List<ExpenseEntity> findApprovedHousehold(
			@Param("householdId") Long householdId,
			@Param("status") ExpenseStatus status
			);
	
	Optional<ExpenseEntity> findByIdAndHousehold(Long id,Household household);
	
	
	//filter query
	@Query(
			"select e from ExpenseEntity e "
			+ "where e.household.id = :householdId "
			+ "and e.status = :status "
			+ "and e.date >= :start "
			+ "and e.date < :end "
			)
	List<ExpenseEntity> findExpenseInRange(
			@Param("householdId") Long householdId,
			@Param("status") ExpenseStatus status,
			@Param("start") LocalDate start,
			@Param("end") LocalDate end
			);
	
}
