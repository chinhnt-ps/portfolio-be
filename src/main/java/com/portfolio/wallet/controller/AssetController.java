package com.portfolio.wallet.controller;

import com.portfolio.common.annotation.RateLimited;
import com.portfolio.common.dto.ApiResponse;
import com.portfolio.wallet.dto.request.CreateAssetRequest;
import com.portfolio.wallet.dto.request.UpdateAssetRequest;
import com.portfolio.wallet.dto.response.AssetResponse;
import com.portfolio.wallet.service.AssetService;
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

import java.math.BigDecimal;

/**
 * Asset controller
 * 
 * Endpoints:
 * - GET /api/v1/wallet/assets - List assets (paginated)
 * - POST /api/v1/wallet/assets - Create asset
 * - GET /api/v1/wallet/assets/{id} - Get asset
 * - PUT /api/v1/wallet/assets/{id} - Update asset
 * - DELETE /api/v1/wallet/assets/{id} - Delete asset (soft delete)
 * - GET /api/v1/wallet/assets/total-value - Get total asset value
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/wallet/assets")
@RequiredArgsConstructor
public class AssetController {
    
    private final AssetService assetService;
    
    /**
     * Get all assets for the authenticated user
     */
    @GetMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Page<AssetResponse>>> getAllAssets(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        String userId = authentication.getName(); // userId từ JWT token
        Page<AssetResponse> assets = assetService.getAllAssets(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(assets, "Assets retrieved successfully"));
    }
    
    /**
     * Get asset by id
     */
    @GetMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<AssetResponse>> getAssetById(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        AssetResponse asset = assetService.getAssetById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(asset, "Asset retrieved successfully"));
    }
    
    /**
     * Get total asset value for the authenticated user
     */
    @GetMapping("/total-value")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalAssetValue(
            Authentication authentication) {
        String userId = authentication.getName();
        BigDecimal totalValue = assetService.getTotalAssetValue(userId);
        return ResponseEntity.ok(ApiResponse.success(totalValue, "Total asset value retrieved successfully"));
    }
    
    /**
     * Create a new asset
     */
    @PostMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<AssetResponse>> createAsset(
            @Valid @RequestBody CreateAssetRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        AssetResponse asset = assetService.createAsset(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(asset, "Asset created successfully"));
    }
    
    /**
     * Update an asset
     */
    @PutMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<AssetResponse>> updateAsset(
            @PathVariable String id,
            @Valid @RequestBody UpdateAssetRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        AssetResponse asset = assetService.updateAsset(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success(asset, "Asset updated successfully"));
    }
    
    /**
     * Delete an asset (soft delete)
     */
    @DeleteMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Void>> deleteAsset(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        assetService.deleteAsset(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Asset deleted successfully"));
    }
}
