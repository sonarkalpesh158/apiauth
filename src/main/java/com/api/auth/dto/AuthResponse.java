package com.api.auth.dto;

import lombok.*;

/**
 * AuthResponse DTO - Response object after successful authentication
 * 
 * FLOW:
 * 1. After successful login, this object is created
 * 2. Contains JWT token, refresh token, and user information
 * 3. Sent back to client for API authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String message;
    private Boolean success;
    private String token;  // JWT Access Token
    private String refreshToken;  // Token for refreshing expired access token
    private String tokenType = "Bearer";
    private Long expiresIn;  // Token expiration time in milliseconds
    private UserDTO user;  // User information
}

