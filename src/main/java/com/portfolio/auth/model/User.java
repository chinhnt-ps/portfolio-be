package com.portfolio.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * User entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String userId; // UUID
    
    @Indexed(unique = true)
    private String email;
    
    private String password; // BCrypt hash
    
    private String fullName;
    
    @Indexed
    @Builder.Default
    private UserStatus status = UserStatus.INACTIVE;
    
    @Indexed
    @Builder.Default
    private Role role = Role.USER; // Default role is USER
    
    @Builder.Default
    private Boolean emailVerified = false;
    
    private LocalDateTime emailVerifiedAt;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastLoginAt;
}
