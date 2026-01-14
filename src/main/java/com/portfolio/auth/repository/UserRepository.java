package com.portfolio.auth.repository;

import com.portfolio.auth.model.User;
import com.portfolio.auth.model.UserStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User repository
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by userId
     */
    Optional<User> findByUserId(String userId);
    
    /**
     * Find user by email and status
     */
    Optional<User> findByEmailAndStatus(String email, UserStatus status);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if userId exists
     */
    boolean existsByUserId(String userId);
}
