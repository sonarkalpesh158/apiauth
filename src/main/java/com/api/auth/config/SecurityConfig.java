package com.api.auth.config;

import com.api.auth.security.JwtAuthenticationFilter;
import com.api.auth.security.UserDetailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig - Spring Security Configuration
 * 
 * FLOW:
 * 1. Configures password encoder (BCrypt)
 * 2. Sets up authentication provider with UserDetailsService
 * 3. Creates authentication manager
 * 4. Configures HTTP security
 * 5. Registers JWT filter in filter chain
 * 6. Sets up authorization rules
 * 
 * AUTHORIZATION RULES:
 * - Public endpoints: /api/auth/login, /api/auth/register, /h2-console/**
 * - Admin only: /api/admin/**
 * - Authenticated users: /api/user/**
 * - Role-based: /api/premium/** (PREMIUM_USER), /api/govt/** (GOVT_OFFICIAL)
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // Enables @PreAuthorize and @PostAuthorize annotations
public class SecurityConfig {

    @Autowired
    private UserDetailServiceImpl userDetailsService;

    /**
     * Password Encoder Bean - BCrypt
     * 
     * FLOW:
     * 1. Used to encode passwords before storing in database
     * 2. Used to verify password during login
     * 3. Passwords are one-way hashed (cannot be decrypted)
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info(">>> [CONFIG] Configuring BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Provider Bean
     * 
     * FLOW:
     * 1. Uses UserDetailsService to load user details
     * 2. Uses PasswordEncoder to verify password
     * 3. Returns authentication provider for Spring Security
     * 
     * @return DaoAuthenticationProvider instance
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        log.info(">>> [CONFIG] Configuring DaoAuthenticationProvider");
        
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        
        log.debug(">>> [CONFIG] DaoAuthenticationProvider configured");
        return authProvider;
    }

    /**
     * Authentication Manager Bean
     * 
     * FLOW:
     * 1. Used to authenticate user credentials (username/password)
     * 2. Called during login process
     * 3. Returns authentication object if credentials are valid
     * 
     * @param authConfig AuthenticationConfiguration
     * @return AuthenticationManager instance
     * @throws Exception If configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        log.info(">>> [CONFIG] Configuring AuthenticationManager");
        return authConfig.getAuthenticationManager();
    }

    /**
     * JWT Authentication Filter Bean
     * 
     * FLOW:
     * 1. This filter intercepts every request
     * 2. Extracts JWT token from Authorization header
     * 3. Validates token and sets authentication in SecurityContext
     * 4. Runs before UsernamePasswordAuthenticationFilter
     * 
     * @return JwtAuthenticationFilter instance
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        log.info(">>> [CONFIG] Creating JwtAuthenticationFilter bean");
        return new JwtAuthenticationFilter();
    }

    /**
     * HTTP Security Configuration
     * 
     * FLOW:
     * 1. Disable CSRF (not needed for stateless JWT authentication)
     * 2. Set session creation policy to STATELESS (no sessions)
     * 3. Configure endpoint authorization
     * 4. Add custom JWT authentication filter
     * 5. Configure exception handling for unauthorized access
     * 
     * ENDPOINT SECURITY:
     * - POST /api/auth/login - PUBLIC (allows anonymous)
     * - POST /api/auth/register - PUBLIC (allows anonymous)
     * - GET /h2-console/** - PUBLIC (allows anonymous, for development)
     * - GET /api/admin/** - ADMIN role required
     * - GET /api/premium/** - PREMIUM_USER role required
     * - GET /api/govt/** - GOVT_OFFICIAL role required
     * - GET /api/user/** - Authenticated users (any role)
     * - All other requests - Authenticated
     * 
     * @param http HttpSecurity configuration object
     * @return SecurityFilterChain instance
     * @throws Exception If configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info(">>> [CONFIG] Configuring HTTP Security Filter Chain");

        http
                // Step 1: Disable CSRF protection (stateless JWT doesn't need it)
                .csrf(csrf -> csrf.disable())
                
                // Step 2: Set exception handling for unauthorized access
                .exceptionHandling(exception -> {
                    exception.authenticationEntryPoint((request, response, authException) -> {
                        log.warn(">>> [CONFIG] Unauthorized access attempt: {}", authException.getMessage());
                        response.setContentType("application/json");
                        response.setStatus(401);
                        response.getWriter().write("{\"message\": \"Unauthorized: " + authException.getMessage() + "\"}");
                    });
                    
                    exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                        log.warn(">>> [CONFIG] Access denied: {}", accessDeniedException.getMessage());
                        response.setContentType("application/json");
                        response.setStatus(403);
                        response.getWriter().write("{\"message\": \"Forbidden: " + accessDeniedException.getMessage() + "\"}");
                    });
                })
                
                // Step 3: Set session creation policy to STATELESS
                // No session will be created or maintained by Spring Security
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Step 4: Configure authorization for endpoints
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh-token").permitAll()
                        
                        // H2 Console for development (remove in production)
                        .requestMatchers("/h2-console/**").permitAll()
                        
                        // Admin endpoints - only ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // Premium user endpoints - only PREMIUM_USER role
                        .requestMatchers("/api/premium/**").hasRole("PREMIUM_USER")
                        
                        // Government official endpoints - only GOVT_OFFICIAL role
                        .requestMatchers("/api/govt/**").hasRole("GOVT_OFFICIAL")
                        
                        // User endpoints - any authenticated user
                        .requestMatchers("/api/user/**").authenticated()
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                
                // Step 5: Add JWT authentication filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        log.info(">>> [CONFIG] HTTP Security Filter Chain configuration complete");
        return http.build();
    }
}

