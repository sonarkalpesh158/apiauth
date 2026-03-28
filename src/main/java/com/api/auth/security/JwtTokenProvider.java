package com.api.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtTokenProvider - JWT Token Management Service
 * 
 * FLOW:
 * 1. Generates JWT tokens on successful authentication
 * 2. Validates tokens on each request
 * 3. Extracts user information from tokens
 * 4. Handles token expiration and refresh
 * 
 * JWT Structure: Header.Payload.Signature
 * - Header: Token type and algorithm
 * - Payload: Claims (username, role, expiration)
 * - Signature: Ensures token hasn't been tampered with
 */
@Slf4j
@Service
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpirationMs;

    /**
     * Generate JWT Access Token
     * 
     * FLOW:
     * 1. Get username from authentication
     * 2. Create JWT token with username and role in claims
     * 3. Set expiration time
     * 4. Sign token with secret key
     * 5. Return token string
     * 
     * @param authentication Spring Security Authentication object
     * @return JWT Access Token as String
     */
    public String generateToken(Authentication authentication) {
        log.info(">>> [JWT] Generating access token for user: {}", authentication.getName());
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
                .subject(authentication.getName())  // Set username as subject
                .claim("role", userPrincipal.getRole())  // Add user role
                .claim("id", userPrincipal.getId())  // Add user id
                .issuedAt(now)  // Set issue time
                .expiration(expiryDate)  // Set expiration time
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))  // Sign with secret
                .compact();  // Build compact string

        log.debug(">>> [JWT] Token generated successfully. Expires at: {}", expiryDate);
        return token;
    }

    /**
     * Generate Refresh Token
     * 
     * FLOW:
     * 1. Similar to access token but with longer expiration
     * 2. Used to get new access token without re-authentication
     * 
     * @param username The username for which to generate refresh token
     * @return Refresh Token as String
     */
    public String generateRefreshToken(String username) {
        log.info(">>> [JWT] Generating refresh token for user: {}", username);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        String token = Jwts.builder()
                .subject(username)
                .claim("type", "REFRESH")  // Mark this as refresh token
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        log.debug(">>> [JWT] Refresh token generated successfully");
        return token;
    }

    /**
     * Get username from JWT token
     * 
     * FLOW:
     * 1. Parse and validate token
     * 2. Extract subject (username)
     * 3. Return username
     * 
     * @param token JWT Token
     * @return Username extracted from token
     */
    public String getUsernameFromToken(String token) {
        log.debug(">>> [JWT] Extracting username from token");
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        log.debug(">>> [JWT] Username extracted: {}", username);
        return username;
    }

    /**
     * Get user role from JWT token
     * 
     * @param token JWT Token
     * @return User role
     */
    public String getRoleFromToken(String token) {
        log.debug(">>> [JWT] Extracting role from token");
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        String role = claims.get("role", String.class);
        log.debug(">>> [JWT] Role extracted: {}", role);
        return role;
    }

    /**
     * Validate JWT token
     * 
     * FLOW:
     * 1. Parse token with secret key
     * 2. Check signature validity
     * 3. Check expiration
     * 4. Return validation status
     * 
     * @param token JWT Token to validate
     * @return True if token is valid, False otherwise
     */
    public Boolean validateToken(String token) {
        log.debug(">>> [JWT] Validating token");
        
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token);

            log.info(">>> [JWT] Token is valid");
            return true;
        } catch (SecurityException ex) {
            log.error(">>> [JWT] Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error(">>> [JWT] Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error(">>> [JWT] Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error(">>> [JWT] Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error(">>> [JWT] JWT claims string is empty: {}", ex.getMessage());
        }

        log.warn(">>> [JWT] Token validation failed");
        return false;
    }

    /**
     * Extract user ID from token
     * 
     * @param token JWT Token
     * @return User ID
     */
    public Long getUserIdFromToken(String token) {
        log.debug(">>> [JWT] Extracting user ID from token");
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long userId = claims.get("id", Long.class);
        log.debug(">>> [JWT] User ID extracted: {}", userId);
        return userId;
    }
}

