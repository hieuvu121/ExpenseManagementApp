package com.be9expensphie.expensphie_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.be9expensphie.expensphie_backend.entity.Household;
import com.be9expensphie.expensphie_backend.entity.UserEntity;

public interface HouseholdMember extends JpaRepository<HouseholdMember,Long>{

	boolean existByHouseholdAndUser(Household household,UserEntity user);
}
