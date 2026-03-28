package com.api.auth.repository;

import com.api.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * UserRepository - Data Access Object for User entity
 * 
 * FLOW:
 * 1. Provides database operations for User entity
 * 2. Allows finding users by username or email
 * 3. Supports custom queries for user lookup
 * 
 * CRUD Operations: Create, Read, Update, Delete
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     * Used during login process
     * 
     * @param username The username to search for
     * @return Optional containing user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     * Used during registration to check email uniqueness
     * 
     * @param email The email to search for
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username exists in database
     * 
     * @param username The username to check
     * @return True if username exists, False otherwise
     */
    Boolean existsByUsername(String username);
    
    /**
     * Check if email exists in database
     * 
     * @param email The email to check
     * @return True if email exists, False otherwise
     */
    Boolean existsByEmail(String email);
}

