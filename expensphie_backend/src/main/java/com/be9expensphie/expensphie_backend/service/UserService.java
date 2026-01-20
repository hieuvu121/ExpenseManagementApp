package com.be9expensphie.expensphie_backend.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.be9expensphie.expensphie_backend.dto.AuthDTO;
import com.be9expensphie.expensphie_backend.dto.UserDTO;
import com.be9expensphie.expensphie_backend.entity.UserEntity;
import com.be9expensphie.expensphie_backend.repository.UserRepository;
import com.be9expensphie.expensphie_backend.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public UserDTO registerUser(UserDTO userDTO) {
        UserEntity newUser = toEntity(userDTO);
        newUser.setActivationToken(UUID.randomUUID().toString());
        newUser = userRepository.save(newUser);
        // Send activation email
        String activationLink = "http://localhost:8080/app/v1/activate?token=" + newUser.getActivationToken();
        String subject = "Activate your Expensphie account";
        String body = "Click on the following link to activate your account: " + activationLink;
        emailService.sendEmail(newUser.getEmail(), subject, body);
        return toDTO(newUser);
    }

    public UserEntity toEntity(UserDTO userDTO) {
        return UserEntity.builder()
                .id(userDTO.getId())
                .fullName(userDTO.getFullName())
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(userDTO.getRole())
                .userImageUrl(userDTO.getUserImageUrl())
                .resetOtp(null)
                .resetOtpExpiresAt(0L)
                .createdAt(userDTO.getCreatedAt())
                .updatedAt(userDTO.getUpdatedAt())
                .build();
    }

    public UserDTO toDTO(UserEntity userEntity) {
        return UserDTO.builder()
                .id(userEntity.getId())
                .fullName(userEntity.getFullName())
                .email(userEntity.getEmail())
                .role(userEntity.getRole())
                .userImageUrl(userEntity.getUserImageUrl())
                .createdAt(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .build();
    }

    public boolean activateUser(String activationToken) {
        return userRepository.findByActivationToken(activationToken)
                .map(user -> {
                    user.setIsActive(true);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    public boolean isAccountActive(String email) {
        return userRepository.findByEmail(email)
                .map(UserEntity::getIsActive)
                .orElse(false);
    }

    public UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new UsernameNotFoundException("Account not found with email: " + authentication.getName()));
    }

    public UserDTO getPublicUser(String email) {
        UserEntity currentUser = null;
        if (email == null) {
            currentUser = getCurrentUser();
        }
        else {
            currentUser = userRepository.findByEmail(email)
                                        .orElseThrow(() -> new UsernameNotFoundException("Account not found with email: " + email));
        }
        return UserDTO.builder()
                        .id(currentUser.getId())
                        .fullName(currentUser.getFullName())
                        .email(currentUser.getEmail())
                        .role(currentUser.getRole())
                        .userImageUrl(currentUser.getUserImageUrl())
                        .createdAt(currentUser.getCreatedAt())
                        .updatedAt(currentUser.getUpdatedAt())
                        .build();
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));
            //Generate JWT token
            String token = jwtUtil.generateToken(authDTO.getEmail());
            return Map.of(
                "token", token,
                "user", getPublicUser(authDTO.getEmail())
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password");
        }
    }

    public void sendResetOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                                                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        //Generate 6 digit otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        //Calculate expirytime(current time + 2 minutes in millisecond)
        long expiryTime = System.currentTimeMillis() + (2 * 60 * 1000);

        //Update userentity
        existingUser.setResetOtp(otp);
        existingUser.setResetOtpExpiresAt(expiryTime);

        userRepository.save(existingUser);

        try {
            // Send reset otp email
            String subject = "Password Reset OTP";
            String body = "Your OTP is " + otp + ". Please use this to reset your password within 2 minutes";
            emailService.sendEmail(email, subject, body);
        } catch (Exception e) {
            throw new RuntimeException("Unable to send email");
        }
    }

    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity existingUser = userRepository.findByEmail(email)
                                                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        if (existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if (existingUser.getResetOtpExpiresAt() < System.currentTimeMillis()) {
            throw new RuntimeException("OTP expired");
        }

        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null);
        existingUser.setResetOtpExpiresAt(0L);

        userRepository.save(existingUser);
    }
}
