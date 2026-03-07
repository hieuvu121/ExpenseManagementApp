package com.be9expensphie.expensphie_backend.service;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.be9expensphie.expensphie_backend.dto.ExpenseDTO.CreateExpenseResponseDTO;
import com.be9expensphie.expensphie_backend.repository.ExpenseRepository;

@Service
public class GeminiService {
    private final ExpenseService expenseService;
    private final ChatClient chatClient;

    public GeminiService(ExpenseService expenseService,
            ExpenseRepository expenseRepo,
            @Qualifier("geminiChatClient") ChatClient chatClient) {
        this.expenseService = expenseService;
        this.chatClient = chatClient;
    }

    public String getExpenseSuggestions(Long householdId) {
        List<CreateExpenseResponseDTO> expenses = expenseService.getExpenseLastMonth(householdId);

        if (expenses.isEmpty()) {
            return "No recent expenses found to analyze.";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Based on the following household expenses from the last month, ")
                .append("provide practical financial suggestions and spending insights:\n\n");

        for (CreateExpenseResponseDTO expense : expenses) {
            prompt.append(String.format("- Category: %s | Currency: %s | Amount: %s | Date: %s%n",
                    expense.getCategory(),
                    expense.getCurrency(),
                    expense.getAmount(),
                    expense.getDate()));
        }

        prompt.append("\nPlease provide (in a friendly language and keep it concise):\n")
                .append("1. Spending pattern analysis\n")
                .append("2. Cost-saving suggestions\n")
                .append("3. Budget recommendations\n");

        return chatClient.prompt(prompt.toString()).call().content();
    }
}
