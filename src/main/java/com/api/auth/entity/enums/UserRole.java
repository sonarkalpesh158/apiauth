package com.api.auth.entity.enums;

/**
 * UserRole Enum - Defines all available roles in the system
 * 
 * FLOW:
 * 1. ADMIN - Has full access to all system resources and can manage users
 * 2. NORMAL_USER - Regular user with limited access to their own resources
 * 3. PREMIUM_USER - Premium user with enhanced features and priority support
 * 4. GOVT_OFFICIAL - Government official with special access to government-related resources
 */
public enum UserRole {
    ADMIN("ROLE_ADMIN", "Administrator with full system access"),
    NORMAL_USER("ROLE_NORMAL_USER", "Regular user with limited access"),
    PREMIUM_USER("ROLE_PREMIUM_USER", "Premium user with enhanced features"),
    GOVT_OFFICIAL("ROLE_GOVT_OFFICIAL", "Government official with special access");

    private final String roleName;
    private final String description;

    /**
     * Constructor to initialize role with name and description
     * 
     * @param roleName The role identifier (e.g., ROLE_ADMIN)
     * @param description Brief description of the role
     */
    UserRole(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }
}

