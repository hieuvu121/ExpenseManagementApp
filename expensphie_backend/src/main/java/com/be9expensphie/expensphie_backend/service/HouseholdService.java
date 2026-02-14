package com.be9expensphie.expensphie_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.be9expensphie.expensphie_backend.dto.HouseholdDTO;
import com.be9expensphie.expensphie_backend.dto.CreateHouseholdDTO.CreateRequest;
import com.be9expensphie.expensphie_backend.dto.CreateHouseholdDTO.CreateResponse;
import com.be9expensphie.expensphie_backend.dto.JoinHouseholdDTO.JoinHouseholdRequestDTO;
import com.be9expensphie.expensphie_backend.dto.JoinHouseholdDTO.JoinHouseholdResponseDTO;
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
	private final UserService userService;
	//check and get user that currently login
	
	
	//create household
	@Transactional
	public CreateResponse create(CreateRequest request) {
		UserEntity user=userService.getCurrentUser();
		
		//checking existing name
		if(householdRepository.existsByName(request.getName())) {
			throw new IllegalArgumentException("This name is already exist");
		}
		
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
				.memberId(householdMember.getId())
				.build();
	}
	
	//join household
	@Transactional
	public JoinHouseholdResponseDTO joinHousehold(JoinHouseholdRequestDTO request) {
		
		UserEntity user=userService.getCurrentUser();
		//check if household exist 
		Household household = householdRepository.findByCode(request.getCode())
		        .orElseThrow(() -> new RuntimeException("Household not found"));
		
		//check user already exist and retrive member
		Optional<HouseholdMember> existing=
				householdmemberRepository.findByUserAndHousehold(user,household);
		
		//exist-> return response
		HouseholdMember member=existing.orElseGet(() -> {
		    HouseholdMember newMember = HouseholdMember.builder()
		            .household(household)
		            .user(user)
		            .role(HouseholdRole.ROLE_MEMBER)
		            .build();
		    return householdmemberRepository.save(newMember);
		});

		return JoinHouseholdResponseDTO.builder()
		        .householdId(household.getId())
		        .householdName(household.getName())
		        .role(member.getRole().name())
		        .memberId(member.getId())
		        .build();
		}
	
	//get current household list
	@Transactional
	public List<HouseholdDTO> getHousehold(){
		UserEntity user=userService.getCurrentUser();
		return householdmemberRepository.findByUser(user)
				.stream()
				.map(m-> HouseholdDTO.builder()
						.id(m.getHousehold().getId())
						.name(m.getHousehold().getName())
						.role(m.getRole().name())
						.code(m.getHousehold().getCode())
						.memberId(m.getId())
						.build()
						)
				.toList();
	}
}
