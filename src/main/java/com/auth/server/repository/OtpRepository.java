package com.auth.server.repository;

import com.auth.server.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    
    Optional<Otp> findByIdentifierAndOtpAndTypeAndPurposeAndUsedFalse(
            String identifier, 
            String otp, 
            Otp.OtpType type, 
            Otp.OtpPurpose purpose
    );
    
    Optional<Otp> findTopByIdentifierAndTypeAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String identifier, 
            Otp.OtpType type, 
            Otp.OtpPurpose purpose
    );
    
    void deleteByExpiryTimeBefore(LocalDateTime dateTime);
    
    void deleteByIdentifierAndTypeAndPurpose(
            String identifier, 
            Otp.OtpType type, 
            Otp.OtpPurpose purpose
    );
}
