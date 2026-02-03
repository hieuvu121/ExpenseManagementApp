package com.be9expensphie.expensphie_backend.security;

import org.springframework.stereotype.Component;

import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import com.be9expensphie.expensphie_backend.entity.UserEntity;
import com.be9expensphie.expensphie_backend.enums.HouseholdRole;
import com.be9expensphie.expensphie_backend.repository.HouseholdMemberRepository;
import com.be9expensphie.expensphie_backend.service.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HouseholdSecurity {
	private final UserService userService;
	private final HouseholdMemberRepository householdMemberRepo;
	
	public boolean isAdmin(Long householdId) {
		UserEntity currentUser=userService.getCurrentUser();
		
		HouseholdMember member=householdMemberRepo.findByUserAndHouseholdId(currentUser, householdId)
				.orElseThrow(()->new RuntimeException("No member found"));
		HouseholdRole roleInHousehold=member.getRole();
		return roleInHousehold==HouseholdRole.ROLE_ADMIN;
	}
}
