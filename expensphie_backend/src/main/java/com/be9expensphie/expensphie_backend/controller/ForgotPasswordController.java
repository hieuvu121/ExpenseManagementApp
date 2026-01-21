package com.be9expensphie.expensphie_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.be9expensphie.expensphie_backend.service.ForgotPasswordService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordController {
    private final ForgotPasswordService forgotPasswordService;

    // Send email for email verification
    @PostMapping("/verify-email/{email}")
    public ResponseEntity<String> verifyEmail(@PathVariable String email) {
        try {
            forgotPasswordService.verifyEmail(email);
            return ResponseEntity.ok("Email sent for verification!");
        } catch (Exception e) {
            throw new RuntimeException("Verification failed");
        }
    }

    @PostMapping("/verify-otp/{otp}/{email}")
    public ResponseEntity<String> verifyOtp(@PathVariable Integer otp, @PathVariable String email) {
        if (forgotPasswordService.verifyOtp(otp, email)) {
            return ResponseEntity.ok("OTP verified");
        }
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("OTP has expired!");
    }
}
