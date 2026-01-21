package com.be9expensphie.expensphie_backend.service;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.be9expensphie.expensphie_backend.entity.ForgotPasswordEntity;
import com.be9expensphie.expensphie_backend.entity.UserEntity;
import com.be9expensphie.expensphie_backend.repository.ForgotPasswordRepository;
import com.be9expensphie.expensphie_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @SuppressWarnings("null")
    public void verifyEmail(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        Integer otp = ThreadLocalRandom.current().nextInt(100000, 1000000);
        String subject = "OTP for Forgot Password Request";
        String body = "Please use this OTP to reset your password within 2 minutes";
        emailService.sendEmail(email, subject, body);

        ForgotPasswordEntity fp = ForgotPasswordEntity.builder()
                                                    .otp(otp)
                                                    .expirationTime(new Date(System.currentTimeMillis() + 2 * 60 * 1000)) // 2 minutes
                                                    .userEntity(existingUser)
                                                    .build();
        forgotPasswordRepository.save(fp);
    }

    @SuppressWarnings("null")
    public boolean verifyOtp(Integer otp, String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        ForgotPasswordEntity fp = forgotPasswordRepository.findByOtpAndUserEntity(otp, existingUser)
                                        .orElseThrow(() -> new RuntimeException("Invalid OTP for email: " + email));
        if (fp.getExpirationTime().before(new Date())) {
            forgotPasswordRepository.deleteById(fp.getFpid());
            return false;
        }

        forgotPasswordRepository.deleteById(fp.getFpid());
        return true;
    }
}
