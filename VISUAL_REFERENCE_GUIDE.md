# 🎨 VISUAL REFERENCE GUIDE - JWT Authentication System

## 🔐 COMPLETE AUTHENTICATION & AUTHORIZATION SYSTEM

---

## 📊 SYSTEM COMPONENTS MAP

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT APPLICATION                               │
│                                                                             │
│  Browser / Mobile App / API Client                                         │
└──────────────────────────┬──────────────────────────────────────────────────┘
                           │
                           │ HTTPS
                           ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        SPRING BOOT API SERVER                              │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                     Request Processing Pipeline                     │  │
│  │                                                                      │  │
│  │  1. JwtAuthenticationFilter                                         │  │
│  │     ├─ Extract JWT from Authorization header                       │  │
│  │     ├─ Validate JWT signature                                      │  │
│  │     ├─ Extract username and role                                   │  │
│  │     └─ Set authentication in SecurityContext                       │  │
│  │                                                                      │  │
│  │  2. DispatcherServlet                                              │  │
│  │     └─ Route request to appropriate controller                     │  │
│  │                                                                      │  │
│  │  3. @PreAuthorize Check                                            │  │
│  │     ├─ Verify user has required role                               │  │
│  │     ├─ If authorized → Continue                                    │  │
│  │     └─ If unauthorized → 403 Forbidden                             │  │
│  │                                                                      │  │
│  │  4. Controller Method                                              │  │
│  │     ├─ Execute business logic                                      │  │
│  │     ├─ Access current user via Authentication object               │  │
│  │     └─ Return response                                             │  │
│  │                                                                      │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                        Components & Services                        │  │
│  │                                                                      │  │
│  │  AuthenticationController  → Public endpoints (login/register)      │  │
│  │  AdminController           → Admin-only endpoints                   │  │
│  │  UserController            → User endpoints                         │  │
│  │  PremiumUserController     → Premium user endpoints                 │  │
│  │  GovtOfficialController    → Government endpoints                   │  │
│  │                                                                      │  │
│  │  AuthenticationService     → Login/Register/RefreshToken logic     │  │
│  │  JwtTokenProvider          → JWT generation/validation              │  │
│  │  UserDetailsService        → Load user from database                │  │
│  │                                                                      │  │
│  │  SecurityConfig            → Spring Security configuration          │  │
│  │  BCryptPasswordEncoder     → Password encryption                    │  │
│  │                                                                      │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
└──────────────────────────┬──────────────────────────────────────────────────┘
                           │
                           │ JDBC
                           ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        DATABASE (H2 or MySQL)                              │
│                                                                             │
│  TABLE: users                                                              │
│  ┌──────┬──────────┬───────────────────┬──────────────┬─────────┐         │
│  │ id   │ username │ email             │ password     │ role    │         │
│  ├──────┼──────────┼───────────────────┼──────────────┼─────────┤         │
│  │ 1    │ admin    │ admin@ex.com      │ $2a$10$..    │ ADMIN   │         │
│  │ 2    │ user     │ user@ex.com       │ $2a$10$..    │ USER    │         │
│  │ 3    │ premium  │ premium@ex.com    │ $2a$10$..    │ PREMIUM │         │
│  │ 4    │ govt..   │ official@ex.com   │ $2a$10$..    │ GOVT    │         │
│  └──────┴──────────┴───────────────────┴──────────────┴─────────┘         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 LOGIN FLOW (Complete)

