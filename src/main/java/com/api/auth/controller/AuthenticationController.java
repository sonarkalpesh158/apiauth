package com.api.auth.controller;

import com.api.auth.dto.AuthResponse;
import com.api.auth.dto.LoginRequest;
import com.api.auth.dto.RegisterRequest;
import com.api.auth.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthenticationController - REST endpoints for authentication and authorization
 * 
 * FLOW:
 * 1. POST /api/auth/login - User logs in with username and password
 * 2. POST /api/auth/register - New user registers with email
 * 3. POST /api/auth/refresh-token - Refresh expired access token
 * 
 * BASE URL: /api/auth
 * All endpoints in this controller are PUBLIC (no authentication required)
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * User Login Endpoint
     * 
     * FLOW:
     * 1. Receive login request with username and password
     * 2. Call authentication service to verify credentials
     * 3. If valid, generate JWT and refresh tokens
     * 4. Return tokens and user information
     * 5. If invalid, return error message
     * 
     * HTTP METHOD: POST
     * ENDPOINT: /api/auth/login
     * REQUEST BODY: {"username": "user1", "password": "password123"}
     * RESPONSE: {"token": "jwt...", "refreshToken": "refresh...", "user": {...}}
     * STATUS: 200 OK on success, 401 UNAUTHORIZED on failure
     * 
     * @param loginRequest Login request with username and password
     * @return ResponseEntity with authentication response
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info(">>> [CONTROLLER] /api/auth/login endpoint called");
        log.info(">>> [CONTROLLER] Login request received for username: {}", loginRequest.getUsername());

        try {
            // Call authentication service
            AuthResponse response = authenticationService.login(loginRequest);

            // Return appropriate status code
            if (response.getSuccess()) {
                log.info(">>> [CONTROLLER] Login successful - returning 200 OK");
                return ResponseEntity.ok(response);
            } else {
                log.warn(">>> [CONTROLLER] Login failed - returning 401 UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception ex) {
            log.error(">>> [CONTROLLER] Error in login endpoint: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder()
                            .success(false)
                            .message("Login failed: " + ex.getMessage())
                            .build());
        }
    }

    /**
     * User Registration Endpoint
     * 
     * FLOW:
     * 1. Receive registration request with user details
     * 2. Validate request data
     * 3. Call authentication service to create new user
     * 4. If successful, generate JWT and refresh tokens
     * 5. Return success response with user info
     * 6. If failed, return error message
     * 
     * HTTP METHOD: POST
     * ENDPOINT: /api/auth/register
     * REQUEST BODY: {
     *   "username": "newuser",
     *   "email": "user@example.com",
     *   "password": "SecurePass123!",
     *   "fullName": "John Doe",
     *   "phoneNumber": "1234567890",
     *   "role": "NORMAL_USER"
     * }
     * RESPONSE: {"token": "jwt...", "user": {...}}
     * STATUS: 201 CREATED on success, 400 BAD REQUEST on failure
     * 
     * AVAILABLE ROLES:
     * - ADMIN (system administrator)
     * - NORMAL_USER (regular user - default)
     * - PREMIUM_USER (premium subscriber)
     * - GOVT_OFFICIAL (government official)
     * 
     * @param registerRequest Registration request with user details
     * @return ResponseEntity with authentication response
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        log.info(">>> [CONTROLLER] /api/auth/register endpoint called");
        log.info(">>> [CONTROLLER] Registration request received for username: {}", registerRequest.getUsername());
        log.info(">>> [CONTROLLER] Email: {}, Full Name: {}, Requested Role: {}", 
                 registerRequest.getEmail(), registerRequest.getFullName(), registerRequest.getRole());

        try {
            // Validate request data
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                log.warn(">>> [CONTROLLER] Invalid registration - username is empty");
                return ResponseEntity.badRequest()
                        .body(AuthResponse.builder()
                                .success(false)
                                .message("Username is required")
                                .build());
            }

            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                log.warn(">>> [CONTROLLER] Invalid registration - email is empty");
                return ResponseEntity.badRequest()
                        .body(AuthResponse.builder()
                                .success(false)
                                .message("Email is required")
                                .build());
            }

            if (registerRequest.getPassword() == null || registerRequest.getPassword().length() < 6) {
                log.warn(">>> [CONTROLLER] Invalid registration - password too short");
                return ResponseEntity.badRequest()
                        .body(AuthResponse.builder()
                                .success(false)
                                .message("Password must be at least 6 characters")
                                .build());
            }

            // Call authentication service
            AuthResponse response = authenticationService.register(registerRequest);

            // Return appropriate status code
            if (response.getSuccess()) {
                log.info(">>> [CONTROLLER] Registration successful - returning 201 CREATED");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                log.warn(">>> [CONTROLLER] Registration failed - returning 400 BAD REQUEST");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception ex) {
            log.error(">>> [CONTROLLER] Error in register endpoint: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder()
                            .success(false)
                            .message("Registration failed: " + ex.getMessage())
                            .build());
        }
    }

    /**
     * Refresh Token Endpoint
     * 
     * FLOW:
     * 1. Receive refresh token in header or body
     * 2. Validate refresh token
     * 3. If valid, generate new access token
     * 4. Return new access token
     * 5. If invalid, return error message
     * 
     * HTTP METHOD: POST
     * ENDPOINT: /api/auth/refresh-token
     * REQUEST BODY: {"refreshToken": "refresh_token_string"}
     * RESPONSE: {"token": "new_jwt...", "tokenType": "Bearer"}
     * STATUS: 200 OK on success, 401 UNAUTHORIZED on failure
     * 
     * Use this endpoint when access token expires but you still have a valid refresh token
     * 
     * @param refreshToken The refresh token
     * @return ResponseEntity with new access token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody String refreshToken) {
        log.info(">>> [CONTROLLER] /api/auth/refresh-token endpoint called");
        log.debug(">>> [CONTROLLER] Refresh token request received");

        try {
            // Remove Bearer prefix if present
            if (refreshToken.startsWith("Bearer ")) {
                refreshToken = refreshToken.substring(7);
            }

            // Call authentication service
            AuthResponse response = authenticationService.refreshToken(refreshToken);

            // Return appropriate status code
            if (response.getSuccess()) {
                log.info(">>> [CONTROLLER] Token refresh successful - returning 200 OK");
                return ResponseEntity.ok(response);
            } else {
                log.warn(">>> [CONTROLLER] Token refresh failed - returning 401 UNAUTHORIZED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (Exception ex) {
            log.error(">>> [CONTROLLER] Error in refresh-token endpoint: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder()
                            .success(false)
                            .message("Token refresh failed: " + ex.getMessage())
                            .build());
        }
    }
}

