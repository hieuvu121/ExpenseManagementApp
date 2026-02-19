package com.be9expensphie.expensphie_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseRequestDTO;
import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseResponseDTO;
import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;
import com.be9expensphie.expensphie_backend.enums.TimeRange;
import com.be9expensphie.expensphie_backend.service.ExpenseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("households/{householdId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {
	private final ExpenseService expenseService;
	
	//all mem can create 
	@PostMapping
	public ResponseEntity<CreateExpenseResponseDTO> createExpense(
			@PathVariable Long householdId,
			@Valid @RequestBody CreateExpenseRequestDTO request
			){
		CreateExpenseResponseDTO response=expenseService.createExpense(householdId,request);
		return ResponseEntity.ok(response);
	}
	
	//get all expense 
	@GetMapping()
	public ResponseEntity<List<CreateExpenseResponseDTO>> getExpenses(
			@PathVariable Long householdId,
			@PathVariable(required=false) ExpenseStatus status
			){
		List<CreateExpenseResponseDTO> expenses=expenseService.getExpense(householdId,status);
		return ResponseEntity.ok(expenses);
	}
	
	//get single expense
	@GetMapping("/{expenseId}")
	public ResponseEntity<CreateExpenseResponseDTO> getSingleExpense(
			@PathVariable Long householdId,
			@PathVariable Long expenseId
			){
		CreateExpenseResponseDTO response=expenseService.getSingleExpense(householdId,expenseId);
		return ResponseEntity.ok(response);
	}
	
	//approve expense
	@PatchMapping("/{expenseId}/approve")
	@PreAuthorize("@householdSecurity.isAdmin(#householdId)")
	public ResponseEntity<?> approveExpense(
			@PathVariable Long householdId,
			@PathVariable Long expenseId
			){
		return ResponseEntity.ok(expenseService.acceptExpense(householdId, expenseId));
	}
	
	//rollback expense
	@PatchMapping("/{expenseId}/rollback")
	@PreAuthorize("@householdSecurity.isAdmin(#householdId)")
	public ResponseEntity<?> rollbackExpense(
			@PathVariable Long householdId,
			@PathVariable Long expenseId
			){
		return ResponseEntity.ok(expenseService.rollback(householdId,expenseId));
	}
	
	//delete
	@DeleteMapping("/{expenseId}/reject")
    @PreAuthorize("@householdSecurity.isAdmin(#householdId)")
    public ResponseEntity<?> rejectExpense(
            @PathVariable Long householdId,
            @PathVariable Long expenseId
            ){
        expenseService.rejectExpense(householdId, expenseId);
        return ResponseEntity.noContent().build();
	}
	
	//get filter range
	@GetMapping("/{range}/{status}")
	public ResponseEntity<List<CreateExpenseResponseDTO>> getRangeExpense(
			@PathVariable Long householdId,
			@PathVariable TimeRange range,
			@PathVariable ExpenseStatus status
			){
		List<CreateExpenseResponseDTO> response=expenseService.getExpenseByPeriod(status, householdId,range);
		return ResponseEntity.ok(response);
	}
}
	
