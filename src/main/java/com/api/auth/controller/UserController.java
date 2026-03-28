package com.api.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.preauthorize.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * UserController - User-accessible endpoints
 * 
 * FLOW:
 * 1. These endpoints are accessible by ANY authenticated user
 * 2. Users can access their own profile and resources
 * 3. @PreAuthorize("isAuthenticated()") ensures user is logged in
 * 
 * BASE URL: /api/user
 * REQUIRED ROLE: Any authenticated user (ADMIN, NORMAL_USER, PREMIUM_USER, GOVT_OFFICIAL)
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    /**
     * Get Current User Profile
     * 
     * FLOW:
     * 1. User sends authenticated request with JWT token
     * 2. JwtAuthenticationFilter validates token
     * 3. If valid, Spring Security sets user principal in context
     * 4. This method returns current user's profile
     * 5. Authentication object contains current logged-in user
     * 
     * HTTP METHOD: GET
     * ENDPOINT: /api/user/profile
     * AUTHORIZATION: Bearer <jwt_token>
     * REQUIRED ROLE: Any authenticated user
     * RESPONSE: Current user's profile data
     * STATUS: 200 OK
     * 
     * @param authentication Spring Security authentication object containing logged-in user
     * @return ResponseEntity with user profile
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")  // Only authenticated users can access
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        log.info(">>> [USER] /api/user/profile endpoint called");
        log.debug(">>> [USER] Authenticated user accessing profile - Username: {}", authentication.getName());

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("username", authentication.getName());
        userProfile.put("roles", authentication.getAuthorities());
        userProfile.put("message", "Your profile information");
        userProfile.put("profileStatus", "Active");

        log.info(">>> [USER] Profile returned for user: {}", authentication.getName());
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Get User Dashboard
     * 
     * FLOW:
     * 1. Any authenticated user can view their dashboard
     * 2. Returns user-specific data and statistics
     * 3. Different roles might see different information
     * 
     * HTTP METHOD: GET
     * ENDPOINT: /api/user/dashboard
     * AUTHORIZATION: Bearer <jwt_token>
     * REQUIRED ROLE: Any authenticated user
     * RESPONSE: User dashboard data
     * STATUS: 200 OK
     * 
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with dashboard data
     */
    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")  // Only authenticated users can access
    public ResponseEntity<?> getUserDashboard(Authentication authentication) {
        log.info(">>> [USER] /api/user/dashboard endpoint called");
        log.debug(">>> [USER] User accessing dashboard - Username: {}, Role: {}", 
                 authentication.getName(), authentication.getAuthorities());

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("username", authentication.getName());
        dashboard.put("lastLogin", "2026-03-28 10:30:00");
        dashboard.put("totalActions", 50);
        dashboard.put("activeStatus", "Online");
        dashboard.put("message", "Welcome to your dashboard");

        log.info(">>> [USER] Dashboard returned for user: {}", authentication.getName());
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get User Settings
     * 
     * FLOW:
     * 1. User can access and modify their own settings
     * 2. Only their own settings are accessible
     * 
     * HTTP METHOD: GET
     * ENDPOINT: /api/user/settings
     * AUTHORIZATION: Bearer <jwt_token>
     * REQUIRED ROLE: Any authenticated user
     * RESPONSE: User's personal settings
     * STATUS: 200 OK
     * 
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with user settings
     */
    @GetMapping("/settings")
    @PreAuthorize("isAuthenticated()")  // Only authenticated users can access
    public ResponseEntity<?> getUserSettings(Authentication authentication) {
        log.info(">>> [USER] /api/user/settings endpoint called");
        log.debug(">>> [USER] User accessing settings - Username: {}", authentication.getName());

        Map<String, Object> settings = new HashMap<>();
        settings.put("username", authentication.getName());
        settings.put("emailNotifications", true);
        settings.put("twoFactorAuth", false);
        settings.put("language", "English");
        settings.put("theme", "Light");

        log.info(">>> [USER] Settings returned for user: {}", authentication.getName());
        return ResponseEntity.ok(settings);
    }

    /**
     * Get User Activity Log
     * 
     * FLOW:
     * 1. Authenticated users can view their activity
     * 2. Tracks user actions for security and analytics
     * 
     * HTTP METHOD: GET
     * ENDPOINT: /api/user/activity
     * AUTHORIZATION: Bearer <jwt_token>
     * REQUIRED ROLE: Any authenticated user
     * RESPONSE: User's activity log
     * STATUS: 200 OK
     * 
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with activity log
     */
    @GetMapping("/activity")
    @PreAuthorize("isAuthenticated()")  // Only authenticated users can access
    public ResponseEntity<?> getUserActivity(Authentication authentication) {
        log.info(">>> [USER] /api/user/activity endpoint called");
        log.debug(">>> [USER] User accessing activity log - Username: {}", authentication.getName());

        Map<String, Object> activity = new HashMap<>();
        activity.put("username", authentication.getName());
        activity.put("recentLogins", 15);
        activity.put("lastLogin", "Today at 10:30 AM");
        activity.put("actions", "Viewed profile, Updated settings, Downloaded report");

        log.info(">>> [USER] Activity log returned for user: {}", authentication.getName());
        return ResponseEntity.ok(activity);
    }
}

