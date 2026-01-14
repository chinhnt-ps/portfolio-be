package com.portfolio.auth.service;

import com.portfolio.auth.model.Role;
import com.portfolio.auth.model.User;
import com.portfolio.auth.model.UserStatus;
import com.portfolio.auth.repository.UserRepository;
import com.portfolio.common.exception.BusinessException;
import com.portfolio.common.exception.ConflictException;
import com.portfolio.common.exception.NotFoundException;
import com.portfolio.common.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User service for user management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * Create new user
     * 
     * @param email User email
     * @param password Plain password (will be hashed)
     * @param fullName User full name (optional)
     * @return Created user
     */
    @Transactional
    public User createUser(String email, String password, String fullName) {
        // Check email uniqueness
        if (userRepository.existsByEmail(email.toLowerCase())) {
            throw new ConflictException("Email already exists");
        }
        
        // Validate password
        PasswordUtil.validatePassword(password);
        
        // Hash password
        String hashedPassword = PasswordUtil.hashPassword(password);
        
        // Generate userId
        String userId = UUID.randomUUID().toString();
        
        // Create user with default role USER
        User user = User.builder()
                .userId(userId)
                .email(email.toLowerCase())
                .password(hashedPassword)
                .fullName(fullName)
                .status(UserStatus.INACTIVE)
                .role(Role.USER) // Default role is USER
                .emailVerified(false)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("Created user with userId: {}", userId);
        
        return savedUser;
    }
    
    /**
     * Find user by email
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new NotFoundException("User", email));
    }
    
    /**
     * Find user by userId
     */
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
    }
    
    /**
     * Activate user (set status to ACTIVE)
     */
    @Transactional
    public User activateUser(String userId) {
        User user = findByUserId(userId);
        
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BusinessException("User is already active");
        }
        
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Update last login timestamp
     */
    @Transactional
    public void updateLastLogin(String userId) {
        User user = findByUserId(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    /**
     * Update user password
     */
    @Transactional
    public void updatePassword(String userId, String newPassword) {
        // Validate password
        PasswordUtil.validatePassword(newPassword);
        
        User user = findByUserId(userId);
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        user.setPassword(hashedPassword);
        
        userRepository.save(user);
        log.info("Updated password for user: {}", userId);
    }
    
    /**
     * Update user role (only ADMIN can do this)
     * 
     * @param userId User ID to update
     * @param newRole New role to assign
     * @return Updated user
     */
    @Transactional
    public User updateRole(String userId, Role newRole) {
        User user = findByUserId(userId);
        user.setRole(newRole);
        
        User updatedUser = userRepository.save(user);
        log.info("Updated role for user {} to {}", userId, newRole);
        
        return updatedUser;
    }
}
