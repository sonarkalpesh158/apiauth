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
 * PremiumUserController - Premium user only endpoints
 * 
 * FLOW:
 * 1. These endpoints are only accessible by users with PREMIUM_USER role
 * 2. Provides premium features and enhanced functionality
 * 3. @PreAuthorize annotation enforces premium role requirement
 * 
 * BASE URL: /api/premium
 * REQUIRED ROLE: PREMIUM_USER
 */
@Slf4j
@RestController
@RequestMapping("/api/premium")
public class PremiumUserController {

    /**
     * Get Premium Features
     * 
     * FLOW:
     * 1. Only PREMIUM_USER can access premium features
     * 2. Returns list of available premium services
     * 3. Non-premium users get 403 FORBIDDEN
     * 
     * HTTP METHOD: GET
     * ENDPOINT: /api/premium/features
     * AUTHORIZATION: Bearer <jwt_token>
     * REQUIRED ROLE: PREMIUM_USER
     * RESPONSE: Premium features list
     * STATUS: 200 OK on success, 403 FORBIDDEN if not premium
     * 
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with premium features
     */
    @GetMapping("/features")
    @PreAuthorize("hasRole('PREMIUM_USER')")  // Only PREMIUM_USER can access
    public ResponseEntity<?> getPremiumFeatures(Authentication authentication) {
        log.info(">>> [PREMIUM] /api/premium/features endpoint called");
        log.debug(">>> [PREMIUM] Premium user accessing features - Username: {}", authentication.getName());

        Map<String, Object> premiumFeatures = new HashMap<>();
        premiumFeatures.put("username", authentication.getName());
        premiumFeatures.put("advancedAnalytics", true);
        premiumFeatures.put("prioritySupport", true);
        premiumFeatures.put("customReports", true);
        premiumFeatures.put("apiAccess", true);
        premiumFeatures.put("storageLimit", "1TB");
        premiumFeatures.put("message", "Welcome to Premium Features");

        log.info(">>> [PREMIUM] Premium features returned for user: {}", authentication.getName());
        return ResponseEntity.ok(premiumFeatures);
    }

    /**
     * Get Premium Analytics
     * 
     * FLOW:
     * 1. Premium users can access advanced analytics
     * 2. Provides detailed reports and insights
     * 
     * HTTP METHOD: GET
     * ENDPOINT: /api/premium/analytics
     * AUTHORIZATION: Bearer <jwt_token>
     * REQUIRED ROLE: PREMIUM_USER
     * RESPONSE: Advanced analytics data
     * STATUS: 200 OK on success, 403 FORBIDDEN if not premium
     * 
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with analytics data
     */
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('PREMIUM_USER')")  // Only PREMIUM_USER can access
    public ResponseEntity<?> getAdvancedAnalytics(Authentication authentication) {
        log.info(">>> [PREMIUM] /api/premium/analytics endpoint called");
        log.debug(">>> [PREMIUM] Premium user accessing analytics - Username: {}", authentication.getName());

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("username", authentication.getName());
        analytics.put("totalViews", 5000);
        analytics.put("conversionRate", 12.5);
        analytics.put("avgSessionTime", "5m 30s");
        analytics.put("topCountries", new String[]{"USA", "UK", "Canada"});
        analytics.put("reportGenerated", "2026-03-28");

        log.info(">>> [PREMIUM] Advanced analytics returned for premium user: {}", authentication.getName());
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get Priority Support Contact
     * 
     * FLOW:
     * 1. Premium users have access to priority support
     * 2. Get dedicated support channels and contact info
     * 
     * HTTP METHOD: GET
     * ENDPOINT: /api/premium/support
     * AUTHORIZATION: Bearer <jwt_token>
     * REQUIRED ROLE: PREMIUM_USER
     * RESPONSE: Priority support information
     * STATUS: 200 OK on success, 403 FORBIDDEN if not premium
     * 
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with support information
     */
    @GetMapping("/support")
    @PreAuthorize("hasRole('PREMIUM_USER')")  // Only PREMIUM_USER can access
    public ResponseEntity<?> getPrioritySupport(Authentication authentication) {
        log.info(">>> [PREMIUM] /api/premium/support endpoint called");
        log.debug(">>> [PREMIUM] Premium user accessing priority support - Username: {}", authentication.getName());

        Map<String, Object> support = new HashMap<>();
        support.put("username", authentication.getName());
        support.put("supportLevel", "Priority");
        support.put("responseTime", "30 minutes");
        support.put("dedicatedAgent", "agent_001");
        support.put("email", "premium-support@api.com");
        support.put("phone", "+1-800-PREMIUM-1");

        log.info(">>> [PREMIUM] Priority support information returned for premium user: {}", authentication.getName());
        return ResponseEntity.ok(support);
    }

    /**
     * Get Premium Resources
     * 
     * FLOW:
     * 1. Access premium resources and templates
     * 2. Download exclusive content
     * 
     * HTTP METHOD: GET
     * ENDPOINT: /api/premium/resources
     * AUTHORIZATION: Bearer <jwt_token>
     * REQUIRED ROLE: PREMIUM_USER
     * RESPONSE: Available premium resources
     * STATUS: 200 OK on success, 403 FORBIDDEN if not premium
     * 
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with resources list
     */
    @GetMapping("/resources")
    @PreAuthorize("hasRole('PREMIUM_USER')")  // Only PREMIUM_USER can access
    public ResponseEntity<?> getPremiumResources(Authentication authentication) {
        log.info(">>> [PREMIUM] /api/premium/resources endpoint called");
        log.debug(">>> [PREMIUM] Premium user accessing resources - Username: {}", authentication.getName());

        Map<String, Object> resources = new HashMap<>();
        resources.put("username", authentication.getName());
        resources.put("availableTemplates", 50);
        resources.put("eBooks", 30);
        resources.put("videoTutorials", 100);
        resources.put("whitepapers", 15);
        resources.put("downloadLimit", "Unlimited");

        log.info(">>> [PREMIUM] Premium resources returned for user: {}", authentication.getName());
        return ResponseEntity.ok(resources);
    }
}

