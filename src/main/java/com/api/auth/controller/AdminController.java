package com.api.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.preauthorize.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * AdminController - Admin-only endpoints
 * 
 * FLOW:
 * 1. These endpoints are only accessible by users with ADMIN role
 * 2. @PreAuthorize annotation enforces role-based access control
 * 3. If non-admin user tries to access, 403 FORBIDDEN is returned
 * 
 * BASE URL: /api/admin
 * REQUIRED ROLE: ADMIN
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /**
     * Get Dashboard Data - Admin Only
     * 
     * FLOW:
     * 1. Request comes with JWT token
     * 2. JwtAuthenticationFilter validates token and extracts role
     * 3. @PreAuthorize checks if user has ROLE_ADMIN
     * 4. If yes, endpoint is executed
     * 5. If no, 403 FORBIDDEN is returned
     * 
     * HTTP METHOD: GET
     * ENDPOINT: /api/admin/dashboard
     * AUTHORIZATION: Bearer <jwt_token>
     * REQUIRED ROLE: ADMIN
     * RESPONSE: Dashboard data
     * STATUS: 200 OK on success, 403 FORBIDDEN if not admin
     * 
     * @return ResponseEntity with dashboard data
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can access
    public ResponseEntity<?> getDashboard() {
        log.info(">>> [ADMIN] /api/admin/dashboard endpoint called");
        log.debug(">>> [ADMIN] Authenticated admin user accessing dashboard");

        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("totalUsers", 150);
        dashboardData.put("totalRevenue", 50000);
        dashboardData.put("activeUsers", 120);
        dashboardData.put("adminMessage", "Welcome to Admin Dashboard");

        log.info(">>> [ADMIN] Dashboard data prepared and returning to admin");
        return ResponseEntity.ok(dashboardData);
    }

    /**
     * Get All Users - Admin Only
     * 
     * FLOW:
     * 1. Only ADMIN can view all users
     * 2. Returns list of all users in system
     * 3. Useful for user management
     * 
     * HTTP METHOD: GET
     * ENDPOINT: /api/admin/users
     * AUTHORIZATION: Bearer <jwt_token>
     * REQUIRED ROLE: ADMIN
     * RESPONSE: List of all users
     * STATUS: 200 OK on success, 403 FORBIDDEN if not admin
     * 
     * @return ResponseEntity with list of users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can access
    public ResponseEntity<?> getAllUsers() {
        log.info(">>> [ADMIN] /api/admin/users endpoint called");
        log.debug(">>> [ADMIN] Authenticated admin user fetching all users");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "All users list");
        response.put("count", 150);
        response.put("users", "User list would be populated from database");

        log.info(">>> [ADMIN] Returning all users to admin");
        return ResponseEntity.ok(response);
    }

    /**
     * Admin System Settings - Admin Only
     * 
     * FLOW:
     * 1. Only ADMIN can modify system settings
     * 2. Controls global application configuration
     * 
     * HTTP METHOD: GET
     * ENDPOINT: /api/admin/settings
     * AUTHORIZATION: Bearer <jwt_token>
     * REQUIRED ROLE: ADMIN
     * RESPONSE: System settings
     * STATUS: 200 OK on success, 403 FORBIDDEN if not admin
     * 
     * @return ResponseEntity with system settings
     */
    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can access
    public ResponseEntity<?> getSystemSettings() {
        log.info(">>> [ADMIN] /api/admin/settings endpoint called");
        log.debug(">>> [ADMIN] Authenticated admin user accessing system settings");

        Map<String, Object> settings = new HashMap<>();
        settings.put("maintenanceMode", false);
        settings.put("maxLoginAttempts", 5);
        settings.put("tokenExpiration", "24 hours");
        settings.put("apiVersion", "1.0.0");

        log.info(">>> [ADMIN] System settings returned to admin");
        return ResponseEntity.ok(settings);
    }
}

