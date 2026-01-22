package com.be9expensphie.expensphie_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.be9expensphie.expensphie_backend.entity.Household;
import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import com.be9expensphie.expensphie_backend.entity.UserEntity;

public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember,Long> {
		boolean existsByHouseholdAndUser(Household household,UserEntity user);
	
}
