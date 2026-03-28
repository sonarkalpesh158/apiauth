# JWT Authentication System - QUICK START GUIDE

## 🚀 GETTING STARTED IN 5 MINUTES

### Step 1: Start the Application
```bash
cd C:\Users\sonar\Downloads\apiauth\apiauth
mvn clean install
mvn spring-boot:run
```

**Expected Output:**
```
>>> [INIT] Database is empty, creating demo users...
>>> [INIT] Admin user created - Username: admin, Password: Admin@123
>>> [INIT] Normal user created - Username: user, Password: User@123
>>> [INIT] Premium user created - Username: premium, Password: Premium@123
>>> [INIT] Government official created - Username: govt_official, Password: Govt@123
>>> [INIT] Demo data initialization completed!
```

The application starts on: `http://localhost:8080`

---

## 🔐 DEMO USERS (For Testing)

| Username | Password | Role | Endpoints Access |
|----------|----------|------|-----------------|
| `admin` | `Admin@123` | ADMIN | /api/admin/** |
| `user` | `User@123` | NORMAL_USER | /api/user/** |
| `premium` | `Premium@123` | PREMIUM_USER | /api/premium/** |
| `govt_official` | `Govt@123` | GOVT_OFFICIAL | /api/govt/** |

---

## 📌 COMPLETE AUTHENTICATION FLOW TESTING

### 1️⃣ REGISTER NEW USER

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser123",
    "email": "testuser@example.com",
    "password": "TestPass@123",
    "fullName": "Test User",
    "phoneNumber": "9876543210",
    "role": "PREMIUM_USER"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 5,
    "username": "testuser123",
    "email": "testuser@example.com",
    "fullName": "Test User",
    "phoneNumber": "9876543210",
    "role": "ROLE_PREMIUM_USER",
    "enabled": true,
    "createdAt": "2026-03-28T10:00:00"
  }
}
```

**📝 Notes:**
- Save the `token` for next requests
- Available roles: `ADMIN`, `NORMAL_USER`, `PREMIUM_USER`, `GOVT_OFFICIAL`
- Default role is `NORMAL_USER` if not specified

---

### 2️⃣ LOGIN WITH EXISTING USER

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJST0xFX0FETUlOIiwiaWQiOjEsImlhdCI6MTcxMTYxMDQwMCwiZXhwIjoxNzExNjk2ODAwfQ.xxx",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsInR5cGUiOiJSRUZSRVNIIiwiaWF0IjoxNzExNjEwNDAwLCJleHAiOjE3MTIyMTUyMDB9.xxx",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "fullName": "Administrator",
    "role": "ROLE_ADMIN",
    "enabled": true,
    "createdAt": "2026-03-28T09:30:00",
    "lastLogin": "2026-03-28T10:05:00"
  }
}
```

**📝 Important:** Copy the `token` value - you'll need it for all protected endpoints!

---

### 3️⃣ ACCESS PROTECTED ENDPOINT - USER PROFILE

```bash
# Replace YOUR_JWT_TOKEN with the token from login response
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJST0xFX0FETUlOIiwiaWQiOjEsImlhdCI6MTcxMTYxMDQwMCwiZXhwIjoxNzExNjk2ODAwfQ.xxx"
```

**Response:**
```json
{
  "username": "admin",
  "roles": [
    {
      "authority": "ROLE_ADMIN"
    }
  ],
  "message": "Your profile information",
  "profileStatus": "Active"
}
```

---

### 4️⃣ ROLE-BASED ACCESS TESTING

#### ✅ Access ADMIN endpoint (as admin user)
```bash
curl -X GET http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

**Response:**
```json
{
  "totalUsers": 150,
  "totalRevenue": 50000,
  "activeUsers": 120,
  "adminMessage": "Welcome to Admin Dashboard"
}
```

#### ❌ Access ADMIN endpoint as NORMAL_USER (403 Forbidden)
```bash
# Login as user (not admin)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "User@123"
  }'

# Try to access admin endpoint with user token
curl -X GET http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer <NORMAL_USER_TOKEN>"
```

**Response (403 Forbidden):**
```json
{
  "message": "Forbidden: Access is denied"
}
```

#### ✅ Access PREMIUM endpoint (as premium user)
```bash
curl -X GET http://localhost:8080/api/premium/features \
  -H "Authorization: Bearer <PREMIUM_USER_TOKEN>"
```

**Response:**
```json
{
  "username": "premium",
  "advancedAnalytics": true,
  "prioritySupport": true,
  "customReports": true,
  "apiAccess": true,
  "storageLimit": "1TB",
  "message": "Welcome to Premium Features"
}
```

#### ✅ Access GOVT endpoint (as govt official)
```bash
curl -X GET http://localhost:8080/api/govt/portal \
  -H "Authorization: Bearer <GOVT_OFFICIAL_TOKEN>"
```

**Response:**
```json
{
  "username": "govt_official",
  "officialLevel": "State",
  "department": "Finance",
  "accessLevel": "Restricted",
  "availableForms": ["Form-101", "Form-201", "Form-301"],
  "message": "Welcome to Government Portal"
}
```

---

### 5️⃣ REFRESH EXPIRED TOKEN

When access token expires after 24 hours, use refresh token to get new access token:

```bash
curl -X POST http://localhost:8080/api/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}'
```

**Response:**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

---

## 🔍 TRACE AUTHENTICATION FLOW IN LOGS

Every action is logged with `>>>` prefix. Monitor the console output:

### Registration Flow Logs:
```
>>> [CONTROLLER] /api/auth/register endpoint called
>>> [CONTROLLER] Registration request received for username: testuser123
>>> [AUTH] Registration attempt for username: testuser123
>>> [AUTH] User entity created - Username: testuser123, Email: testuser@example.com, Role: ROLE_PREMIUM_USER
>>> [AUTH] User saved to database with ID: 5
>>> [JWT] Generating access token for user: testuser123
>>> [JWT] Token generated successfully. Expires at: 2026-03-28 14:30:00
>>> [JWT] Generating refresh token for user: testuser123
>>> [AUTH] User registration successful - Username: testuser123, Role: ROLE_PREMIUM_USER
>>> [CONTROLLER] Registration successful - returning 201 CREATED
```

### Login Flow Logs:
```
>>> [CONTROLLER] /api/auth/login endpoint called
>>> [CONTROLLER] Login request received for username: admin
>>> [AUTH] Login attempt for username: admin
>>> [AUTH] Authentication successful for user: admin
>>> [AUTH] JWT token generated for user: admin
>>> [AUTH] Refresh token generated for user: admin
>>> [AUTH] Last login timestamp updated for user: admin
>>> [AUTH] Login successful for user: admin with role: ROLE_ADMIN
>>> [CONTROLLER] Login successful - returning 200 OK
```

### Protected Endpoint Access Logs:
```
>>> [FILTER] Processing request - Token present: true
>>> [FILTER] JWT token validation successful
>>> [FILTER] Username extracted from token: admin
>>> [USER_SERVICE] Loading user details for username: admin
>>> [USER_SERVICE] User found - ID: 1, Email: admin@example.com, Role: ROLE_ADMIN
>>> [FILTER] Authentication set in SecurityContext for user: admin with role: ROLE_ADMIN
>>> [ADMIN] /api/admin/dashboard endpoint called
>>> [ADMIN] Dashboard data prepared and returning to admin
```

---

## ✅ COMPLETE TEST SCENARIOS

### Scenario 1: Admin User Full Flow

```bash
# 1. LOGIN
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123"
  }' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

# 2. ACCESS OWN PROFILE (Any user)
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer $TOKEN"

# 3. ACCESS ADMIN DASHBOARD (Admin only)
curl -X GET http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer $TOKEN"

# 4. ACCESS ADMIN USERS LIST (Admin only)
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $TOKEN"
```

### Scenario 2: Premium User Testing

```bash
# 1. LOGIN
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "premium",
    "password": "Premium@123"
  }' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

# 2. ACCESS OWN PROFILE
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer $TOKEN"

# 3. ACCESS PREMIUM FEATURES (Premium only)
curl -X GET http://localhost:8080/api/premium/features \
  -H "Authorization: Bearer $TOKEN"

# 4. TRY ADMIN DASHBOARD (Should fail)
curl -X GET http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer $TOKEN"
# Response: 403 Forbidden
```

### Scenario 3: Government Official Testing

```bash
# 1. LOGIN
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "govt_official",
    "password": "Govt@123"
  }' | grep -o '"token":"[^"]*' | cut -d'"' -f4)

# 2. ACCESS GOVERNMENT PORTAL (Govt only)
curl -X GET http://localhost:8080/api/govt/portal \
  -H "Authorization: Bearer $TOKEN"

# 3. ACCESS CITIZEN DATA (Govt only)
curl -X GET http://localhost:8080/api/govt/citizen-data \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📊 API ENDPOINT SUMMARY

| Method | Endpoint | Auth Required | Role Required | Description |
|--------|----------|----------------|---------------|-------------|
| POST | /api/auth/register | ❌ No | - | Register new user |
| POST | /api/auth/login | ❌ No | - | Login user |
| POST | /api/auth/refresh-token | ❌ No | - | Refresh access token |
| GET | /api/user/profile | ✅ Yes | Any | Get user profile |
| GET | /api/user/dashboard | ✅ Yes | Any | Get user dashboard |
| GET | /api/user/settings | ✅ Yes | Any | Get user settings |
| GET | /api/user/activity | ✅ Yes | Any | Get user activity |
| GET | /api/admin/dashboard | ✅ Yes | ADMIN | Admin dashboard |
| GET | /api/admin/users | ✅ Yes | ADMIN | List all users |
| GET | /api/admin/settings | ✅ Yes | ADMIN | System settings |
| GET | /api/premium/features | ✅ Yes | PREMIUM_USER | Premium features |
| GET | /api/premium/analytics | ✅ Yes | PREMIUM_USER | Advanced analytics |
| GET | /api/premium/support | ✅ Yes | PREMIUM_USER | Priority support |
| GET | /api/premium/resources | ✅ Yes | PREMIUM_USER | Premium resources |
| GET | /api/govt/portal | ✅ Yes | GOVT_OFFICIAL | Government portal |
| GET | /api/govt/citizen-data | ✅ Yes | GOVT_OFFICIAL | Citizen data |
| GET | /api/govt/compliance | ✅ Yes | GOVT_OFFICIAL | Compliance reports |
| GET | /api/govt/approvals | ✅ Yes | GOVT_OFFICIAL | Approval workflow |

---

## 🔐 HTTP STATUS CODES

| Code | Meaning | Example |
|------|---------|---------|
| 200 | ✅ OK | Successful request |
| 201 | ✅ Created | User registered successfully |
| 400 | ❌ Bad Request | Invalid input (missing fields, weak password) |
| 401 | ❌ Unauthorized | Invalid credentials or expired token |
| 403 | ❌ Forbidden | Insufficient permissions for role |
| 500 | ❌ Server Error | Internal server error |

---

## 🆘 COMMON ISSUES & FIXES

### Issue: "Unauthorized" on Login
```
{
  "success": false,
  "message": "Invalid username or password"
}
```
**Fix:** Check username and password spelling. Demo users are case-sensitive!

### Issue: "Access is denied" (403 Forbidden)
```
{
  "message": "Forbidden: Access is denied"
}
```
**Fix:** Your user role doesn't have permission. Use correct user:
- Admin endpoints → Login as `admin`
- Premium endpoints → Login as `premium`
- Govt endpoints → Login as `govt_official`

### Issue: "Invalid JWT signature"
```
{
  "message": "Unauthorized: Invalid JWT signature"
}
```
**Fix:** Token is invalid or expired. Login again to get new token.

### Issue: "JWT claims string is empty"
```
Missing Authorization header or token is missing
```
**Fix:** Include header: `Authorization: Bearer <your_token>`

---

## 📱 USING POSTMAN

1. **Create Environment Variable**
   - Click "Environments" → "New"
   - Add `baseUrl`: `http://localhost:8080`
   - Add `accessToken`: (empty)

2. **Create Login Request**
   - POST {{baseUrl}}/api/auth/login
   - Body: `{"username":"admin","password":"Admin@123"}`
   - Tests (Extract token):
     ```javascript
     pm.environment.set("accessToken", pm.response.json().token);
     ```

3. **Create Protected Request**
   - GET {{baseUrl}}/api/admin/dashboard
   - Header: `Authorization` → `Bearer {{accessToken}}`

4. **Run Requests in Order**
   - First run Login request (sets token)
   - Then run Protected request (uses token)

---

## 📚 DETAILED DOCUMENTATION

See `API_AUTHENTICATION_GUIDE.md` for:
- Complete system architecture
- Detailed flow diagrams
- Security best practices
- Database schema
- Troubleshooting guide
- File structure

---

## ✨ NEXT STEPS

1. ✅ Test with demo users
2. ✅ Create new users with different roles
3. ✅ Try accessing endpoints with different roles
4. ✅ Check console logs for authentication flow
5. ✅ Read API_AUTHENTICATION_GUIDE.md for advanced features
6. ⚠️ In production:
   - Change JWT secret
   - Enable HTTPS
   - Disable H2 console
   - Add rate limiting
   - Setup monitoring

---

**Happy Testing! 🚀**

For any questions, check the logs with `>>>` prefix in console output!

