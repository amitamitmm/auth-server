package com.auth.server.service;

import com.auth.server.dto.*;
import com.auth.server.entity.Otp;
import com.auth.server.entity.User;
import com.auth.server.repository.UserRepository;
import com.auth.server.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final JwtUtil jwtUtil;

    @Transactional
    public ApiResponse registerUser(RegistrationRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return new ApiResponse(false, "Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return new ApiResponse(false, "Email already exists");
        }

        // Check if mobile number already exists
        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            return new ApiResponse(false, "Mobile number already exists");
        }

        // Create new user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMobileNumber(request.getMobileNumber());
        user.setEmailVerified(false);
        user.setMobileVerified(false);
        user.setEnabled(false);
        user.setAccountNonLocked(true);

        userRepository.save(user);

        // Send OTP to email and mobile
        otpService.generateAndSendOtp(user.getEmail(), Otp.OtpType.EMAIL, Otp.OtpPurpose.REGISTRATION);
        otpService.generateAndSendOtp(user.getMobileNumber(), Otp.OtpType.SMS, Otp.OtpPurpose.REGISTRATION);

        return new ApiResponse(true, "Registration successful. Please verify your email and mobile number.");
    }

    @Transactional
    public ApiResponse verifyEmail(OtpVerificationRequest request) {
        boolean isValid = otpService.verifyOtp(
                request.getIdentifier(),
                request.getOtp(),
                Otp.OtpType.EMAIL,
                Otp.OtpPurpose.REGISTRATION
        );

        if (!isValid) {
            return new ApiResponse(false, "Invalid or expired OTP");
        }

        Optional<User> userOptional = userRepository.findByEmail(request.getIdentifier());
        if (userOptional.isEmpty()) {
            return new ApiResponse(false, "User not found");
        }

        User user = userOptional.get();
        user.setEmailVerified(true);

        // If both email and mobile are verified, enable the account
        if (user.isMobileVerified()) {
            user.setEnabled(true);
            emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        }

        userRepository.save(user);

        return new ApiResponse(true, "Email verified successfully");
    }

    @Transactional
    public ApiResponse verifyMobile(OtpVerificationRequest request) {
        boolean isValid = otpService.verifyOtp(
                request.getIdentifier(),
                request.getOtp(),
                Otp.OtpType.SMS,
                Otp.OtpPurpose.REGISTRATION
        );

        if (!isValid) {
            return new ApiResponse(false, "Invalid or expired OTP");
        }

        Optional<User> userOptional = userRepository.findByMobileNumber(request.getIdentifier());
        if (userOptional.isEmpty()) {
            return new ApiResponse(false, "User not found");
        }

        User user = userOptional.get();
        user.setMobileVerified(true);

        // If both email and mobile are verified, enable the account
        if (user.isEmailVerified()) {
            user.setEnabled(true);
            smsService.sendWelcomeSms(user.getMobileNumber(), user.getFirstName());
        }

        userRepository.save(user);

        return new ApiResponse(true, "Mobile number verified successfully");
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by username or email
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(
                request.getUsername(),
                request.getUsername()
        );

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid username or password");
        }

        User user = userOptional.get();

        // Check if password matches
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Check if account is enabled
        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified. Please verify your email and mobile number.");
        }

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            throw new RuntimeException("Account is locked. Please contact support.");
        }

        // Update last login time
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        return new AuthResponse(token, user.getUsername(), user.getEmail(), "Login successful");
    }

    @Transactional
    public ApiResponse changePassword(String username, ChangePasswordRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        
        if (userOptional.isEmpty()) {
            return new ApiResponse(false, "User not found");
        }

        User user = userOptional.get();

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return new ApiResponse(false, "Old password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new ApiResponse(true, "Password changed successfully");
    }

    @Transactional
    public ApiResponse forgotPassword(ForgotPasswordRequest request) {
        // Find user by email or username
        Optional<User> userOptional = userRepository.findByUsernameOrEmail(
                request.getIdentifier(),
                request.getIdentifier()
        );

        if (userOptional.isEmpty()) {
            // Don't reveal if user exists or not for security
            return new ApiResponse(true, "If the account exists, an OTP has been sent to your registered email");
        }

        User user = userOptional.get();

        // Send OTP to user's email
        otpService.generateAndSendOtp(user.getEmail(), Otp.OtpType.EMAIL, Otp.OtpPurpose.FORGOT_PASSWORD);

        return new ApiResponse(true, "OTP has been sent to your registered email");
    }

    @Transactional
    public ApiResponse resetPassword(String email, String otp, String newPassword) {
        // Verify OTP
        boolean isValid = otpService.verifyOtp(email, otp, Otp.OtpType.EMAIL, Otp.OtpPurpose.FORGOT_PASSWORD);

        if (!isValid) {
            return new ApiResponse(false, "Invalid or expired OTP");
        }

        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return new ApiResponse(false, "User not found");
        }

        // Update password
        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return new ApiResponse(true, "Password reset successfully");
    }

    public ApiResponse resendOtp(String identifier, String type, String purpose) {
        try {
            Otp.OtpType otpType = Otp.OtpType.valueOf(type.toUpperCase());
            Otp.OtpPurpose otpPurpose = Otp.OtpPurpose.valueOf(purpose.toUpperCase());

            otpService.resendOtp(identifier, otpType, otpPurpose);
            return new ApiResponse(true, "OTP resent successfully");
        } catch (Exception e) {
            return new ApiResponse(false, e.getMessage());
        }
    }
}
