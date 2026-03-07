package com.be9expensphie.expensphie_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.be9expensphie.expensphie_backend.service.GeminiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("households/{householdId}/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final GeminiService geminiService;

    @GetMapping
    public ResponseEntity<String> getExpenseSuggestions(@PathVariable Long householdId) {
        String suggestions = geminiService.getExpenseSuggestions(householdId);
        return ResponseEntity.ok(suggestions);
    }
}
