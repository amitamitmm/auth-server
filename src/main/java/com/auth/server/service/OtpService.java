package com.auth.server.service;

import com.auth.server.entity.Otp;
import com.auth.server.repository.OtpRepository;
import com.auth.server.util.OtpGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final OtpGenerator otpGenerator;
    private final EmailService emailService;
    private final SmsService smsService;

    @Value("${otp.expiration}")
    private long otpExpirationMs;

    @Transactional
    public String generateAndSendOtp(String identifier, Otp.OtpType type, Otp.OtpPurpose purpose) {
        // Delete any existing OTPs for this identifier and purpose
        otpRepository.deleteByIdentifierAndTypeAndPurpose(identifier, type, purpose);

        // Generate new OTP
        String otpCode = otpGenerator.generateOtp();
        
        // Calculate expiry time
        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(otpExpirationMs / 1000);

        // Create and save OTP entity
        Otp otp = new Otp();
        otp.setOtp(otpCode);
        otp.setIdentifier(identifier);
        otp.setType(type);
        otp.setPurpose(purpose);
        otp.setExpiryTime(expiryTime);
        otp.setCreatedAt(LocalDateTime.now());
        otp.setUsed(false);

        otpRepository.save(otp);

        // Send OTP via appropriate channel
        if (type == Otp.OtpType.EMAIL) {
            emailService.sendOtpEmail(identifier, otpCode, purpose.name());
        } else if (type == Otp.OtpType.SMS) {
            smsService.sendOtpSms(identifier, otpCode, purpose.name());
        }

        return otpCode;
    }

    public boolean verifyOtp(String identifier, String otpCode, Otp.OtpType type, Otp.OtpPurpose purpose) {
        Optional<Otp> otpOptional = otpRepository.findByIdentifierAndOtpAndTypeAndPurposeAndUsedFalse(
                identifier, otpCode, type, purpose
        );

        if (otpOptional.isEmpty()) {
            return false;
        }

        Otp otp = otpOptional.get();

        // Check if OTP is expired
        if (otp.isExpired()) {
            return false;
        }

        // Mark OTP as used
        otp.setUsed(true);
        otp.setUsedAt(LocalDateTime.now());
        otpRepository.save(otp);

        return true;
    }

    public boolean isOtpValid(String identifier, Otp.OtpType type, Otp.OtpPurpose purpose) {
        Optional<Otp> otpOptional = otpRepository.findTopByIdentifierAndTypeAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                identifier, type, purpose
        );

        return otpOptional.isPresent() && !otpOptional.get().isExpired();
    }

    @Transactional
    public void resendOtp(String identifier, Otp.OtpType type, Otp.OtpPurpose purpose) {
        // Check if there's a recent valid OTP
        Optional<Otp> recentOtp = otpRepository.findTopByIdentifierAndTypeAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                identifier, type, purpose
        );

        // If OTP was created less than 1 minute ago, throw exception
        if (recentOtp.isPresent() && 
            recentOtp.get().getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1))) {
            throw new RuntimeException("Please wait before requesting a new OTP");
        }

        // Generate and send new OTP
        generateAndSendOtp(identifier, type, purpose);
    }

    // Clean up expired OTPs every hour
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiryTimeBefore(LocalDateTime.now());
    }
}
