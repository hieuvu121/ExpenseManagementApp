package com.be9expensphie.expensphie_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.be9expensphie.expensphie_backend.entity.Household;

public interface HouseholdRepository extends JpaRepository<Household,Long> {
	//instead of read and return all rows, just need bool
	//use for checking unique code when create
	boolean existsByCode(String code);

}



