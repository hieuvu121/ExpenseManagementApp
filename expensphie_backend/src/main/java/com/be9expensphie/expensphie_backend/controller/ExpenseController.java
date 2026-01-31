package com.be9expensphie.expensphie_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.be9expensphie.expensphie_backend.dto.HouseholdDTO;
import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseRequestDTO;
import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseResponseDTO;
import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;
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
	@GetMapping
	public ResponseEntity<List<CreateExpenseResponseDTO>> getExpenses(
			@PathVariable Long householdId
			){
		List<CreateExpenseResponseDTO> expenses=expenseService.getExpense(householdId);
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

}
