package com.api.auth.dto;

import lombok.*;

/**
 * LoginRequest DTO - Data Transfer Object for user login
 * 
 * FLOW:
 * 1. User sends username/email and password
 * 2. Request is validated
 * 3. If valid, JWT token is generated
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    private String username;
    private String password;
}