```
CLIENT REQUEST
     │
     │ POST /api/auth/login
     │ {"username":"admin", "password":"Admin@123"}
     ▼
┌────────────────────────────────────────┐
│ 1. AuthenticationController             │
│    login(LoginRequest)                  │
│    ├─ Log: "Login attempt..."           │
│    └─ Call: AuthenticationService       │
└────────────┬───────────────────────────┘
             │
             ▼
┌────────────────────────────────────────┐
│ 2. AuthenticationService.login()        │
│    ├─ Create authentication token       │
│    │  (username, password)              │
│    │                                    │
│    └─ Call: AuthenticationManager       │
│       .authenticate()                   │
└────────────┬───────────────────────────┘
             │
             ▼
┌────────────────────────────────────────┐
│ 3. AuthenticationManager                │
│    (Spring Security Core)               │
│                                         │
│    ├─ Call: DaoAuthenticationProvider   │
│    └─ Trigger: UserDetailsService       │
│       .loadUserByUsername()             │
└────────────┬───────────────────────────┘
             │
             ▼
┌────────────────────────────────────────┐
│ 4. UserDetailsService                   │
│    .loadUserByUsername(username)       │
│                                         │
│    ├─ Call: UserRepository              │
│    │ .findByUsername("admin")           │
│    │                                    │
│    ├─ Load User entity from DB:         │
│    │ {id:1, username:"admin",           │
│    │  email:"admin@ex.com",             │
│    │  password:"$2a$10$...",            │
│    │  role:ADMIN}                       │
│    │                                    │
│    └─ Convert User → UserPrincipal      │
│       (implements UserDetails)          │
│       {username:"admin",                │
│        password:"$2a$10$...",           │
│        authorities:[ROLE_ADMIN]}        │
└────────────┬───────────────────────────┘
             │
             ▼
┌────────────────────────────────────────┐
│ 5. DaoAuthenticationProvider            │
│    Compare Passwords                    │
│                                         │
│    ├─ Submitted: "Admin@123"            │
│    ├─ Stored: "$2a$10$..."              │
│    │  (BCrypt encrypted)                │
│    │                                    │
│    ├─ Use: BCryptPasswordEncoder        │
│    │  .matches(plain, encrypted)        │
│    │                                    │
│    └─ Result: ✅ MATCH or ❌ NO MATCH   │
└────────────┬───────────────────────────┘
             │
    ┌────────┴────────┐
    │                 │
    ▼ YES             ▼ NO
┌──────────┐    ┌───────────┐
│ SUCCESS  │    │ FAILURE   │
└────┬─────┘    └─────┬─────┘
     │                │
     ▼                ▼ Return
┌────────────────────────────────────────┐ AuthException
│ 6. Authentication Successful            │
│    Create Authentication object:        │
│    {principal: UserPrincipal,           │
│     authorities: [ROLE_ADMIN],          │
│     authenticated: true}                │
└────────────┬───────────────────────────┘
             │
             ▼
┌────────────────────────────────────────┐
│ 7. AuthenticationService                │
│    Returns to login()                   │
│                                         │
│    Authentication object ready          │
└────────────┬───────────────────────────┘
             │
             ▼
┌────────────────────────────────────────┐
│ 8. JwtTokenProvider                     │
│    .generateToken(authentication)       │
│                                         │
│    ├─ Extract: username="admin"         │
│    ├─ Extract: role="ROLE_ADMIN"        │
│    ├─ Create JWT payload:               │
│    │  {sub:"admin",                     │
│    │   role:"ROLE_ADMIN",               │
│    │   id:1,                            │
│    │   iat:1711610400,                  │
│    │   exp:1711696800}                  │
│    │                                    │
│    ├─ Sign with:                        │
│    │  HMACSHA256(header+payload,        │
│    │  secret_key)                       │
│    │                                    │
│    └─ Return JWT:                       │
│       "eyJhbGc.eyJzdWI.xxx..."          │
└────────────┬───────────────────────────┘
             │
             ▼
┌────────────────────────────────────────┐
│ 9. JwtTokenProvider                     │
│    .generateRefreshToken(username)      │
│                                         │
│    Similar to access token but:         │
│    ├─ Longer expiration (7 days)        │
│    ├─ type:"REFRESH" claim              │
│    └─ Return refresh token              │
└────────────┬───────────────────────────┘
             │
             ▼
┌────────────────────────────────────────┐
│ 10. Update Last Login                   │
│     user.lastLogin = now()              │
│     userRepository.save(user)           │
└────────────┬───────────────────────────┘
             │
             ▼
┌────────────────────────────────────────┐
│ 11. Return Success Response (200 OK)   │
│                                         │
│  {                                      │
│    "success": true,                     │
│    "message": "Login successful",       │
│    "token": "JWT_TOKEN",                │
│    "refreshToken": "REFRESH_TOKEN",     │
│    "tokenType": "Bearer",               │
│    "expiresIn": 86400000,               │
│    "user": {                            │
│      "id": 1,                           │
│      "username": "admin",               │
│      "email": "admin@example.com",      │
│      "role": "ROLE_ADMIN"               │
│    }                                    │
│  }                                      │
└────────────┬───────────────────────────┘
             │
             ▼
        CLIENT STORES
        TOKEN IN:
        ├─ localStorage
        ├─ sessionStorage
        └─ Memory

        NEXT REQUESTS INCLUDE:
        Authorization: Bearer JWT_TOKEN
```

---

## 🛡️ AUTHORIZATION FLOW (With Protected Endpoint)

