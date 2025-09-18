package com.grewal.user_management.service;

import com.grewal.user_management.dto.ForgotPasswordRequestDTO;
import com.grewal.user_management.dto.ResetPasswordRequestDTO;
import com.grewal.user_management.dto.VerifyOtpRequestDTO;
import com.grewal.user_management.model.User;
import com.grewal.user_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;

    public String requestOtp(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No user found with this email"));

        String otp = generateNumericOtp();
        user.setResetOtp(otp);
        user.setResetOtpExpiry(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        userRepository.save(user);

        String subject = "Your Password Reset OTP";
        String text = "Your OTP for password reset is: " + otp + "\nIt will expire in " + OTP_EXPIRY_MINUTES + " minutes.";
        try {
            mailService.sendSimpleMail(user.getEmail(), subject, text);
        } catch (Exception e) {
            // Log concise warning without stack trace to avoid alarming logs in dev
            log.warn("Failed to send OTP email to {}: {}", user.getEmail(), e.getMessage());
        }
        return "OTP sent to your email if it exists in our system";
    }

    public boolean verifyOtp(VerifyOtpRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or OTP"));
        return isValidOtp(user, request.getOtp());
    }

    public String resetPassword(ResetPasswordRequestDTO request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("new password doesn't match confirm password");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or OTP"));

        if (!isValidOtp(user, request.getOtp())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        // clear OTP
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);
        userRepository.save(user);
        return "Password reset successful";
    }

    private boolean isValidOtp(User user, String otp) {
        if (user.getResetOtp() == null || user.getResetOtpExpiry() == null) return false;
        if (!user.getResetOtp().equals(otp)) return false;
        return LocalDateTime.now().isBefore(user.getResetOtpExpiry());
    }

    private String generateNumericOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(PasswordResetService.OTP_LENGTH);
        for (int i = 0; i < PasswordResetService.OTP_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
