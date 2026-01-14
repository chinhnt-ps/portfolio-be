package com.portfolio.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Refresh token entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refresh_tokens")
public class RefreshToken {
    
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    @Indexed(unique = true)
    private String token;
    
    @Indexed
    private LocalDateTime expiresAt;
    
    @Builder.Default
    private Boolean revoked = false;
    
    private LocalDateTime revokedAt;
    
    private LocalDateTime createdAt;
}
