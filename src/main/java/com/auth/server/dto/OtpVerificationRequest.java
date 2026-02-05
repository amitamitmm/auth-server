package com.auth.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationRequest {

    @NotBlank(message = "Identifier (email or mobile) is required")
    private String identifier;

    @NotBlank(message = "OTP is required")
    private String otp;

    @NotBlank(message = "Verification type is required")
    private String type; // EMAIL or SMS
}
