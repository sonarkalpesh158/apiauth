package com.api.auth.security;

import com.api.auth.entity.User;
import com.api.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailServiceImpl - Spring Security UserDetailsService implementation
 * 
 * FLOW:
 * 1. Spring Security calls loadUserByUsername when authentication needed
 * 2. This service fetches user from database
 * 3. Converts User entity to UserDetails (UserPrincipal)
 * 4. Returns UserDetails to Spring Security for authentication
 * 
 * This is called:
 * - During login process
 * - When JWT filter needs to load user details
 */
@Slf4j
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user by username from database
     * 
     * FLOW:
     * 1. Search for user with given username
     * 2. If found, convert to UserDetails
     * 3. If not found, throw UsernameNotFoundException
     * 4. Spring Security will handle the exception and deny access
     * 
     * @param username The username to look up
     * @return UserDetails object containing user information
     * @throws UsernameNotFoundException If user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info(">>> [USER_SERVICE] Loading user details for username: {}", username);

        // Search for user in database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error(">>> [USER_SERVICE] User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        log.debug(">>> [USER_SERVICE] User found - ID: {}, Email: {}, Role: {}", 
                 user.getId(), user.getEmail(), user.getRole().getRoleName());

        // Convert User entity to UserPrincipal
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        log.info(">>> [USER_SERVICE] UserPrincipal created successfully for: {}", username);
        return userPrincipal;
    }
}

