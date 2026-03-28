package com.api.auth.security;

import com.api.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * UserPrincipal - Spring Security UserDetails implementation
 * 
 * FLOW:
 * 1. User entity is converted to UserPrincipal
 * 2. Contains user information and authorities (roles)
 * 3. Used by Spring Security for authentication and authorization
 * 4. Stored in Security Context after login
 */
@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String role;
    private Boolean enabled;
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Factory method to create UserPrincipal from User entity
     * 
     * @param user User entity from database
     * @return UserPrincipal object
     */
    public static UserPrincipal create(User user) {
        // Create authorities list with user's role
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add the user's role as ROLE_ prefixed authority
        authorities.add(new SimpleGrantedAuthority(user.getRole().getRoleName()));

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().getRoleName(),
                user.getEnabled(),
                authorities
        );
    }

    /**
     * Returns the authorities granted to the user
     * Used by Spring Security for authorization
     * 
     * @return Collection of authorities (roles)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the password used to authenticate the user
     * 
     * @return User password (encrypted)
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user
     * 
     * @return User username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user's account has expired
     * 
     * @return True if account is not expired
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked
     * 
     * @return True if user is not locked
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) has expired
     * 
     * @return True if credentials are not expired
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled
     * 
     * @return User's enabled status
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

