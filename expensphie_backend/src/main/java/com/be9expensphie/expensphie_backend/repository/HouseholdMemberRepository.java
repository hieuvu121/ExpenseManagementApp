package com.be9expensphie.expensphie_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.be9expensphie.expensphie_backend.entity.Household;
import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import com.be9expensphie.expensphie_backend.entity.UserEntity;
import com.be9expensphie.expensphie_backend.enums.HouseholdRole;

public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember,Long> {

		//get all household to select in frontend
    	@Query("""
    		    SELECT m 
    		    FROM HouseholdMember m 
    		    JOIN FETCH m.user u
    		    WHERE m.household.id = :householdId
    		""")
    		List<HouseholdMember> findByHouseholdId(@Param("householdId") Long householdId);

    	
		//if user have been in this household
		boolean existsByHouseholdAndUser(
				Household household,
				UserEntity user
		);
		
		//check role
		Optional<HouseholdMember> findByUserAndHousehold(
				UserEntity user,
				Household household
				);
		
		//return group list
		List<HouseholdMember> findByUser(UserEntity user);
		
		//find admin of a group
		Optional<HouseholdMember> findByHouseholdAndRole(Household household, HouseholdRole role);
		
		Optional<HouseholdMember> findByUserAndHouseholdId(
				UserEntity user, Long householdId );
		
}
