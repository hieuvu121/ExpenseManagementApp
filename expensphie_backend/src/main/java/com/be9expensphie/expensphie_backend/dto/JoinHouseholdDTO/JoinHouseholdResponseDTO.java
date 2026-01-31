package com.be9expensphie.expensphie_backend.dto.JoinHouseholdDTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class JoinHouseholdResponseDTO {
	//need this for display in UI
    private Long householdId;
    private String householdName;
    private String role;
}
