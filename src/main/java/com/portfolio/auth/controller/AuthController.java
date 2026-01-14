package com.portfolio.auth.controller;

import com.portfolio.auth.dto.request.*;
import com.portfolio.auth.dto.response.AuthResponse;
import com.portfolio.auth.dto.response.UserResponse;
import com.portfolio.auth.service.AuthService;
import com.portfolio.auth.service.UserService;
import com.portfolio.common.annotation.RateLimited;
import com.portfolio.common.dto.ApiResponse;
import com.portfolio.common.util.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    
    /**
     * Register new user
     */
    @PostMapping("/register")
    @RateLimited(RateLimited.RateLimitType.REGISTER)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Đăng ký thành công. Vui lòng kiểm tra email để xác nhận tài khoản."
                ));
    }
    
    /**
     * Verify email với verification code
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        AuthResponse response = authService.verifyEmail(request);
        
        return ResponseEntity.ok(ApiResponse.success(
                response,
                "Xác nhận thành công. Bạn có thể đăng nhập ngay."
        ));
    }
    
    /**
     * Login với email và password
     */
    @PostMapping("/login")
    @RateLimited(RateLimited.RateLimitType.LOGIN)
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        
        return ResponseEntity.ok(ApiResponse.success(
                response,
                "Đăng nhập thành công"
        ));
    }
    
    /**
     * Refresh access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Forgot password - send verification code
     */
    @PostMapping("/forgot-password")
    @RateLimited(RateLimited.RateLimitType.FORGOT_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        
        return ResponseEntity.ok(ApiResponse.success(
                null,
                "Nếu email tồn tại, chúng tôi đã gửi mã xác nhận. Vui lòng kiểm tra email."
        ));
    }
    
    /**
     * Reset password với verification code
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        
        return ResponseEntity.ok(ApiResponse.success(
                null,
                "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại."
        ));
    }
    
    /**
     * Logout - revoke refresh token
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        
        return ResponseEntity.ok(ApiResponse.success(
                null,
                "Đăng xuất thành công"
        ));
    }
    
    /**
     * Update user role (ADMIN only)
     * 
     * PUT /api/v1/auth/users/{userId}/role
     * 
     * @param userId User ID to update
     * @param request UpdateRoleRequest with new role
     * @return Updated user info
     */
    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable String userId,
            @Valid @RequestBody UpdateRoleRequest request) {
        
        var updatedUser = userService.updateRole(userId, request.getRole());
        UserResponse userResponse = UserMapper.toUserResponse(updatedUser);
        
        return ResponseEntity.ok(ApiResponse.success(
                userResponse,
                "Cập nhật role thành công"
        ));
    }
}
