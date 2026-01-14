package com.portfolio.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User response DTO (without sensitive information)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private String id;
    private String userId;
    private String email;
    private String fullName;
    private String status;
    private String role; // User role (USER, ADMIN)
    private Boolean emailVerified;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
