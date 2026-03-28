package com.api.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * JwtAuthenticationFilter - JWT Token Authentication Filter
 * 
 * FLOW:
 * 1. For each incoming request, extract JWT token from header
 * 2. Validate the token using JwtTokenProvider
 * 3. If valid, extract username and role
 * 4. Create Authentication object and set in SecurityContext
 * 5. Request proceeds with authenticated user
 * 6. If invalid, request proceeds without authentication (401 Unauthorized on endpoint)
 * 
 * This filter runs BEFORE other security filters
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailServiceImpl userDetailsService;

    /**
     * Main filter method - Called for each request
     * 
     * FLOW:
     * 1. Extract JWT token from request header
     * 2. Validate token
     * 3. Get username from token
     * 4. Load user from database
     * 5. Create authentication token with user and authorities
     * 6. Set authentication in security context
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter chain to continue
     * @throws ServletException If error during filtering
     * @throws IOException If IO error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Step 1: Extract JWT token from Authorization header
            String jwt = extractJwtFromRequest(request);
            
            log.debug(">>> [FILTER] Processing request - Token present: {}", StringUtils.hasText(jwt));

            // Step 2: Validate token if present
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                log.info(">>> [FILTER] JWT token validation successful");

                // Step 3: Get username from token
                String username = tokenProvider.getUsernameFromToken(jwt);
                log.debug(">>> [FILTER] Username extracted from token: {}", username);

                // Step 4: Load user details from database
                UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(username);
                log.debug(">>> [FILTER] User loaded from database: {}", username);

                // Step 5: Create authentication token with user details and authorities
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal, 
                                null, 
                                userPrincipal.getAuthorities()
                        );
                
                // Add request details for traceability
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 6: Set authentication in security context
                // This makes the user "logged in" for this request
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.info(">>> [FILTER] Authentication set in SecurityContext for user: {} with role: {}", 
                         username, userPrincipal.getRole());
            } else {
                log.debug(">>> [FILTER] JWT token is missing or invalid");
            }
        } catch (Exception ex) {
            log.error(">>> [FILTER] Could not set user authentication: {}", ex.getMessage());
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     * 
     * FLOW:
     * 1. Get Authorization header from request
     * 2. Check if it starts with "Bearer "
     * 3. Extract and return the token part
     * 4. Return null if header is missing or invalid
     * 
     * @param request HTTP request
     * @return JWT token string or null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        log.debug(">>> [FILTER] Extracting JWT from request headers");
        
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);  // Remove "Bearer " prefix
            log.debug(">>> [FILTER] JWT token extracted successfully");
            return token;
        }
        
        log.debug(">>> [FILTER] No valid Authorization header found");
        return null;
    }
}

