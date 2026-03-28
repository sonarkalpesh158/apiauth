package com.api.auth.dto;

import lombok.*;

/**
 * UserDTO - Data Transfer Object for user information
 * 
 * FLOW:
 * 1. User details are fetched from database
 * 2. Converted to DTO (no password included)
 * 3. Sent to client in API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String role;
    private Boolean enabled;
    private String createdAt;
    private String lastLogin;
}

