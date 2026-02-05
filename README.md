# JWT Authentication Server

A complete Spring Boot-based authentication server with JWT token generation, OTP-based email and mobile verification, and comprehensive user management features.

## Features

- ✅ User Registration with validation
- ✅ Email Verification with OTP
- ✅ Mobile Number Verification with OTP
- ✅ JWT Token-based Authentication
- ✅ Login with username/email and password
- ✅ Change Password (authenticated users)
- ✅ Forgot Password flow with OTP
- ✅ Reset Password with OTP verification
- ✅ Resend OTP functionality
- ✅ Automatic OTP cleanup
- ✅ Comprehensive validation
- ✅ Exception handling

## Tech Stack

- Java 17
- Spring Boot 3.2.0
- Spring Security 6
- Spring Data JPA
- MySQL Database
- JWT (JSON Web Tokens)
- Spring Mail
- Lombok
- Maven

## Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6+
- SMTP server (Gmail) for email
- SMS provider account (Twilio, AWS SNS, etc.) for SMS

## Setup Instructions

### 1. Database Setup

```sql
CREATE DATABASE auth_db;
```

### 2. Configure Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/auth_db
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password

# JWT Secret (use a strong secret key)
jwt.secret=your-strong-secret-key-at-least-32-characters-long
jwt.expiration=86400000

# Gmail Configuration
spring.mail.username=your-email@gmail.com
spring.mail.password=your-gmail-app-password
```

#### Getting Gmail App Password:
1. Go to Google Account settings
2. Enable 2-Step Verification
3. Go to Security → 2-Step Verification → App passwords
4. Generate a new app password for "Mail"
5. Use this password in `spring.mail.password`

### 3. SMS Provider Setup

The application includes placeholder SMS service. To enable SMS functionality:

**Option 1: Twilio**
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>com.twilio.sdk</groupId>
    <artifactId>twilio</artifactId>
    <version>9.2.0</version>
</dependency>
```

Update `SmsService.java` with Twilio implementation (see comments in the file).

### 4. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Register User
**POST** `/api/auth/register`
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "username": "johndoe",
  "email": "john.doe@example.com",
  "password": "Password@123",
  "mobileNumber": "+1234567890"
}
```

### 2. Verify Email
**POST** `/api/auth/verify-email`
```json
{
  "identifier": "john.doe@example.com",
  "otp": "123456",
  "type": "EMAIL"
}
```

### 3. Verify Mobile
**POST** `/api/auth/verify-mobile`
```json
{
  "identifier": "+1234567890",
  "otp": "123456",
  "type": "SMS"
}
```

### 4. Login
**POST** `/api/auth/login`
```json
{
  "username": "johndoe",
  "password": "Password@123"
}
```

### 5. Change Password (Authenticated)
**POST** `/api/auth/change-password`  
**Header:** `Authorization: Bearer <token>`
```json
{
  "oldPassword": "Password@123",
  "newPassword": "NewPassword@123"
}
```

### 6. Forgot Password
**POST** `/api/auth/forgot-password`
```json
{
  "identifier": "john.doe@example.com"
}
```

### 7. Reset Password
**POST** `/api/auth/reset-password`
```json
{
  "identifier": "john.doe@example.com",
  "otp": "123456",
  "newPassword": "NewPassword@123"
}
```

### 8. Resend OTP
**POST** `/api/auth/resend-otp`
```json
{
  "identifier": "john.doe@example.com",
  "type": "EMAIL",
  "purpose": "REGISTRATION"
}
```

## Password Requirements

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (@#$%^&+=)

## Project Structure

```
auth-server/
├── src/main/java/com/auth/server/
│   ├── config/          # Security configuration
│   ├── controller/      # REST controllers
│   ├── dto/             # Data Transfer Objects
│   ├── entity/          # JPA entities
│   ├── exception/       # Exception handlers
│   ├── repository/      # JPA repositories
│   ├── security/        # Security components
│   ├── service/         # Business logic
│   └── util/            # Utility classes
└── src/main/resources/
    └── application.properties
```

## License

MIT License
