package com.portfolio.auth.repository;

import com.portfolio.auth.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Refresh token repository
 */
@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    
    /**
     * Find refresh token by token string
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * Find all refresh tokens by userId
     */
    List<RefreshToken> findByUserId(String userId);
    
    /**
     * Find all non-revoked refresh tokens by userId
     */
    List<RefreshToken> findByUserIdAndRevokedFalse(String userId);
    
    /**
     * Delete all refresh tokens by userId
     */
    void deleteByUserId(String userId);
}