```
CLIENT REQUEST
     │
     │ GET /api/admin/dashboard
     │ Header: Authorization: Bearer eyJhbGc...
     ▼
┌────────────────────────────────────────┐
│ 1. JwtAuthenticationFilter              │
│    doFilterInternal()                   │
│                                         │
│    ├─ Extract Authorization header      │
│    │  "Bearer eyJhbGc..."               │
│    │                                    │
│    ├─ Remove "Bearer " prefix           │
│    └─ Get JWT: "eyJhbGc..."             │
└────────────┬───────────────────────────┘
             │
             ▼
┌────────────────────────────────────────┐
│ 2. JwtTokenProvider                     │
│    .validateToken(jwt)                  │
│                                         │
│    ├─ Parse JWT with secret key         │
│    ├─ Verify HMAC-SHA-256 signature     │
│    ├─ Check: exp > now()?               │
│    │                                    │
│    ├─ Possible Errors:                  │
│    │  - SecurityException (bad sig)     │
│    │  - MalformedJwtException (bad fmt) │
│    │  - ExpiredJwtException (expired)   │
│    │  - IllegalArgumentException        │
│    │                                    │
│    └─ Result: ✅ VALID or ❌ INVALID   │
└────────────┬───────────────────────────┘
             │
    ┌────────┴────────┐
    │                 │
    ▼ VALID           ▼ INVALID
┌──────────────────────────────────────┐ │
│ Continue Processing                   │ │
└────────────┬─────────────────────────┘ │
             │                           │
             ▼                           │
┌────────────────────────────────────────┐ │
│ 3. Extract Information from JWT        │ │
│    (JwtTokenProvider methods)           │ │
│                                         │ │
│    ├─ username = jwt.getSubject()       │ │
│    │  Result: "admin"                   │ │
│    │                                    │ │
│    ├─ role = jwt.getClaim("role")       │ │
│    │  Result: "ROLE_ADMIN"              │ │
│    │                                    │ │
│    └─ id = jwt.getClaim("id")           │ │
│       Result: 1                         │ │
└────────────┬──────────────────────────┘ │
             │                            │
             ▼                            │
┌────────────────────────────────────────┐ │
│ 4. Load User from Database             │ │
│    UserDetailsService                   │ │
│    .loadUserByUsername("admin")         │ │
│                                         │ │
│    ├─ Query: SELECT * FROM users        │ │
│    │         WHERE username='admin'     │ │
│    │                                    │ │
│    ├─ Get User entity:                  │ │
│    │  {id:1, username:"admin",          │ │
│    │   email:"admin@ex.com",            │ │
│    │   role:ADMIN, enabled:true}        │ │
│    │                                    │ │
│    └─ Convert to UserPrincipal:         │ │
│       {username:"admin",                │ │
│        authorities:[ROLE_ADMIN]}        │ │
└────────────┬──────────────────────────┘ │
             │                            │
             ▼                            │
┌────────────────────────────────────────┐ │
│ 5. Create Authentication Object        │ │
│    (in JwtAuthenticationFilter)          │ │
│                                         │ │
│    token = new                          │ │
│    UsernamePasswordAuthenticationToken( │ │
│      userPrincipal,                     │ │
│      null,                              │ │
│      authorities: [ROLE_ADMIN]          │ │
│    )                                    │ │
└────────────┬──────────────────────────┘ │
             │                            │
             ▼                            │
┌────────────────────────────────────────┐ │
│ 6. Set Authentication in Context       │ │
│    SecurityContextHolder                │ │
│    .getContext()                        │ │
│    .setAuthentication(token)            │ │
│                                         │ │
│    => User is now authenticated!        │ │
└────────────┬──────────────────────────┘ │
             │                            │
             ▼                            │
┌────────────────────────────────────────┐ │
│ 7. Filter Chain Continues               │ │
│    Request reaches Controller            │ │
│                                         │ │
│    @GetMapping("/dashboard")            │ │
│    @PreAuthorize(                       │ │
│      "hasRole('ADMIN')")                │ │
│    public ResponseEntity<?>             │ │
│    getDashboard()                       │ │
└────────────┬──────────────────────────┘ │
             │                            │
             ▼                            │
┌────────────────────────────────────────┐ │
│ 8. @PreAuthorize Check                  │ │
│    hasRole('ADMIN')?                    │ │
│                                         │ │
│    ├─ Get current authentication        │ │
│    │  from SecurityContext              │ │
│    │                                    │ │
│    ├─ Get authorities:                  │ │
│    │  [ROLE_ADMIN]                      │ │
│    │                                    │ │
│    ├─ Check: ROLE_ADMIN in authorities? │ │
│    │  YES ✅                            │ │
│    │                                    │ │
│    └─ Result: AUTHORIZED                │ │
└────────────┬──────────────────────────┘ │
             │                            │
             ▼                            │
┌────────────────────────────────────────┐ │
│ 9. Method Executes                      │ │
│    Access to:                           │ │
│    - Request object                     │ │
│    - Response object                    │ │
│    - Authentication object              │ │
│      (current user info)                │ │
│                                         │ │
│    String username =                    │ │
│      authentication.getName()           │ │
│      => "admin"                         │ │
└────────────┬──────────────────────────┘ │
             │                            │ │ INVALID
             ▼                            │ TOKEN:
         EXECUTE                          │ Return 401
   Controller Logic                       │ Unauthorized
         & Return                         │
        Response                          │
     (200 OK with data)                   │
                                          │
                                          │ End Processing
             ┌────────────────────────────┘
             │
             ▼
    CLIENT RECEIVES:
    - Success: 200 OK with data
    - Unauthorized: 401 Unauthorized
```

