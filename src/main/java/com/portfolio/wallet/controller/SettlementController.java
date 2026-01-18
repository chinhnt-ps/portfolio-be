package com.portfolio.wallet.controller;

import com.portfolio.common.annotation.RateLimited;
import com.portfolio.common.dto.ApiResponse;
import com.portfolio.wallet.dto.request.CreateSettlementRequest;
import com.portfolio.wallet.dto.request.UpdateSettlementRequest;
import com.portfolio.wallet.dto.response.SettlementResponse;
import com.portfolio.wallet.service.SettlementService;
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

import java.util.List;

/**
 * Settlement controller
 * 
 * Endpoints:
 * - GET /api/v1/wallet/settlements - List settlements (paginated)
 * - POST /api/v1/wallet/settlements - Create settlement
 * - GET /api/v1/wallet/settlements/{id} - Get settlement
 * - PUT /api/v1/wallet/settlements/{id} - Update settlement
 * - DELETE /api/v1/wallet/settlements/{id} - Delete settlement (soft delete)
 * - GET /api/v1/wallet/settlements/receivable/{receivableId} - Get settlements for receivable
 * - GET /api/v1/wallet/settlements/liability/{liabilityId} - Get settlements for liability
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/wallet/settlements")
@RequiredArgsConstructor
public class SettlementController {
    
    private final SettlementService settlementService;
    
    /**
     * Get all settlements for the authenticated user
     */
    @GetMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Page<SettlementResponse>>> getAllSettlements(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "occurredAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        String userId = authentication.getName(); // userId từ JWT token
        Page<SettlementResponse> settlements = settlementService.getAllSettlements(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(settlements, "Settlements retrieved successfully"));
    }
    
    /**
     * Get settlement by id
     */
    @GetMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<SettlementResponse>> getSettlementById(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        SettlementResponse settlement = settlementService.getSettlementById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(settlement, "Settlement retrieved successfully"));
    }
    
    /**
     * Get all settlements for a receivable
     */
    @GetMapping("/receivable/{receivableId}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getSettlementsByReceivableId(
            @PathVariable String receivableId,
            Authentication authentication) {
        String userId = authentication.getName();
        List<SettlementResponse> settlements = settlementService.getSettlementsByReceivableId(receivableId, userId);
        return ResponseEntity.ok(ApiResponse.success(settlements, "Settlements retrieved successfully"));
    }
    
    /**
     * Get all settlements for a liability
     */
    @GetMapping("/liability/{liabilityId}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getSettlementsByLiabilityId(
            @PathVariable String liabilityId,
            Authentication authentication) {
        String userId = authentication.getName();
        List<SettlementResponse> settlements = settlementService.getSettlementsByLiabilityId(liabilityId, userId);
        return ResponseEntity.ok(ApiResponse.success(settlements, "Settlements retrieved successfully"));
    }
    
    /**
     * Create a new settlement
     */
    @PostMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<SettlementResponse>> createSettlement(
            @Valid @RequestBody CreateSettlementRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        SettlementResponse settlement = settlementService.createSettlement(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(settlement, "Settlement created successfully"));
    }
    
    /**
     * Update a settlement
     */
    @PutMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<SettlementResponse>> updateSettlement(
            @PathVariable String id,
            @Valid @RequestBody UpdateSettlementRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        SettlementResponse settlement = settlementService.updateSettlement(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success(settlement, "Settlement updated successfully"));
    }
    
    /**
     * Delete a settlement (soft delete)
     */
    @DeleteMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Void>> deleteSettlement(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        settlementService.deleteSettlement(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Settlement deleted successfully"));
    }
}
