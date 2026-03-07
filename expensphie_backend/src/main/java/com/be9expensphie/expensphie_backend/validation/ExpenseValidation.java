package com.be9expensphie.expensphie_backend.validation;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.be9expensphie.expensphie_backend.dto.MemberDTO;
import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseRequestDTO;
import com.be9expensphie.expensphie_backend.dto.SplitDTO.SplitRequestDTO;
import com.be9expensphie.expensphie_backend.enums.Method;
import com.be9expensphie.expensphie_backend.service.HouseholdMemberService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ExpenseValidation {
	
	private final HouseholdMemberService householdMemberService;
	
	//validate each attribute
	public void validateExpense(CreateExpenseRequestDTO request,Long householdId) {
		if (request.getAmount()==null||request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
	        throw new RuntimeException("Amount must be greater than 0");
	    }
		
		if(request.getCategory()==null||request.getCategory().isBlank()) {
			throw new RuntimeException("Category must be filled");
		}
		
		if(request.getCurrency()==null||request.getCurrency().isBlank()) {
			throw new RuntimeException("Currency must be filled");
		}
		
		if(request.getMethod()==null) {
			throw new RuntimeException("Method must be filled");
		}
		
		List<SplitRequestDTO> splits=request.getSplits();
		if(splits==null||splits.isEmpty()) {
			throw new RuntimeException("Must have at least 1 splits");
		}
		
		//get member id using hashset
		List<MemberDTO> member=householdMemberService.getMembers(householdId);
		Set<Long>memberId=new HashSet<>();
		//check valid member 
		for(MemberDTO m:member) {
			memberId.add(m.getMemberId());
		}
		
		//validate for each spits
		BigDecimal total=BigDecimal.ZERO;
		for(SplitRequestDTO split:request.getSplits()) {
			if(split.getMemberId()==null||!memberId.contains(split.getMemberId())) {
				throw new RuntimeException("Member not in household or invalid!");
			}
			
			if (split.getAmount()==null||split.getAmount().compareTo(BigDecimal.ZERO)<0) {
	            throw new RuntimeException("Split amount must be >= 0");
	            }
			
			total=total.add(split.getAmount());
		}
		
		//validate total splits
		if (total.compareTo(request.getAmount()) != 0) {
	        throw new RuntimeException("Sum of split amounts must equal total amount");
	    }
		
		if(request.getMethod()==Method.EQUAL) {
			BigDecimal first=splits.get(0).getAmount();
			for(SplitRequestDTO split:splits) {
				if(split.getAmount().compareTo(first)!=0) {
					throw new RuntimeException("Splits of each member should equal");
				}
			}
		}
	}

}
