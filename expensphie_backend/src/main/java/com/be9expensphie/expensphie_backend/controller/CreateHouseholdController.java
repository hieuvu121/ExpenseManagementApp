package com.be9expensphie.expensphie_backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.be9expensphie.expensphie_backend.dto.CreateHouseholdDTO.CreateRequest;
import com.be9expensphie.expensphie_backend.dto.CreateHouseholdDTO.CreateResponse;
import com.be9expensphie.expensphie_backend.entity.UserEntity;
import com.be9expensphie.expensphie_backend.repository.UserRepository;
import com.be9expensphie.expensphie_backend.service.HouseholdService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/households")
@RequiredArgsConstructor
public class CreateHouseholdController {
	
	private final HouseholdService householdService;
	private final UserRepository userRepository;
	@PostMapping
	public ResponseEntity<CreateResponse> createHousehold(
	        @Valid @RequestBody CreateRequest request,
	        Authentication authentication
	) {
	    String email = authentication.getName(); 
	    UserEntity user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("User not found"));
	    return ResponseEntity.ok(householdService.create(request, user));
	}


}
