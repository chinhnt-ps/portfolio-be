package com.portfolio.auth.service;

import com.portfolio.auth.model.VerificationCode;
import com.portfolio.auth.model.VerificationType;
import com.portfolio.auth.repository.VerificationCodeRepository;
import com.portfolio.common.exception.BusinessException;
import com.portfolio.common.exception.ValidationException;
import com.portfolio.common.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Verification code service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {
    
    private static final int CODE_EXPIRATION_MINUTES = 15;
    
    private final VerificationCodeRepository verificationCodeRepository;
    
    /**
     * Generate verification code và save to database
     * 
     * @param userId User ID
     * @param email User email
     * @param type Verification type
     * @return Generated code
     */
    @Transactional
    public String generateCode(String userId, String email, VerificationType type) {
        // Generate 6-digit code
        String code = CodeGenerator.generateVerificationCode();
        
        // Invalidate old codes for same email and type
        invalidateOldCodes(email, type);
        
        // Create verification code với expiration 15 phút
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES);
        
        VerificationCode verificationCode = VerificationCode.builder()
                .userId(userId)
                .email(email.toLowerCase())
                .code(code)
                .type(type)
                .expiresAt(expiresAt)
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        verificationCodeRepository.save(verificationCode);
        log.info("Generated verification code for user: {}, type: {}, expires at: {}", userId, type, expiresAt);
        
        return code;
    }
    
    /**
     * Validate verification code
     * 
     * @param code Verification code
     * @param email User email
     * @param type Verification type
     * @return VerificationCode if valid
     * @throws ValidationException if code is invalid or expired
     */
    @Transactional
    public VerificationCode validateCode(String code, String email, VerificationType type) {
        Optional<VerificationCode> verificationCodeOpt = verificationCodeRepository
                .findByCodeAndEmailAndTypeAndUsed(code, email.toLowerCase(), type, false);
        
        if (verificationCodeOpt.isEmpty()) {
            throw new ValidationException("Invalid verification code");
        }
        
        VerificationCode verificationCode = verificationCodeOpt.get();
        
        // Check expiration
        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Verification code has expired");
        }
        
        return verificationCode;
    }
    
    /**
     * Mark verification code as used
     */
    @Transactional
    public void markAsUsed(String codeId) {
        VerificationCode verificationCode = verificationCodeRepository.findById(codeId)
                .orElseThrow(() -> new BusinessException("Verification code not found"));
        
        verificationCode.setUsed(true);
        verificationCode.setUsedAt(LocalDateTime.now());
        verificationCodeRepository.save(verificationCode);
    }
    
    /**
     * Invalidate old codes for same email and type
     */
    private void invalidateOldCodes(String email, VerificationType type) {
        Optional<VerificationCode> oldCodeOpt = verificationCodeRepository
                .findFirstByEmailAndTypeOrderByCreatedAtDesc(email.toLowerCase(), type);
        
        if (oldCodeOpt.isPresent() && !oldCodeOpt.get().getUsed()) {
            VerificationCode oldCode = oldCodeOpt.get();
            oldCode.setUsed(true);
            oldCode.setUsedAt(LocalDateTime.now());
            verificationCodeRepository.save(oldCode);
        }
    }
}
