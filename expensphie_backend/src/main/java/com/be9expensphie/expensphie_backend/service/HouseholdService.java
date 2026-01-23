package com.be9expensphie.expensphie_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
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
import com.be9expensphie.expensphie_backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HouseholdService {
	private final HouseholdRepository householdRepository;
	private final HouseholdMemberRepository householdmemberRepository;
	private final UserRepository userRepository;
	
	//check and get user that currently login
	private UserEntity getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    } 
	
	//create household
	@Transactional
	public CreateResponse create(CreateRequest request, Authentication authentication) {
		UserEntity user=getCurrentUser(authentication);
		
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
				.build();
	}
	
	//join household
	@Transactional
	public JoinHouseholdResponseDTO joinHousehold(JoinHouseholdRequestDTO request, Authentication authentication) {
		
		UserEntity user=getCurrentUser(authentication);
		//check if household exist 
		Household household = householdRepository.findByCode(request.getCode())
		        .orElseThrow(() -> new RuntimeException("Household not found"));
		
		//check user already exist and retrive member
		Optional<HouseholdMember> existing=
				householdmemberRepository.findByUserAndHousehold(user,household);
		
		//exist-> return response
		if(existing.isPresent()) {
			HouseholdMember member=existing.get();
			return JoinHouseholdResponseDTO.builder()
					.householdId(household.getId())
					.householdName(household.getName())
					.role(member.getRole().name())
					.build();
		}else {
			//first time-> create and save member
			HouseholdMember member = HouseholdMember.builder()
			        .household(household)
			        .user(user)
			        .role(HouseholdRole.ROLE_MEMBER)
			        .build();
			householdmemberRepository.save(member);
			
			return JoinHouseholdResponseDTO.builder()
					.householdId(household.getId())
					.householdName(household.getName())
					.role(member.getRole().name())
					.build();

		}
	}
	
	//get current household list
	@Transactional
	public List<HouseholdDTO> getHousehold(Authentication authentication){
		UserEntity user=getCurrentUser(authentication);
		return householdmemberRepository.findByUser(user)
				.stream()
				.map(m-> HouseholdDTO.builder()
						.id(m.getHousehold().getId())
						.name(m.getHousehold().getName())
						.role(m.getRole().name())
						.code(m.getHousehold().getCode())
						.build()
						)
				.toList();
	}
}
