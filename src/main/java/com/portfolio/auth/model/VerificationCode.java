package com.portfolio.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Verification code entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "verification_codes")
@CompoundIndex(name = "code_email_type_used_idx", def = "{'code': 1, 'email': 1, 'type': 1, 'used': 1}")
public class VerificationCode {
    
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    @Indexed
    private String email;
    
    private String code; // 6 digits
    
    private VerificationType type; // REGISTRATION, PASSWORD_RESET
    
    @Indexed
    private LocalDateTime expiresAt;
    
    @Builder.Default
    private Boolean used = false;
    
    private LocalDateTime usedAt;
    
    private LocalDateTime createdAt;
}
