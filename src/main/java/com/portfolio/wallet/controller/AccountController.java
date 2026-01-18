package com.portfolio.wallet.controller;

import com.portfolio.common.annotation.RateLimited;
import com.portfolio.common.dto.ApiResponse;
import com.portfolio.wallet.dto.request.CreateAccountRequest;
import com.portfolio.wallet.dto.request.UpdateAccountRequest;
import com.portfolio.wallet.dto.response.AccountResponse;
import com.portfolio.wallet.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Account controller
 * 
 * Endpoints:
 * - GET /api/v1/wallet/accounts - List accounts (paginated)
 * - POST /api/v1/wallet/accounts - Create account
 * - GET /api/v1/wallet/accounts/{id} - Get account
 * - PUT /api/v1/wallet/accounts/{id} - Update account
 * - DELETE /api/v1/wallet/accounts/{id} - Delete account (soft delete)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/wallet/accounts")
@RequiredArgsConstructor
public class AccountController {
    
    private final AccountService accountService;
    
    /**
     * Get all accounts for the authenticated user
     */
    @GetMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Page<AccountResponse>>> getAllAccounts(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        String userId = authentication.getName(); // userId từ JWT token
        Page<AccountResponse> accounts = accountService.getAllAccounts(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(accounts, "Accounts retrieved successfully"));
    }
    
    /**
     * Get account by id
     */
    @GetMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        AccountResponse account = accountService.getAccountById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(account, "Account retrieved successfully"));
    }
    
    /**
     * Create a new account
     */
    @PostMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        AccountResponse account = accountService.createAccount(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(account, "Account created successfully"));
    }
    
    /**
     * Update an account
     */
    @PutMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @PathVariable String id,
            @Valid @RequestBody UpdateAccountRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        AccountResponse account = accountService.updateAccount(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success(account, "Account updated successfully"));
    }
    
    /**
     * Delete an account (soft delete)
     */
    @DeleteMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        accountService.deleteAccount(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Account deleted successfully"));
    }
}
