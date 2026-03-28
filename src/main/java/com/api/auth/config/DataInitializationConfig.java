package com.api.auth.config;

import com.api.auth.entity.User;
import com.api.auth.entity.enums.UserRole;
import com.api.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

/**
 * DataInitializationConfig - Initialize demo data on application startup
 * 
 * FLOW:
 * 1. On application startup, this component runs
 * 2. Checks if demo users already exist
 * 3. If not, creates sample users with different roles
 * 4. Useful for testing and development
 * 
 * This creates 4 demo users:
 * 1. Admin User
 * 2. Normal User
 * 3. Premium User
 * 4. Government Official
 */
@Slf4j
@Configuration
public class DataInitializationConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Initialize demo data
     * 
     * FLOW:
     * 1. CommandLineRunner is executed after Spring context initialization
     * 2. Check if users already exist
     * 3. If database is empty, create sample users
     * 4. Each user has different role for testing authorization
     * 
     * @return CommandLineRunner bean
     */
    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            log.info(">>> [INIT] Checking if demo data needs to be initialized...");

            // Check if users already exist
            if (userRepository.count() > 0) {
                log.info(">>> [INIT] Database already contains users, skipping initialization");
                return;
            }

            log.info(">>> [INIT] Database is empty, creating demo users...");

            // Create Admin User
            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin@123"))  // BCrypt encrypted
                    .fullName("Administrator")
                    .phoneNumber("9999999999")
                    .role(UserRole.ADMIN)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Create Normal User
            User normalUser = User.builder()
                    .username("user")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("User@123"))
                    .fullName("John Doe")
                    .phoneNumber("1234567890")
                    .role(UserRole.NORMAL_USER)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Create Premium User
            User premiumUser = User.builder()
                    .username("premium")
                    .email("premium@example.com")
                    .password(passwordEncoder.encode("Premium@123"))
                    .fullName("Jane Smith")
                    .phoneNumber("8765432109")
                    .role(UserRole.PREMIUM_USER)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Create Government Official
            User govtOfficial = User.builder()
                    .username("govt_official")
                    .email("official@example.com")
                    .password(passwordEncoder.encode("Govt@123"))
                    .fullName("Robert Johnson")
                    .phoneNumber("5555555555")
                    .role(UserRole.GOVT_OFFICIAL)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Save all users to database
            userRepository.save(adminUser);
            log.info(">>> [INIT] Admin user created - Username: admin, Password: Admin@123");

            userRepository.save(normalUser);
            log.info(">>> [INIT] Normal user created - Username: user, Password: User@123");

            userRepository.save(premiumUser);
            log.info(">>> [INIT] Premium user created - Username: premium, Password: Premium@123");

            userRepository.save(govtOfficial);
            log.info(">>> [INIT] Government official created - Username: govt_official, Password: Govt@123");

            log.info(">>> [INIT] Demo data initialization completed!");
            log.info(">>> [INIT] You can now login with:");
            log.info(">>> [INIT]   Admin: username=admin, password=Admin@123");
            log.info(">>> [INIT]   User: username=user, password=User@123");
            log.info(">>> [INIT]   Premium: username=premium, password=Premium@123");
            log.info(">>> [INIT]   Govt: username=govt_official, password=Govt@123");
        };
    }
}

