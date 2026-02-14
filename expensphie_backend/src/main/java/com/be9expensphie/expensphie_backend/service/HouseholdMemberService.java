package com.be9expensphie.expensphie_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.be9expensphie.expensphie_backend.dto.MemberDTO;
import com.be9expensphie.expensphie_backend.entity.Household;
import com.be9expensphie.expensphie_backend.entity.HouseholdMember;
import com.be9expensphie.expensphie_backend.repository.HouseholdMemberRepository;
import com.be9expensphie.expensphie_backend.repository.HouseholdRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HouseholdMemberService {
	private final HouseholdMemberRepository memberRepo;
	private final HouseholdRepository householdRepo;
	
	public List<MemberDTO> getMembers(Long householdId){
		Household household=householdRepo.findById(householdId)
				.orElseThrow(()->new RuntimeException("Household not found"));
		
		List<HouseholdMember> members=memberRepo.findByHouseholdId(householdId);
		return members.stream()
				.map(m->new MemberDTO(
						m.getUser().getId(),
						m.getUser().getFullName(),
						m.getRole()	
						))
				.toList();	
	}

}
