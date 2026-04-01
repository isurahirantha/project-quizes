package com.quizapp.module.admin.service;

import com.quizapp.exception.BadRequestException;
import com.quizapp.exception.ResourceNotFoundException;
import com.quizapp.module.admin.dto.*;
import com.quizapp.module.admin.entity.Admin;
import com.quizapp.module.admin.entity.PasswordResetToken;
import com.quizapp.module.admin.repository.AdminRepository;
import com.quizapp.module.admin.repository.PasswordResetTokenRepository;
import com.quizapp.security.JwtUtil;
import com.quizapp.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminRepository adminRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.reset-token-expiry-ms}")
    private long resetTokenExpiryMs;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Admin admin = adminRepository.findByEmailAndActiveTrue(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Admin account not found"));

        String token = jwtUtil.generateToken(admin.getEmail());
        log.info("Admin logged in: {}", admin.getEmail());

        return LoginResponse.builder()
                .token(token)
                .email(admin.getEmail())
                .fullName(admin.getFullName())
                .mustChangePassword(admin.isMustChangePassword())
                .expiresInMs(jwtExpirationMs)
                .build();
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        Admin admin = adminRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        admin.setMustChangePassword(false);
        adminRepository.save(admin);
        log.info("Admin password changed: {}", email);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail()).orElse(null);
        if (admin == null) {
            // Silently succeed to prevent email enumeration
            log.warn("Password reset requested for non-existent email: {}", request.getEmail());
            return;
        }

        resetTokenRepository.invalidateAllForAdmin(admin.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .admin(admin)
                .token(token)
                .expiresAt(LocalDateTime.now().plusSeconds(resetTokenExpiryMs / 1000))
                .used(false)
                .build();
        resetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(admin.getEmail(), admin.getFullName(), resetLink);
        log.info("Password reset email sent to: {}", admin.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        PasswordResetToken resetToken = resetTokenRepository
                .findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        Admin admin = resetToken.getAdmin();
        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        admin.setMustChangePassword(false);
        adminRepository.save(admin);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);
        log.info("Password reset successful for: {}", admin.getEmail());
    }
}
