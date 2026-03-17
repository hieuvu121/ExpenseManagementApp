package com.be9expensphie.expensphie_backend.repository;

import org.springframework.data.domain.Pageable;
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
	@Query("select e from ExpenseEntity e where e.id<:cursor and e.household=:household order by e.id desc ")
	List<ExpenseEntity> findNextExpense(@Param("cursor") Long cursor,
										@Param("household") Household household,
										Pageable pageable);
	
	//get expense based on status
	@Query(
			"select e from ExpenseEntity e "
			+ "where e.household.id= :householdId "
			+ "and e.status = :status "
					+ "and e.id<:cursor order by e.id desc"
			)
	List<ExpenseEntity> findExpenseByStatus(
			@Param("householdId") Long householdId,
			@Param("status") ExpenseStatus status,
			@Param("cursor")Long cursor,
			Pageable pageable
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
	
	@Query(value = "SELECT * FROM expense e WHERE e.household_id = :householdId AND e.status = 'APPROVED' AND e.date >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)", nativeQuery = true)
	List<ExpenseEntity> findExpenseInLastMonth(@Param("householdId") Long householdId);
}
