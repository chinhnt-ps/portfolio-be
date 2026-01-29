package com.portfolio.auth.service;

import com.portfolio.auth.dto.request.*;
import com.portfolio.auth.dto.response.AuthResponse;
import com.portfolio.auth.dto.response.UserResponse;
import com.portfolio.auth.model.RefreshToken;
import com.portfolio.auth.model.User;
import com.portfolio.auth.model.UserStatus;
import com.portfolio.auth.model.VerificationCode;
import com.portfolio.auth.model.VerificationType;
import com.portfolio.auth.repository.RefreshTokenRepository;
import com.portfolio.common.exception.UnauthorizedException;
import com.portfolio.common.util.PasswordUtil;
import com.portfolio.email.service.EmailService;
import com.portfolio.security.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserService userService;
    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Value("${spring.security.jwt.refresh-expiration:7}")
    private long refreshTokenExpirationDays;
    
    /**
     * Register new user
     * 
     * @param request Register request
     * @return Auth response với user info (no tokens yet)
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Create user
        User user = userService.createUser(
                request.getEmail(),
                request.getPassword(),
                request.getFullName()
        );
        
        // Generate verification code
        String code = verificationCodeService.generateCode(
                user.getUserId(),
                user.getEmail(),
                com.portfolio.auth.model.VerificationType.REGISTRATION
        );
        
        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), code);
        
        // Build response (no tokens, user is INACTIVE)
        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus().name())
                .user(mapToUserResponse(user))
                .build();
    }
    
    /**
     * Verify email với verification code
     */
    @Transactional
    public AuthResponse verifyEmail(VerifyEmailRequest request) {
        // Validate verification code
        VerificationCode verificationCode = verificationCodeService.validateCode(
                request.getVerificationCode(),
                request.getEmail(),
                VerificationType.REGISTRATION
        );
        
        // Activate user
        User user = userService.activateUser(verificationCode.getUserId());
        
        // Mark code as used
        verificationCodeService.markAsUsed(verificationCode.getId());
        
        log.info("User {} verified email successfully", user.getUserId());
        
        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus().name())
                .user(mapToUserResponse(user))
                .build();
    }
    
    /**
     * Login với email và password
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Find user
        User user = userService.findByEmail(request.getEmail());
        
        // Check user status (must be ACTIVE)
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Tài khoản chưa được kích hoạt. Vui lòng xác nhận email.");
        }
        
        // Validate password
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Email hoặc mật khẩu không đúng");
        }
        
        // Generate tokens with role and fullName (for display after session restore)
        String accessToken = jwtTokenProvider.generateToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole().name(),
                user.getFullName()
        );
        String refreshToken = generateRefreshToken(user.getUserId());
        
        // Update last login
        userService.updateLastLogin(user.getUserId());
        
        log.info("User {} logged in successfully", user.getUserId());
        
        // Calculate expiresIn (seconds until expiration)
        long expiresIn = (jwtTokenProvider.getExpirationDateFromToken(accessToken).getTime() - System.currentTimeMillis()) / 1000;
        
        return AuthResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .user(mapToUserResponse(user))
                .build();
    }
    
    /**
     * Refresh access token
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        // Find refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        
        // Check if revoked
        if (refreshToken.getRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        
        // Check if expired
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token has expired");
        }
        
        // Get user
        User user = userService.findByUserId(refreshToken.getUserId());
        
        // Generate new access token with role and fullName
        String newAccessToken = jwtTokenProvider.generateToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole().name(),
                user.getFullName()
        );
        
        // Calculate expiresIn (seconds until expiration)
        long expiresIn = (jwtTokenProvider.getExpirationDateFromToken(newAccessToken).getTime() - System.currentTimeMillis()) / 1000;
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken()) // Keep same refresh token
                .expiresIn(expiresIn)
                .build();
    }
    
    /**
     * Forgot password - send verification code
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Check if email exists (don't reveal if it doesn't exist - security best practice)
        try {
            User user = userService.findByEmail(request.getEmail());
            
            // Generate verification code
            String code = verificationCodeService.generateCode(
                    user.getUserId(),
                    user.getEmail(),
                    VerificationType.PASSWORD_RESET
            );
            
            // Send email
            emailService.sendPasswordResetEmail(user.getEmail(), code);
            
            log.info("Password reset code sent to: {}", request.getEmail());
        } catch (Exception e) {
            // Don't reveal if email exists or not
            log.warn("Password reset requested for email: {}", request.getEmail());
        }
        
        // Always return success (security best practice)
    }
    
    /**
     * Reset password với verification code
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Validate verification code
        VerificationCode verificationCode = verificationCodeService.validateCode(
                request.getVerificationCode(),
                request.getEmail(),
                VerificationType.PASSWORD_RESET
        );
        
        // Update password
        userService.updatePassword(verificationCode.getUserId(), request.getNewPassword());
        
        // Invalidate all refresh tokens
        invalidateAllRefreshTokens(verificationCode.getUserId());
        
        // Mark code as used
        verificationCodeService.markAsUsed(verificationCode.getId());
        
        log.info("Password reset successful for user: {}", verificationCode.getUserId());
    }
    
    /**
     * Logout - revoke refresh token
     */
    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
        
        log.info("User {} logged out", refreshToken.getUserId());
    }
    
    /**
     * Generate refresh token và save to database
     */
    private String generateRefreshToken(String userId) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(refreshTokenExpirationDays);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiresAt(expiresAt)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        refreshTokenRepository.save(refreshToken);
        
        return token;
    }
    
    /**
     * Invalidate all refresh tokens for user
     */
    private void invalidateAllRefreshTokens(String userId) {
        refreshTokenRepository.findByUserIdAndRevokedFalse(userId)
                .forEach(token -> {
                    token.setRevoked(true);
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                });
    }
    
    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
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
