package com.auth.server.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @Value("${app.name}")
    private String appName;

    /**
     * Initialize Twilio once at startup
     */
    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    /**
     * Send OTP SMS
     */
    public void sendOtpSms(String mobileNumber, String otp, String purpose) {
        String message = buildSmsMessage(otp, purpose);
            mobileNumber = "+91"+mobileNumber;
        try {
            Message twilioMessage = Message.creator(
                    new PhoneNumber(mobileNumber),
                    new PhoneNumber(fromNumber),
                    message
            ).create();

            System.out.println("OTP SMS sent successfully. SID: " + twilioMessage.getSid());

        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP SMS to " + mobileNumber, e);
        }
    }

    /**
     * Send Welcome SMS
     */
    public void sendWelcomeSms(String mobileNumber, String firstName) {
        String message = String.format(
                "Welcome to %s, %s! Your account has been successfully verified. Thank you for joining us!",
                appName, firstName
        );

        try {
            Message twilioMessage = Message.creator(
                    new PhoneNumber(mobileNumber),
                    new PhoneNumber(fromNumber),
                    message
            ).create();

            System.out.println("Welcome SMS sent successfully. SID: " + twilioMessage.getSid());

        } catch (Exception e) {
            throw new RuntimeException("Failed to send welcome SMS to " + mobileNumber, e);
        }
    }

    /**
     * Build OTP message
     */
    private String buildSmsMessage(String otp, String purpose) {
        String action = switch (purpose.toUpperCase()) {
            case "REGISTRATION" -> "verify your mobile number";
            case "FORGOT_PASSWORD" -> "reset your password";
            case "LOGIN_VERIFICATION" -> "complete your login";
            default -> "verify your account";
        };

        return String.format(
                "%s: Your OTP to %s is %s. Valid for 5 minutes. Do not share this code.",
                appName, action, otp
        );
    }
}
