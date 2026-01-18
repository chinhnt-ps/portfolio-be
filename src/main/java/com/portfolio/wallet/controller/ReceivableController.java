package com.portfolio.wallet.controller;

import com.portfolio.common.annotation.RateLimited;
import com.portfolio.common.dto.ApiResponse;
import com.portfolio.wallet.dto.request.CreateReceivableRequest;
import com.portfolio.wallet.dto.request.UpdateReceivableRequest;
import com.portfolio.wallet.dto.response.ReceivableResponse;
import com.portfolio.wallet.service.ReceivableService;
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
 * Receivable controller
 * 
 * Endpoints:
 * - GET /api/v1/wallet/receivables - List receivables (paginated)
 * - POST /api/v1/wallet/receivables - Create receivable
 * - GET /api/v1/wallet/receivables/{id} - Get receivable
 * - PUT /api/v1/wallet/receivables/{id} - Update receivable
 * - DELETE /api/v1/wallet/receivables/{id} - Delete receivable (soft delete)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/wallet/receivables")
@RequiredArgsConstructor
public class ReceivableController {
    
    private final ReceivableService receivableService;
    
    /**
     * Get all receivables for the authenticated user
     */
    @GetMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Page<ReceivableResponse>>> getAllReceivables(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "occurredAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        String userId = authentication.getName(); // userId từ JWT token
        Page<ReceivableResponse> receivables = receivableService.getAllReceivables(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(receivables, "Receivables retrieved successfully"));
    }
    
    /**
     * Get receivable by id
     */
    @GetMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<ReceivableResponse>> getReceivableById(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        ReceivableResponse receivable = receivableService.getReceivableById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(receivable, "Receivable retrieved successfully"));
    }
    
    /**
     * Create a new receivable
     */
    @PostMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<ReceivableResponse>> createReceivable(
            @Valid @RequestBody CreateReceivableRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        ReceivableResponse receivable = receivableService.createReceivable(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(receivable, "Receivable created successfully"));
    }
    
    /**
     * Update a receivable
     */
    @PutMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<ReceivableResponse>> updateReceivable(
            @PathVariable String id,
            @Valid @RequestBody UpdateReceivableRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        ReceivableResponse receivable = receivableService.updateReceivable(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success(receivable, "Receivable updated successfully"));
    }
    
    /**
     * Delete a receivable (soft delete)
     */
    @DeleteMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Void>> deleteReceivable(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        receivableService.deleteReceivable(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Receivable deleted successfully"));
    }
}
