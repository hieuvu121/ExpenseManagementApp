package com.be9expensphie.expensphie_backend.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.be9expensphie.expensphie_backend.dto.CreateHouseholdDTO.CreateRequest;
import com.be9expensphie.expensphie_backend.dto.CreateHouseholdDTO.CreateResponse;
import com.be9expensphie.expensphie_backend.entity.Household;
import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import com.be9expensphie.expensphie_backend.entity.UserEntity;
import com.be9expensphie.expensphie_backend.enums.HouseholdRole;
import com.be9expensphie.expensphie_backend.repository.HouseholdMemberRepository;
import com.be9expensphie.expensphie_backend.repository.HouseholdRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HouseholdService {
	private final HouseholdRepository householdRepository;
	private final HouseholdMemberRepository householdmemberRepository;
	
	@Transactional
	public CreateResponse create(CreateRequest request, UserEntity user) {
		Household household=Household.builder()
				.name(request.getName())
				.createdBy(user)
				.code(UUID.randomUUID().toString().substring(0, 8))
				.build();
		householdRepository.save(household);
		
		HouseholdMember householdMember =HouseholdMember.builder()
				.household(household)
				.user(user)
				.role(HouseholdRole.ROLE_ADMIN)
				.build();
		householdmemberRepository.save(householdMember);
		
		return CreateResponse.builder()
				.id(household.getId())
				.name(household.getName())
				.role(householdMember.getRole().name())
				.build();
	}
}
