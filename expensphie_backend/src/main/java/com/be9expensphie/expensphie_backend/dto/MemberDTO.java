package com.be9expensphie.expensphie_backend.dto;

import com.be9expensphie.expensphie_backend.enums.HouseholdRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberDTO {
	private Long memberId;
	private String fullName;
	private HouseholdRole role;
	
}
