# JWT Authentication and Authorization System - Complete Guide

## 📋 TABLE OF CONTENTS
1. [System Architecture](#system-architecture)
2. [Authentication Flow](#authentication-flow)
3. [Authorization Flow](#authorization-flow)
4. [Role-Based Access Control](#role-based-access-control)
5. [API Endpoints](#api-endpoints)
6. [Testing Guide](#testing-guide)
7. [Security Best Practices](#security-best-practices)

---

## 🏗️ SYSTEM ARCHITECTURE

### Component Overview
```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENT APPLICATION                      │
├─────────────────────────────────────────────────────────────┤
│  1. User submits credentials or makes API request           │
│  2. Request sent with JWT token in Authorization header     │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    SPRING BOOT API SERVER                   │
├─────────────────────────────────────────────────────────────┤
│  Request → JWT Auth Filter → Authentication Filter          │
│         → Authorization Check → Controller → Response       │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                      DATABASE (H2/MySQL)                    │
├─────────────────────────────────────────────────────────────┤
│  Stores: Users, Credentials, Roles, Audit Logs              │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

| Component | Purpose | File |
|-----------|---------|------|
| User Entity | Database model for user | `entity/User.java` |
| UserRole Enum | Role definitions | `entity/enums/UserRole.java` |
| JwtTokenProvider | JWT token generation & validation | `security/JwtTokenProvider.java` |
| JwtAuthenticationFilter | Intercepts requests and validates JWT | `security/JwtAuthenticationFilter.java` |
| UserPrincipal | Spring Security user details | `security/UserPrincipal.java` |
| AuthenticationService | Business logic for auth operations | `service/AuthenticationService.java` |
| SecurityConfig | Spring Security configuration | `config/SecurityConfig.java` |
| AuthenticationController | Login/Register/Refresh endpoints | `controller/AuthenticationController.java` |

---

## 🔐 AUTHENTICATION FLOW

### Step-by-Step Login Process

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 1: USER SUBMITS LOGIN CREDENTIALS                     │
├─────────────────────────────────────────────────────────────┤
│ POST /api/auth/login                                        │
│ Body: {                                                     │
│   "username": "john_doe",                                   │
│   "password": "SecurePass123"                               │
│ }                                                           │
└───────────────┬──────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 2: AUTHENTICATE CREDENTIALS (AuthenticationService)  │
├─────────────────────────────────────────────────────────────┤
│ 1. AuthenticationManager receives credentials               │
│ 2. DaoAuthenticationProvider loads user by username         │
│ 3. UserDetailsService.loadUserByUsername() called           │
│ 4. User fetched from database                               │
│ 5. Password encoder compares provided password with stored  │
│    encrypted password using BCrypt                          │
│ 6. If match → Authentication successful                     │
│    If no match → Authentication failed (401)                │
└───────────────┬──────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 3: GENERATE JWT ACCESS TOKEN (JwtTokenProvider)       │
├─────────────────────────────────────────────────────────────┤
│ 1. Extract user info from Authentication object             │
│ 2. Create JWT payload with:                                 │
│    - username (subject)                                     │
│    - role                                                   │
│    - userId                                                 │
│    - issue time (now)                                       │
│    - expiration time (now + 24 hours)                       │
│ 3. Sign token using HMAC-SHA-256 with secret key            │
│ 4. Return encoded JWT token                                 │
│                                                             │
│ JWT Token Format: Header.Payload.Signature                  │
│ Header: {type: JWT, alg: HS256}                             │
│ Payload: {username, role, id, iat, exp}                     │
│ Signature: HMACSHA256(header+payload, secret)               │
└───────────────┬──────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 4: GENERATE REFRESH TOKEN                              │
├─────────────────────────────────────────────────────────────┤
│ 1. Similar to access token but longer expiration (7 days)   │
│ 2. Marked with type: "REFRESH"                              │
│ 3. Used to get new access token without re-login            │
└───────────────┬──────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 5: UPDATE LAST LOGIN TIMESTAMP                         │
├─────────────────────────────────────────────────────────────┤
│ 1. Set user.lastLogin = LocalDateTime.now()                 │
│ 2. Save user to database                                    │
│ 3. Log authentication event                                 │
└───────────────┬──────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 6: RETURN SUCCESS RESPONSE TO CLIENT                   │
├─────────────────────────────────────────────────────────────┤
│ HTTP Status: 200 OK                                         │
│ Response: {                                                 │
│   "success": true,                                          │
│   "message": "Login successful",                            │
│   "token": "eyJhbGc...",                                    │
│   "refreshToken": "eyJhbGc...",                             │
│   "tokenType": "Bearer",                                    │
│   "expiresIn": 86400000,                                    │
│   "user": {                                                 │
│     "id": 1,                                                │
│     "username": "john_doe",                                 │
│     "email": "john@example.com",                            │
│     "role": "ROLE_NORMAL_USER",                             │
│     "enabled": true,                                        │
│     "createdAt": "2026-03-28T10:00:00"                      │
│   }                                                         │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔒 AUTHORIZATION FLOW

### Request with JWT Token

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 1: CLIENT SENDS REQUEST WITH JWT TOKEN                │
├─────────────────────────────────────────────────────────────┤
│ GET /api/user/profile                                       │
│ Header: Authorization: Bearer eyJhbGc...                    │
│                                                             │
│ (Token stored in client after login)                        │
└───────────────┬──────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 2: JWT AUTHENTICATION FILTER PROCESSES REQUEST         │
├─────────────────────────────────────────────────────────────┤
│ 1. JwtAuthenticationFilter intercepts request               │
│ 2. Extract "Authorization" header                           │
│ 3. Remove "Bearer " prefix from token                       │
│ 4. Token string: eyJhbGc...                                 │
└───────────────┬──────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 3: VALIDATE JWT TOKEN (JwtTokenProvider)              │
├─────────────────────────────────────────────────────────────┤
│ 1. Parse token using secret key                             │
│ 2. Verify signature (HMAC-SHA-256)                          │
│ 3. Check expiration time                                    │
│ 4. If signature invalid → Token rejected                    │
│ 5. If expired → Token rejected                              │
│ 6. If valid → Continue to next step                         │
│                                                             │
│ Errors Caught:                                              │
│ - SecurityException: Invalid signature                      │
│ - MalformedJwtException: Invalid format                     │
│ - ExpiredJwtException: Token expired                        │
│ - UnsupportedJwtException: Unsupported format               │
│ - IllegalArgumentException: Empty claims                    │
└───────────────┬──────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 4: EXTRACT USER INFORMATION FROM TOKEN                │
├─────────────────────────────────────────────────────────────┤
│ 1. Get username (subject) from JWT payload                  │
│ 2. Get role from JWT claims                                 │
│ 3. Get user ID from JWT claims                              │
│                                                             │
│ Example Claims:                                             │
│ {                                                           │
│   "sub": "john_doe",        (username)                      │
│   "role": "ROLE_NORMAL_USER",                               │
│   "id": 1,                                                  │
│   "iat": 1711610400,        (issued at)                     │
│   "exp": 1711696800         (expiration)                    │
│ }                                                           │
└───────────────┬──────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 5: LOAD USER DETAILS FROM DATABASE                    │
├─────────────────────────────────────────────────────────────┤
│ 1. UserDetailsService.loadUserByUsername(username)          │
│ 2. Query database: SELECT * FROM users                      │
│    WHERE username = 'john_doe'                              │
│ 3. If user not found → UsernameNotFoundException             │
│ 4. If found → Create UserPrincipal object                   │
│    - Extract role from database                             │
│    - Create authorities: [ROLE_NORMAL_USER]                 │
│    - Build UserPrincipal                                    │
└───────────────┬──────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 6: SET AUTHENTICATION IN SECURITY CONTEXT              │
├─────────────────────────────────────────────────────────────┤
│ 1. Create UsernamePasswordAuthenticationToken:              │
│    - Principal: UserPrincipal object                        │
│    - Credentials: null (already authenticated)              │
│    - Authorities: [ROLE_NORMAL_USER]                        │
│ 2. Add WebAuthenticationDetails                             │
│ 3. Set in SecurityContextHolder:                            │
│    SecurityContext.setAuthentication(token)                 │
│                                                             │
│ Result: User is now "logged in" for this request            │
└───────────────┬──────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 7: REQUEST REACHES CONTROLLER                          │
├─────────────────────────────────────────────────────────────┤
│ 1. Spring DispatcherServlet routes to controller method     │
│ 2. @PreAuthorize annotation checked                         │
│ 3. Example: @PreAuthorize("isAuthenticated()")              │
│                                                             │
│ Authorization Checks:                                       │
│ - isAuthenticated(): User is authenticated                  │
│ - hasRole('ADMIN'): User has ADMIN role                     │
│ - hasRole('PREMIUM_USER'): User has PREMIUM role            │
│ - hasRole('GOVT_OFFICIAL'): User has GOVT role              │
│ - hasAnyRole('ADMIN','PREMIUM_USER'): Multiple roles        │
│                                                             │
│ If authorization fails → 403 FORBIDDEN                      │
│ If authorization passes → Method executed                   │
└───────────────┬──────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 8: ACCESS AUTHENTICATION IN CONTROLLER METHOD          │
├─────────────────────────────────────────────────────────────┤
│ @GetMapping("/profile")                                     │
│ public ResponseEntity<?> getProfile(                        │
│     Authentication authentication) {                        │
│                                                             │
│   String username = authentication.getName();               │
│   String role = authentication.getAuthorities()             │
│                    .iterator().next().getAuthority();       │
│   UserPrincipal principal =                                 │
│       (UserPrincipal) authentication.getPrincipal();        │
│ }                                                           │
└───────────────┬──────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│ STEP 9: RETURN RESPONSE TO CLIENT                           │
├─────────────────────────────────────────────────────────────┤
│ HTTP Status: 200 OK                                         │
│ Body: {                                                     │
│   "username": "john_doe",                                   │
│   "role": "ROLE_NORMAL_USER",                               │
│   "message": "Your profile information"                     │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 👥 ROLE-BASED ACCESS CONTROL

### Available Roles and Permissions

| Role | Description | Endpoints | Permissions |
|------|-------------|-----------|-------------|
| **ADMIN** | System Administrator | /api/admin/** | Full system access, User management, System settings |
| **NORMAL_USER** | Regular User | /api/user/** | Basic user resources, Profile access, Dashboard |
| **PREMIUM_USER** | Premium Subscriber | /api/premium/** | Advanced analytics, Priority support, Custom reports |
| **GOVT_OFFICIAL** | Government Official | /api/govt/** | Citizen data, Compliance reports, Approvals |

### Authorization Rules

```
┌─────────────────────────────────────────────────────────────┐
│ ENDPOINT SECURITY MATRIX                                    │
├─────────────────────────────────────────────────────────────┤
│ Endpoint             │ Method │ Permission               │
├──────────────────────┼────────┼─────────────────────────┤
│ /api/auth/login      │ POST   │ PUBLIC (No auth)        │
│ /api/auth/register   │ POST   │ PUBLIC (No auth)        │
│ /api/auth/refresh    │ POST   │ PUBLIC (No auth)        │
├──────────────────────┼────────┼─────────────────────────┤
│ /api/admin/**        │ GET    │ ROLE_ADMIN only         │
│ /api/premium/**      │ GET    │ ROLE_PREMIUM_USER only  │
│ /api/govt/**         │ GET    │ ROLE_GOVT_OFFICIAL only │
├──────────────────────┼────────┼─────────────────────────┤
│ /api/user/**         │ GET    │ Any authenticated user  │
│ /h2-console/**       │ ANY    │ PUBLIC (Dev only)       │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔌 API ENDPOINTS

### Authentication Endpoints

#### 1. Login Endpoint
```
POST /api/auth/login
Content-Type: application/json

Request Body:
{
  "username": "john_doe",
  "password": "SecurePass123"
}

Response (200 OK):
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "phoneNumber": "1234567890",
    "role": "ROLE_NORMAL_USER",
    "enabled": true,
    "createdAt": "2026-03-28T10:00:00"
  }
}

Response (401 Unauthorized):
{
  "success": false,
  "message": "Invalid username or password"
}
```

#### 2. Register Endpoint
```
POST /api/auth/register
Content-Type: application/json

Request Body:
{
  "username": "new_user",
  "email": "newuser@example.com",
  "password": "SecurePass123",
  "fullName": "New User",
  "phoneNumber": "9876543210",
  "role": "PREMIUM_USER"
}

Available Roles:
- ADMIN
- NORMAL_USER (default if not specified)
- PREMIUM_USER
- GOVT_OFFICIAL

Response (201 Created):
{
  "success": true,
  "message": "User registered successfully",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": { ... }
}

Response (400 Bad Request):
{
  "success": false,
  "message": "Username already exists"
}
```

#### 3. Refresh Token Endpoint
```
POST /api/auth/refresh-token
Content-Type: application/json

Request Body:
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response (200 OK):
{
  "success": true,
  "message": "Token refreshed successfully",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}

Response (401 Unauthorized):
{
  "success": false,
  "message": "Invalid or expired refresh token"
}
```

### Protected Endpoints

#### Admin Endpoints (/api/admin/**)

```
GET /api/admin/dashboard
Authorization: Bearer <JWT_TOKEN>
Required Role: ADMIN

Response (200 OK):
{
  "totalUsers": 150,
  "totalRevenue": 50000,
  "activeUsers": 120,
  "adminMessage": "Welcome to Admin Dashboard"
}

Response (403 Forbidden):
{
  "message": "Forbidden: Access is denied"
}
```

```
GET /api/admin/users
GET /api/admin/settings
```

#### User Endpoints (/api/user/**)

```
GET /api/user/profile
Authorization: Bearer <JWT_TOKEN>
Required Role: Any authenticated user

Response (200 OK):
{
  "username": "john_doe",
  "roles": ["ROLE_NORMAL_USER"],
  "message": "Your profile information",
  "profileStatus": "Active"
}
```

```
GET /api/user/dashboard
GET /api/user/settings
GET /api/user/activity
```

#### Premium User Endpoints (/api/premium/**)

```
GET /api/premium/features
Authorization: Bearer <JWT_TOKEN>
Required Role: PREMIUM_USER

Response (200 OK):
{
  "username": "premium_user",
  "advancedAnalytics": true,
  "prioritySupport": true,
  "customReports": true,
  "apiAccess": true,
  "storageLimit": "1TB",
  "message": "Welcome to Premium Features"
}

Response (403 Forbidden):
{
  "message": "Forbidden: Access is denied"
}
```

```
GET /api/premium/analytics
GET /api/premium/support
GET /api/premium/resources
```

#### Government Official Endpoints (/api/govt/**)

```
GET /api/govt/portal
Authorization: Bearer <JWT_TOKEN>
Required Role: GOVT_OFFICIAL

Response (200 OK):
{
  "username": "govt_official",
  "officialLevel": "State",
  "department": "Finance",
  "accessLevel": "Restricted",
  "availableForms": ["Form-101", "Form-201", "Form-301"],
  "message": "Welcome to Government Portal"
}
```

```
GET /api/govt/citizen-data
GET /api/govt/compliance
GET /api/govt/approvals
```

---

## 🧪 TESTING GUIDE

### Using cURL

```bash
# 1. REGISTER NEW USER
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPass123",
    "fullName": "Test User",
    "phoneNumber": "1234567890",
    "role": "PREMIUM_USER"
  }'

# 2. LOGIN
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "TestPass123"
  }'

# Save the "token" value from response

# 3. USE TOKEN TO ACCESS PROTECTED ENDPOINT
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"

# 4. ACCESS PREMIUM ENDPOINTS
curl -X GET http://localhost:8080/api/premium/features \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"

# 5. REFRESH TOKEN
curl -X POST http://localhost:8080/api/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN_HERE"}'
```

### Using Postman

1. **Create New Request Collection**
   - Name: "API Auth System"

2. **Add Variables**
   - Click "Environments" → Create new environment
   - Add variables:
     - `baseUrl`: `http://localhost:8080`
     - `accessToken`: (empty, will be set after login)
     - `refreshToken`: (empty, will be set after login)

3. **Create Register Request**
   - Method: POST
   - URL: `{{baseUrl}}/api/auth/register`
   - Body:
     ```json
     {
       "username": "postmanuser",
       "email": "postman@example.com",
       "password": "PostmanPass123",
       "fullName": "Postman User",
       "phoneNumber": "1234567890",
       "role": "NORMAL_USER"
     }
     ```
   - Tests (Script):
     ```javascript
     pm.environment.set("accessToken", pm.response.json().token);
     pm.environment.set("refreshToken", pm.response.json().refreshToken);
     ```

4. **Create Login Request**
   - Method: POST
   - URL: `{{baseUrl}}/api/auth/login`
   - Body:
     ```json
     {
       "username": "postmanuser",
       "password": "PostmanPass123"
     }
     ```
   - Tests (Script):
     ```javascript
     pm.environment.set("accessToken", pm.response.json().token);
     pm.environment.set("refreshToken", pm.response.json().refreshToken);
     ```

5. **Create Protected Endpoint Requests**
   - Method: GET
   - URL: `{{baseUrl}}/api/user/profile`
   - Header: `Authorization`: `Bearer {{accessToken}}`

---

## 🔐 SECURITY BEST PRACTICES

### 1. Password Security
- ✅ **Always use BCrypt**: Passwords are encrypted before storage
- ✅ **Never store plain passwords**: Database stores only encrypted hashes
- ✅ **Minimum 6 characters**: Enforced during registration
- ⚠️ **TODO**: Add password strength validation (uppercase, lowercase, numbers, special chars)

### 2. JWT Token Security
- ✅ **HMAC-SHA-256 Signing**: Ensures token integrity
- ✅ **24-hour expiration**: Access tokens automatically expire
- ✅ **7-day refresh tokens**: Allows renewal without re-login
- ⚠️ **SECRET KEY**: Should be at least 256 bits in production
- ⚠️ **HTTPS Only**: Always use HTTPS in production (not HTTP)

### 3. Role-Based Access Control
- ✅ **@PreAuthorize annotations**: Enforces authorization
- ✅ **Multiple role levels**: ADMIN, NORMAL_USER, PREMIUM_USER, GOVT_OFFICIAL
- ✅ **Endpoint protection**: All sensitive endpoints require authentication
- ⚠️ **TODO**: Add role-based audit logging

### 4. Database Security
- ✅ **Parameterized queries**: JPA prevents SQL injection
- ✅ **Unique constraints**: Username and email must be unique
- ✅ **Timestamp tracking**: Monitors user activity (created_at, last_login)
- ⚠️ **TODO**: Add database encryption for sensitive data

### 5. Request/Response Security
- ✅ **No passwords in responses**: User DTOs exclude passwords
- ✅ **Proper HTTP status codes**: 401, 403 for unauthorized/forbidden
- ✅ **CSRF protection disabled**: Stateless JWT doesn't need CSRF
- ⚠️ **TODO**: Add rate limiting to prevent brute force attacks

### 6. Production Checklist
- [ ] Change `jwt.secret` to a strong, random key (256+ bits)
- [ ] Set `spring.jpa.hibernate.ddl-auto` to `validate` (not `update`)
- [ ] Enable HTTPS/SSL
- [ ] Disable H2 console: Remove `/h2-console/**` from SecurityConfig
- [ ] Enable database encryption
- [ ] Add input validation and sanitization
- [ ] Implement rate limiting
- [ ] Add request/response logging
- [ ] Setup monitoring and alerting
- [ ] Regular security audits

### 7. Configuration Security

**application.yaml - PRODUCTION**
```yaml
jwt:
  secret: ${JWT_SECRET_ENV_VAR}  # Load from environment
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days

spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Never create/update tables automatically
    show-sql: false  # Don't log queries
  datasource:
    url: ${DATABASE_URL}  # Load from environment
    username: ${DATABASE_USER}  # Load from environment
    password: ${DATABASE_PASSWORD}  # Load from environment

logging:
  level:
    root: WARN
    com.api.auth: INFO  # Only log important events
```

---

## 📊 DATABASE SCHEMA

### Users Table
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(100),
  phone_number VARCHAR(20),
  role ENUM('ADMIN', 'NORMAL_USER', 'PREMIUM_USER', 'GOVT_OFFICIAL') NOT NULL,
  enabled BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  last_login TIMESTAMP
);
```

---

## 🐛 TROUBLESHOOTING

### Issue: "Invalid JWT signature"
**Cause**: Token was signed with different secret key
**Solution**: Ensure same JWT secret in all instances

### Issue: "Expired JWT token"
**Cause**: Access token expires after 24 hours
**Solution**: Use refresh token to get new access token

### Issue: "User not found"
**Cause**: User was deleted or username changed
**Solution**: Register new user account

### Issue: "Access is denied"
**Cause**: User role doesn't match endpoint requirements
**Solution**: Check user role and use appropriate endpoint

### Issue: "Unauthorized" on protected endpoint
**Cause**: JWT token missing or invalid in header
**Solution**: Ensure Authorization header format: `Bearer <token>`

---

## 📝 LOGGING TRACE

Every operation logs with `>>>` prefix for easy tracking:

```
>>> [AUTH] Login attempt for username: john_doe
>>> [AUTH] Authentication successful for user: john_doe
>>> [JWT] Generating access token for user: john_doe
>>> [JWT] Token generated successfully. Expires at: 2026-03-28 14:30:00
>>> [FILTER] JWT token validation successful
>>> [USER_SERVICE] Loading user details for username: john_doe
>>> [CONTROLLER] Login successful - returning 200 OK
```

Search logs for `>>>` to trace complete authentication flow!

---

## 📚 FILE STRUCTURE

```
src/main/java/com/api/auth/
├── ApiauthApplication.java              (Main Spring Boot Application)
├── config/
│   └── SecurityConfig.java              (Spring Security Configuration)
├── controller/
│   ├── AuthenticationController.java     (Login, Register, Refresh endpoints)
│   ├── AdminController.java              (Admin-only endpoints)
│   ├── UserController.java               (User-accessible endpoints)
│   ├── PremiumUserController.java        (Premium user endpoints)
│   └── GovtOfficialController.java       (Government official endpoints)
├── dto/
│   ├── LoginRequest.java                 (Login request DTO)
│   ├── RegisterRequest.java              (Registration request DTO)
│   ├── AuthResponse.java                 (Authentication response DTO)
│   └── UserDTO.java                      (User information DTO)
├── entity/
│   ├── User.java                         (User entity)
│   └── enums/
│       └── UserRole.java                 (Role enumeration)
├── repository/
│   └── UserRepository.java               (User data access)
└── security/
    ├── JwtTokenProvider.java             (JWT token management)
    ├── JwtAuthenticationFilter.java      (JWT token filter)
    ├── UserDetailServiceImpl.java         (User details service)
    └── UserPrincipal.java                (Spring Security user details)
```

---

## 🚀 QUICK START

1. **Start Application**
   ```
   mvn spring-boot:run
   ```

2. **Register New User**
   ```
   POST /api/auth/register
   ```

3. **Login**
   ```
   POST /api/auth/login
   ```

4. **Use Token to Access Protected Endpoints**
   ```
   GET /api/user/profile
   Authorization: Bearer <token>
   ```

5. **Check Logs**
   - Look for `>>>` prefix in console logs
   - Trace complete authentication flow

---

**For questions or issues, check the logs with `>>>` prefix for detailed trace!**