---

## 🎯 ROLE-BASED ACCESS MATRIX

```
┌─────────────────────────────────────────────────────────────────────────┐
│                   ENDPOINT AUTHORIZATION MATRIX                         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Endpoint              │ Method │ Public │ Role Required              │
│  ─────────────────────┼────────┼────────┼──────────────────────────── │
│                                                                         │
│  /api/auth/login      │ POST   │  ✅    │ None (Public)              │
│  /api/auth/register   │ POST   │  ✅    │ None (Public)              │
│  /api/auth/refresh    │ POST   │  ✅    │ None (Public)              │
│                       │        │        │                            │
│  /api/user/profile    │ GET    │  ❌    │ Any (isAuthenticated)      │
│  /api/user/dashboard  │ GET    │  ❌    │ Any (isAuthenticated)      │
│  /api/user/settings   │ GET    │  ❌    │ Any (isAuthenticated)      │
│  /api/user/activity   │ GET    │  ❌    │ Any (isAuthenticated)      │
│                       │        │        │                            │
│  /api/admin/dashboard │ GET    │  ❌    │ ADMIN only                 │
│  /api/admin/users     │ GET    │  ❌    │ ADMIN only                 │
│  /api/admin/settings  │ GET    │  ❌    │ ADMIN only                 │
│                       │        │        │                            │
│  /api/premium/**      │ GET    │  ❌    │ PREMIUM_USER only          │
│  /api/govt/**         │ GET    │  ❌    │ GOVT_OFFICIAL only         │
│                       │        │        │                            │
└─────────────────────────────────────────────────────────────────────────┘

USER ACCESS BY ROLE
├─ ADMIN (Most Permissions)
│  ├─ ✅ /api/admin/**
│  ├─ ✅ /api/user/**
│  ├─ ✅ /api/auth/**
│  ├─ ❌ /api/premium/**
│  └─ ❌ /api/govt/**
│
├─ PREMIUM_USER
│  ├─ ✅ /api/premium/**
│  ├─ ✅ /api/user/**
│  ├─ ✅ /api/auth/**
│  ├─ ❌ /api/admin/**
│  └─ ❌ /api/govt/**
│
├─ GOVT_OFFICIAL
│  ├─ ✅ /api/govt/**
│  ├─ ✅ /api/user/**
│  ├─ ✅ /api/auth/**
│  ├─ ❌ /api/admin/**
│  └─ ❌ /api/premium/**
│
└─ NORMAL_USER (Basic Permissions)
   ├─ ✅ /api/user/**
   ├─ ✅ /api/auth/**
   ├─ ❌ /api/admin/**
   ├─ ❌ /api/premium/**
   └─ ❌ /api/govt/**
```

---

## 📱 REQUEST/RESPONSE EXAMPLES

### LOGIN REQUEST & RESPONSE

```
REQUEST
─────────────────────────────────────────────────
POST /api/auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin@123"
}


RESPONSE (200 OK)
─────────────────────────────────────────────────
HTTP/1.1 200 OK
Content-Type: application/json

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
    "phoneNumber": "9999999999",
    "role": "ROLE_ADMIN",
    "enabled": true,
    "createdAt": "2026-03-28T09:30:00"
  }
}
```

