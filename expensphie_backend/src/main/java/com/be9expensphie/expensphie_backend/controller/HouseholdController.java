package com.be9expensphie.expensphie_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.be9expensphie.expensphie_backend.dto.HouseholdDTO;
import com.be9expensphie.expensphie_backend.dto.CreateHouseholdDTO.CreateRequest;
import com.be9expensphie.expensphie_backend.dto.CreateHouseholdDTO.CreateResponse;
import com.be9expensphie.expensphie_backend.dto.JoinHouseholdDTO.JoinHouseholdRequestDTO;
import com.be9expensphie.expensphie_backend.dto.JoinHouseholdDTO.JoinHouseholdResponseDTO;
import com.be9expensphie.expensphie_backend.entity.UserEntity;

import com.be9expensphie.expensphie_backend.service.HouseholdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/households")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class HouseholdController {

    private final HouseholdService householdService;

    //create household
    @PostMapping("/create")
    public ResponseEntity<CreateResponse> createHousehold(
            @Valid @RequestBody CreateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                householdService.create(request, authentication)
        );
    }

    //join household
    @PostMapping("/join")
    public ResponseEntity<JoinHouseholdResponseDTO> joinHousehold(
            @Valid @RequestBody JoinHouseholdRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                householdService.joinHousehold(request, authentication)
        );
    }

    //get household
    @GetMapping("/my")
    public ResponseEntity<List<HouseholdDTO>> getHousehold(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                householdService.getHousehold(authentication)
        );
    }
}

