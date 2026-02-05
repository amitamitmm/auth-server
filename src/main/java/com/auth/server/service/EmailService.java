package com.auth.server.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    public void sendOtpEmail(String toEmail, String otp, String purpose) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(getEmailSubject(purpose));
            helper.setText(getEmailBody(otp, purpose), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    private String getEmailSubject(String purpose) {
        return switch (purpose.toUpperCase()) {
            case "REGISTRATION" -> appName + " - Verify Your Email";
            case "FORGOT_PASSWORD" -> appName + " - Reset Your Password";
            case "LOGIN_VERIFICATION" -> appName + " - Login Verification Code";
            default -> appName + " - Verification Code";
        };
    }

    private String getEmailBody(String otp, String purpose) {
        String action = switch (purpose.toUpperCase()) {
            case "REGISTRATION" -> "verify your email address";
            case "FORGOT_PASSWORD" -> "reset your password";
            case "LOGIN_VERIFICATION" -> "complete your login";
            default -> "verify your account";
        };

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 30px; border-radius: 5px; }
                    .otp-code { background-color: #fff; border: 2px dashed #4CAF50; padding: 15px; 
                                text-align: center; font-size: 32px; font-weight: bold; 
                                letter-spacing: 5px; margin: 20px 0; color: #4CAF50; }
                    .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        <h2>Hello!</h2>
                        <p>You requested to %s. Please use the following One-Time Password (OTP):</p>
                        <div class="otp-code">%s</div>
                        <p>This OTP is valid for <strong>5 minutes</strong>. Please do not share this code with anyone.</p>
                        <p>If you didn't request this, please ignore this email or contact our support team.</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message, please do not reply to this email.</p>
                        <p>&copy; 2026 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(appName, action, otp, appName);
    }

    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to " + appName);
            helper.setText(getWelcomeEmailBody(firstName), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send welcome email: " + e.getMessage());
        }
    }

    private String getWelcomeEmailBody(String firstName) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2>Welcome to %s, %s!</h2>
                    <p>Thank you for registering with us. Your account has been successfully verified.</p>
                    <p>You can now log in and start using our services.</p>
                    <p>If you have any questions, feel free to contact our support team.</p>
                    <p>Best regards,<br>The %s Team</p>
                </div>
            </body>
            </html>
            """.formatted(appName, firstName, appName);
    }
}
