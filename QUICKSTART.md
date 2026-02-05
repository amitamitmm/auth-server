# Quick Start Guide

## 🚀 Get Started in 5 Minutes

### Step 1: Prerequisites Check
```bash
java -version    # Should be 17+
mvn -version     # Should be 3.6+
mysql --version  # Should be 8.0+
```

### Step 2: Database Setup
```sql
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE auth_db;

# Verify
SHOW DATABASES;
```

### Step 3: Configure Application

Edit `src/main/resources/application.properties`:

```properties
# 1. Database (REQUIRED)
spring.datasource.url=jdbc:mysql://localhost:3306/auth_db
spring.datasource.username=root
spring.datasource.password=your_mysql_password

# 2. JWT Secret (REQUIRED - Generate new one)
jwt.secret=dGhpc2lzYXNlY3JldGtleWZvcmp3dGF1dGhlbnRpY2F0aW9u

# 3. Email (REQUIRED for OTP)
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password

# 4. SMS (OPTIONAL)
# Leave empty for development, OTPs will be logged
```

### Step 4: Run Application
```bash
# From project root
cd auth-server

# Build
mvn clean install

# Run
mvn spring-boot:run
```

Server starts at: http://localhost:8080

### Step 5: Test with cURL

#### Register a new user:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test@12345",
    "mobileNumber": "1234567890"
  }'
```

#### Check logs for OTP:
```bash
# Email OTP will be in console logs since email is not configured
# Look for: "SMS OTP for 1234567890: XXXXXX"
```

#### Verify Email:
```bash
curl -X POST http://localhost:8080/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "test@example.com",
    "otp": "YOUR_OTP_FROM_LOGS",
    "otpType": "EMAIL",
    "purpose": "REGISTRATION"
  }'
```

#### Login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@12345"
  }'
```

#### Use Access Token:
```bash
# Copy accessToken from login response
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 📧 Gmail Configuration (For Production)

1. Enable 2-Factor Authentication in your Google Account
2. Go to: https://myaccount.google.com/apppasswords
3. Generate an App Password
4. Use this App Password (not your regular password) in application.properties

```properties
spring.mail.username=your_email@gmail.com
spring.mail.password=your_16_char_app_password
```

## 🔐 Generate Secure JWT Secret

```bash
# On Linux/Mac
openssl rand -base64 32

# Or use online generator
# Visit: https://randomkeygen.com/
# Use "CodeIgniter Encryption Keys" (256-bit)
```

## 📱 SMS Configuration (Optional)

### Using Twilio

1. Sign up at: https://www.twilio.com
2. Get Account SID, Auth Token, and Phone Number
3. Add to application.properties:

```properties
sms.provider.account-sid=ACxxxxxxxxxxxxx
sms.provider.auth-token=your_auth_token
sms.provider.phone-number=+1234567890
```

4. Add Twilio dependency to pom.xml:

```xml
<dependency>
    <groupId>com.twilio.sdk</groupId>
    <artifactId>twilio</artifactId>
    <version>9.14.1</version>
</dependency>
```

5. Uncomment Twilio code in `SMSService.java`

## 🔍 Troubleshooting

### Port 8080 already in use
```bash
# Find process
lsof -i :8080

# Kill process
kill -9 <PID>

# Or change port in test.properties
server.port=8081
```

### MySQL Connection Error
```bash
# Check MySQL is running
sudo systemctl status mysql

# Start MySQL
sudo systemctl start mysql

# Test connection
mysql -u root -p -e "SELECT 1"
```

### Email not sending
- Check Gmail App Password is correct
- Verify "Less secure app access" is not needed (use App Password instead)
- Check firewall allows port 587
- Look at application logs for detailed error

### OTP not working
- Check system time is correct (OTPs are time-sensitive)
- Verify OTP hasn't expired (5 minutes)
- Check you haven't exceeded max attempts (3)
- Look in logs for generated OTP during development

## 📦 Import Postman Collection

1. Open Postman
2. Click Import
3. Select `JWT-Auth-API.postman_collection.json`
4. Test all endpoints easily!

## 🎯 Next Steps

1. ✅ Basic setup complete
2. 📧 Configure email properly
3. 📱 Set up SMS (optional)
4. 🔒 Generate production JWT secret
5. 🌐 Deploy to cloud
6. 🔍 Add monitoring
7. 🚀 Build your app!

## 💡 Development Tips

### Using H2 Database (In-Memory)

For quick testing without MySQL:

```properties
# Comment out MySQL config and add:
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Enable H2 Console
spring.h2.console.enabled=true
```

Access H2 Console: http://localhost:8080/h2-console

### Viewing Logs

```bash
# Tail application logs
tail -f logs/spring-boot-logger.log

# Or check console output
mvn spring-boot:run
```

### Database Reset

```sql
DROP DATABASE auth_db;
CREATE DATABASE auth_db;
```

## 📚 Learning Resources

- [Spring Security](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io/)
- [Spring Boot Docs](https://spring.io/projects/spring-boot)

## 🆘 Need Help?

- Check README.md for detailed documentation
- Review API examples in Postman collection
- Check application logs for errors
- Create an issue on GitHub

---

**Ready to build something awesome! 🎉**
