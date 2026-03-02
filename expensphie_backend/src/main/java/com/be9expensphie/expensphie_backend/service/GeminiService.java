package com.be9expensphie.expensphie_backend.service;

import org.springframework.stereotype.Service;

import com.be9expensphie.expensphie_backend.repository.ExpenseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeminiService {
    private final ExpenseService expenseService;
    private final ExpenseRepository expenseRepo;

    public String getExpenseSuggestions() {
        return "Placeholder for expense suggestions based on recent expenses.";
    }
}
