package com.be9expensphie.expensphie_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.be9expensphie.expensphie_backend.dto.MemberDTO;
import com.be9expensphie.expensphie_backend.service.HouseholdMemberService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class HouseholdMemberController {
	private final HouseholdMemberService memberService;
	
	@GetMapping("/{householdId}/members")
	public ResponseEntity<List<MemberDTO>> getMembers(@PathVariable Long householdId){
		return ResponseEntity.ok(memberService.getMembers(householdId));
	}

}
