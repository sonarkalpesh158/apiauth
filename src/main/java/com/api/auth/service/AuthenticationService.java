package com.api.auth.service;

import com.api.auth.dto.AuthResponse;
import com.api.auth.dto.LoginRequest;
import com.api.auth.dto.RegisterRequest;
import com.api.auth.dto.UserDTO;
import com.api.auth.entity.User;
import com.api.auth.entity.enums.UserRole;
import com.api.auth.repository.UserRepository;
import com.api.auth.security.JwtTokenProvider;
import com.api.auth.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * AuthenticationService - Handles user authentication and authorization
 * 
 * FLOW:
 * 1. Login: Authenticate user with username/password
 * 2. Register: Create new user account with validation
 * 3. Refresh Token: Generate new access token from refresh token
 * 4. Convert user data for responses
 * 
 * This is the business logic layer for authentication
 */
@Slf4j
@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Authenticate user with username and password
     * 
     * FLOW:
     * 1. Validate username and password
     * 2. Check if user exists and is enabled
     * 3. Create JWT token and refresh token
     * 4. Update last login timestamp
     * 5. Return authentication response with tokens
     * 
     * @param loginRequest Login request with username and password
     * @return AuthResponse with JWT token, refresh token, and user info
     */
    public AuthResponse login(LoginRequest loginRequest) {
        log.info(">>> [AUTH] Login attempt for username: {}", loginRequest.getUsername());

        try {
            // Step 1: Authenticate user credentials using AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            log.debug(">>> [AUTH] Authentication successful for user: {}", loginRequest.getUsername());

            // Step 2: Get user from database
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> {
                        log.error(">>> [AUTH] User not found after authentication");
                        return new RuntimeException("User not found");
                    });

            // Step 3: Check if user account is enabled
            if (!user.getEnabled()) {
                log.warn(">>> [AUTH] Login attempt for disabled user: {}", loginRequest.getUsername());
                return AuthResponse.builder()
                        .success(false)
                        .message("User account is disabled")
                        .build();
            }

            // Step 4: Generate JWT token
            String jwtToken = jwtTokenProvider.generateToken(authentication);
            log.debug(">>> [AUTH] JWT token generated for user: {}", loginRequest.getUsername());

            // Step 5: Generate refresh token
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
            log.debug(">>> [AUTH] Refresh token generated for user: {}", loginRequest.getUsername());

            // Step 6: Update last login timestamp
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            log.debug(">>> [AUTH] Last login timestamp updated for user: {}", loginRequest.getUsername());

            // Step 7: Prepare user DTO for response
            UserDTO userDTO = convertUserToDTO(user);

            // Step 8: Build and return success response
            log.info(">>> [AUTH] Login successful for user: {} with role: {}", 
                     loginRequest.getUsername(), user.getRole().getRoleName());
            
            return AuthResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400000L)  // 24 hours
                    .user(userDTO)
                    .build();

        } catch (Exception ex) {
            log.error(">>> [AUTH] Login failed for username: {} - Error: {}", 
                     loginRequest.getUsername(), ex.getMessage());
            
            return AuthResponse.builder()
                    .success(false)
                    .message("Invalid username or password")
                    .build();
        }
    }

    /**
     * Register new user
     * 
     * FLOW:
     * 1. Validate registration request
     * 2. Check if username already exists
     * 3. Check if email already exists
     * 4. Encode password using BCrypt
     * 5. Create user entity with specified role
     * 6. Save user to database
     * 7. Generate JWT and refresh tokens
     * 8. Return success response
     * 
     * @param registerRequest Registration request with user details
     * @return AuthResponse with created user info
     */
    public AuthResponse register(RegisterRequest registerRequest) {
        log.info(">>> [AUTH] Registration attempt for username: {}", registerRequest.getUsername());

        try {
            // Step 1: Check if username already exists
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                log.warn(">>> [AUTH] Registration failed - Username already exists: {}", registerRequest.getUsername());
                return AuthResponse.builder()
                        .success(false)
                        .message("Username already exists")
                        .build();
            }

            // Step 2: Check if email already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                log.warn(">>> [AUTH] Registration failed - Email already exists: {}", registerRequest.getEmail());
                return AuthResponse.builder()
                        .success(false)
                        .message("Email already exists")
                        .build();
            }

            // Step 3: Determine user role
            UserRole role = UserRole.NORMAL_USER;  // Default role
            if (registerRequest.getRole() != null) {
                try {
                    role = UserRole.valueOf(registerRequest.getRole().toUpperCase());
                    log.debug(">>> [AUTH] User role set to: {}", role.getRoleName());
                } catch (IllegalArgumentException ex) {
                    log.warn(">>> [AUTH] Invalid role specified: {}, defaulting to NORMAL_USER", registerRequest.getRole());
                    role = UserRole.NORMAL_USER;
                }
            }

            // Step 4: Create new user entity
            User newUser = User.builder()
                    .username(registerRequest.getUsername())
                    .email(registerRequest.getEmail())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))  // Encrypt password
                    .fullName(registerRequest.getFullName())
                    .phoneNumber(registerRequest.getPhoneNumber())
                    .role(role)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            log.debug(">>> [AUTH] User entity created - Username: {}, Email: {}, Role: {}", 
                     newUser.getUsername(), newUser.getEmail(), role.getRoleName());

            // Step 5: Save user to database
            User savedUser = userRepository.save(newUser);
            log.info(">>> [AUTH] User saved to database with ID: {}", savedUser.getId());

            // Step 6: Generate tokens (simulate authentication)
            String jwtToken = jwtTokenProvider.generateToken(
                    new UsernamePasswordAuthenticationToken(
                            savedUser.getUsername(),
                            null,
                            UserPrincipal.create(savedUser).getAuthorities()
                    )
            );
            log.debug(">>> [AUTH] JWT token generated for new user");

            String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getUsername());
            log.debug(">>> [AUTH] Refresh token generated for new user");

            // Step 7: Prepare user DTO for response
            UserDTO userDTO = convertUserToDTO(savedUser);

            // Step 8: Return success response
            log.info(">>> [AUTH] User registration successful - Username: {}, Role: {}", 
                     savedUser.getUsername(), role.getRoleName());
            
            return AuthResponse.builder()
                    .success(true)
                    .message("User registered successfully")
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400000L)  // 24 hours
                    .user(userDTO)
                    .build();

        } catch (Exception ex) {
            log.error(">>> [AUTH] Registration failed for username: {} - Error: {}", 
                     registerRequest.getUsername(), ex.getMessage());
            
            return AuthResponse.builder()
                    .success(false)
                    .message("Registration failed: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * Refresh expired access token
     * 
     * FLOW:
     * 1. Validate refresh token
     * 2. Extract username from refresh token
     * 3. Load user from database
     * 4. Generate new access token
     * 5. Return new access token
     * 
     * @param refreshToken The refresh token
     * @return AuthResponse with new access token
     */
    public AuthResponse refreshToken(String refreshToken) {
        log.info(">>> [AUTH] Refresh token request received");

        try {
            // Step 1: Validate refresh token
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                log.warn(">>> [AUTH] Invalid or expired refresh token");
                return AuthResponse.builder()
                        .success(false)
                        .message("Invalid or expired refresh token")
                        .build();
            }

            // Step 2: Extract username from refresh token
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            log.debug(">>> [AUTH] Username extracted from refresh token: {}", username);

            // Step 3: Load user from database
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.error(">>> [AUTH] User not found for refresh: {}", username);
                        return new RuntimeException("User not found");
                    });

            // Step 4: Generate new access token
            String newAccessToken = jwtTokenProvider.generateToken(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            null,
                            UserPrincipal.create(user).getAuthorities()
                    )
            );
            log.info(">>> [AUTH] New access token generated for user: {}", username);

            // Step 5: Return response with new token
            return AuthResponse.builder()
                    .success(true)
                    .message("Token refreshed successfully")
                    .token(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(86400000L)
                    .build();

        } catch (Exception ex) {
            log.error(">>> [AUTH] Token refresh failed - Error: {}", ex.getMessage());
            
            return AuthResponse.builder()
                    .success(false)
                    .message("Token refresh failed: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * Convert User entity to UserDTO
     * 
     * FLOW:
     * 1. Extract user information from entity
     * 2. Create DTO without password
     * 3. Return DTO for safe transmission
     * 
     * @param user User entity
     * @return UserDTO with user information
     */
    private UserDTO convertUserToDTO(User user) {
        log.debug(">>> [AUTH] Converting User entity to UserDTO");
        
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().getRoleName())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt().toString())
                .lastLogin(user.getLastLogin() != null ? user.getLastLogin().toString() : null)
                .build();
    }
}

