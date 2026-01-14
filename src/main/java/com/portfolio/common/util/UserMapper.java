package com.portfolio.common.util;

import com.portfolio.auth.dto.response.UserResponse;
import com.portfolio.auth.model.User;

/**
 * Utility class for mapping User entity to UserResponse DTO
 */
public class UserMapper {
    
    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus().name())
                .role(user.getRole().name())
                .emailVerified(user.getEmailVerified())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
