package com.api.auth.dto;

import lombok.*;

/**
 * RegisterRequest DTO - Data Transfer Object for user registration
 * 
 * FLOW:
 * 1. New user submits registration form with username, email, password, and role
 * 2. Request is validated (email uniqueness, password strength)
 * 3. User account is created
 * 4. Confirmation response is sent back
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String role;  // Role to be assigned (e.g., NORMAL_USER, PREMIUM_USER)
}

