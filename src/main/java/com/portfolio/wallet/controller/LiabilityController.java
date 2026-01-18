package com.portfolio.wallet.controller;

import com.portfolio.common.annotation.RateLimited;
import com.portfolio.common.dto.ApiResponse;
import com.portfolio.wallet.dto.request.CreateLiabilityRequest;
import com.portfolio.wallet.dto.request.UpdateLiabilityRequest;
import com.portfolio.wallet.dto.response.LiabilityResponse;
import com.portfolio.wallet.service.LiabilityService;
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
 * Liability controller
 * 
 * Endpoints:
 * - GET /api/v1/wallet/liabilities - List liabilities (paginated)
 * - POST /api/v1/wallet/liabilities - Create liability
 * - GET /api/v1/wallet/liabilities/{id} - Get liability
 * - PUT /api/v1/wallet/liabilities/{id} - Update liability
 * - DELETE /api/v1/wallet/liabilities/{id} - Delete liability (soft delete)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/wallet/liabilities")
@RequiredArgsConstructor
public class LiabilityController {
    
    private final LiabilityService liabilityService;
    
    /**
     * Get all liabilities for the authenticated user
     */
    @GetMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Page<LiabilityResponse>>> getAllLiabilities(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "occurredAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        String userId = authentication.getName(); // userId từ JWT token
        Page<LiabilityResponse> liabilities = liabilityService.getAllLiabilities(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(liabilities, "Liabilities retrieved successfully"));
    }
    
    /**
     * Get liability by id
     */
    @GetMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<LiabilityResponse>> getLiabilityById(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        LiabilityResponse liability = liabilityService.getLiabilityById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(liability, "Liability retrieved successfully"));
    }
    
    /**
     * Create a new liability
     */
    @PostMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<LiabilityResponse>> createLiability(
            @Valid @RequestBody CreateLiabilityRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        LiabilityResponse liability = liabilityService.createLiability(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(liability, "Liability created successfully"));
    }
    
    /**
     * Update a liability
     */
    @PutMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<LiabilityResponse>> updateLiability(
            @PathVariable String id,
            @Valid @RequestBody UpdateLiabilityRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        LiabilityResponse liability = liabilityService.updateLiability(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success(liability, "Liability updated successfully"));
    }
    
    /**
     * Delete a liability (soft delete)
     */
    @DeleteMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Void>> deleteLiability(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        liabilityService.deleteLiability(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Liability deleted successfully"));
    }
}
