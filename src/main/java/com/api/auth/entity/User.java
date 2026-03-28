package com.api.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import com.api.auth.entity.enums.UserRole;
import java.time.LocalDateTime;

/**
 * User Entity - Represents a user in the system
 * 
 * FLOW:
 * 1. Each user has unique email and username
 * 2. Password is stored (should be encrypted in production)
 * 3. User is assigned a role (ADMIN, NORMAL_USER, PREMIUM_USER, GOVT_OFFICIAL)
 * 4. User has enabled/disabled status for account management
 * 5. Timestamps track creation and last login
 * 
 * TABLE: users
 * COLUMNS: id, username, email, password, role, enabled, created_at, updated_at, last_login
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username - Unique identifier for the user
     * Used during login process
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Email - Unique email address of the user
     * Used for notifications and account recovery
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Password - Encrypted password (should use BCrypt in production)
     * Never transmitted in plain text
     */
    @Column(nullable = false)
    private String password;

    /**
     * Role - The role assigned to this user
     * Determines authorization level
     * Possible values: ADMIN, NORMAL_USER, PREMIUM_USER, GOVT_OFFICIAL
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /**
     * Enabled - Whether the user account is active
     * Disabled accounts cannot login
     */
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * Full name of the user
     */
    @Column(name = "full_name")
    private String fullName;

    /**
     * Phone number of the user
     */
    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * Created timestamp - When the user was registered
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Updated timestamp - When user details were last modified
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Last login timestamp - For tracking user activity
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /**
     * Pre-persist hook - Sets creation timestamp
     * Called before entity is saved to database
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Pre-update hook - Updates the modified timestamp
     * Called before entity is updated in database
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

