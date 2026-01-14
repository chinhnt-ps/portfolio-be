package com.portfolio.auth.repository;

import com.portfolio.auth.model.VerificationCode;
import com.portfolio.auth.model.VerificationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Verification code repository
 */
@Repository
public interface VerificationCodeRepository extends MongoRepository<VerificationCode, String> {
    
    /**
     * Find verification code by code, email, type and used status
     */
    Optional<VerificationCode> findByCodeAndEmailAndTypeAndUsed(
            String code, 
            String email, 
            VerificationType type, 
            Boolean used
    );
    
    /**
     * Find latest verification code by email and type
     */
    Optional<VerificationCode> findFirstByEmailAndTypeOrderByCreatedAtDesc(
            String email, 
            VerificationType type
    );
}