### PROTECTED ENDPOINT REQUEST & RESPONSE

```
REQUEST
─────────────────────────────────────────────────
GET /api/admin/dashboard HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWI...


RESPONSE (200 OK - Authorized)
─────────────────────────────────────────────────
HTTP/1.1 200 OK
Content-Type: application/json

{
  "totalUsers": 150,
  "totalRevenue": 50000,
  "activeUsers": 120,
  "adminMessage": "Welcome to Admin Dashboard"
}


RESPONSE (401 Unauthorized - Invalid Token)
─────────────────────────────────────────────────
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "message": "Unauthorized: Invalid JWT signature"
}


RESPONSE (403 Forbidden - Insufficient Role)
─────────────────────────────────────────────────
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "message": "Forbidden: Access is denied"
}
```

---

## 🔐 JWT TOKEN STRUCTURE

```
JWT Token Format: XXXXX.YYYYY.ZZZZZ

XXXXX = Header (Base64URL encoded JSON)
YYYYY = Payload (Base64URL encoded JSON)
ZZZZZ = Signature (HMACSHA256(header.payload, secret))


HEADER DECODED
──────────────────────────────────────
{
  "alg": "HS256",        // Algorithm
  "typ": "JWT"           // Type
}


PAYLOAD DECODED
──────────────────────────────────────
{
  "sub": "admin",        // Subject (username)
  "role": "ROLE_ADMIN",  // Role claim
  "id": 1,               // User ID claim
  "iat": 1711610400,     // Issued at (seconds)
  "exp": 1711696800      // Expiration (seconds)
}


SIGNATURE
──────────────────────────────────────
HMACSHA256(
  header.payload,
  "your-secret-key"
)


EXAMPLE JWT
──────────────────────────────────────
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJST0xFX0FETUlOIiwiaWQiOjEsImlhdCI6MTcxMTYxMDQwMCwiZXhwIjoxNzExNjk2ODAwfQ.
xxx...
```

---

## 🛠️ LOGGING TRACE EXAMPLE

```
>>> [CONTROLLER] /api/auth/login endpoint called
>>> [CONTROLLER] Login request received for username: admin

>>> [AUTH] Login attempt for username: admin
>>> [AUTH] Authentication successful for user: admin

>>> [JWT] Generating access token for user: admin
>>> [JWT] Token generated successfully. Expires at: 2026-03-28 14:30:00
>>> [JWT] Generating refresh token for user: admin
>>> [JWT] Refresh token generated successfully

>>> [AUTH] Last login timestamp updated for user: admin
>>> [AUTH] Login successful for user: admin with role: ROLE_ADMIN

>>> [CONTROLLER] Login successful - returning 200 OK


[Next Request with JWT]

>>> [FILTER] Processing request - Token present: true
>>> [FILTER] Extracting JWT from request headers
>>> [FILTER] JWT token extracted successfully

>>> [JWT] Validating token
>>> [JWT] Token is valid

>>> [FILTER] JWT token validation successful
>>> [FILTER] Extracting username from token
>>> [JWT] Username extracted: admin

>>> [FILTER] User loaded from database: admin
>>> [USER_SERVICE] Loading user details for username: admin
>>> [USER_SERVICE] User found - ID: 1, Email: admin@example.com, Role: ROLE_ADMIN
>>> [USER_SERVICE] UserPrincipal created successfully for: admin

>>> [FILTER] Authentication set in SecurityContext for user: admin with role: ROLE_ADMIN

>>> [ADMIN] /api/admin/dashboard endpoint called
>>> [ADMIN] Authenticated admin user accessing dashboard
>>> [ADMIN] Dashboard data prepared and returning to admin

[200 OK Response]
```

---

## ✅ QUICK REFERENCE TABLE

| Term | Meaning | Example |
|------|---------|---------|
| JWT | JSON Web Token | eyJhbGc.eyJz... |
| Payload | Data in JWT | {sub: "admin", role: "ROLE_ADMIN"} |
| Signature | Verification part of JWT | HMACSHA256(...) |
| Bearer | Token type | Authorization: Bearer JWT |
| Role | User permission level | ROLE_ADMIN |
| Claim | Data field in JWT | role, id, exp, iat |
| Authenticate | Verify identity | Login with username/password |
| Authorize | Verify permission | Check if user has ADMIN role |
| 401 | Unauthorized | Invalid or missing token |
| 403 | Forbidden | Valid token but insufficient role |

---

**Use this guide as a visual reference for understanding the complete flow!**

