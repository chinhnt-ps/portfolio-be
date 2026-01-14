package com.portfolio.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String userId;
    private String email;
    private String fullName;
    private String status;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn; // in seconds
    private UserResponse user;
}
