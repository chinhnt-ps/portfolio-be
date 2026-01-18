package com.portfolio.wallet.service;

import com.portfolio.common.exception.NotFoundException;
import com.portfolio.wallet.dto.request.CreateAssetRequest;
import com.portfolio.wallet.dto.request.UpdateAssetRequest;
import com.portfolio.wallet.dto.response.AssetResponse;
import com.portfolio.wallet.model.Asset;
import com.portfolio.wallet.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Asset service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {
    
    private final AssetRepository assetRepository;
    
    /**
     * Get all assets for a user (paginated)
     */
    public Page<AssetResponse> getAllAssets(String userId, Pageable pageable) {
        log.debug("Getting all assets for user: {}", userId);
        Page<Asset> assets = assetRepository.findByUserIdAndDeletedFalse(userId, pageable);
        return assets.map(AssetResponse::from);
    }
    
    /**
     * Get all assets for a user (list)
     */
    public List<AssetResponse> getAllAssets(String userId) {
        log.debug("Getting all assets for user: {}", userId);
        List<Asset> assets = assetRepository.findByUserIdAndDeletedFalse(userId);
        return assets.stream()
                .map(AssetResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get asset by id
     */
    public AssetResponse getAssetById(String id, String userId) {
        log.debug("Getting asset by id: {} for user: {}", id, userId);
        Asset asset = assetRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Asset not found"));
        return AssetResponse.from(asset);
    }
    
    /**
     * Get total asset value for a user
     */
    public BigDecimal getTotalAssetValue(String userId) {
        log.debug("Calculating total asset value for user: {}", userId);
        List<Asset> assets = assetRepository.findByUserIdAndDeletedFalse(userId);
        return assets.stream()
                .filter(asset -> asset.getEstimatedValue() != null)
                .map(Asset::getEstimatedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Create a new asset
     */
    @Transactional
    public AssetResponse createAsset(CreateAssetRequest request, String userId) {
        log.debug("Creating asset for user: {}", userId);
        
        try {
            if (request == null) {
                throw new IllegalArgumentException("Request cannot be null");
            }
            if (request.getType() == null) {
                throw new IllegalArgumentException("Asset type cannot be null");
            }
            
            Asset asset = Asset.builder()
                    .userId(userId)
                    .name(request.getName())
                    .type(request.getType())
                    .estimatedValue(request.getEstimatedValue())
                    .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                    .acquiredAt(request.getAcquiredAt())
                    .note(request.getNote())
                    .deleted(false)
                    .build();
            
            Asset saved = assetRepository.save(asset);
            log.info("Asset created successfully: {}", saved.getId());
            return AssetResponse.from(saved);
        } catch (Exception e) {
            log.error("Error creating asset: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Update an asset
     */
    @Transactional
    public AssetResponse updateAsset(String id, UpdateAssetRequest request, String userId) {
        log.debug("Updating asset: {} for user: {}", id, userId);
        
        Asset asset = assetRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Asset not found"));
        
        // Update fields if provided
        if (request.getName() != null) {
            asset.setName(request.getName());
        }
        if (request.getType() != null) {
            asset.setType(request.getType());
        }
        if (request.getEstimatedValue() != null) {
            asset.setEstimatedValue(request.getEstimatedValue());
        }
        if (request.getCurrency() != null) {
            asset.setCurrency(request.getCurrency());
        }
        if (request.getAcquiredAt() != null) {
            asset.setAcquiredAt(request.getAcquiredAt());
        }
        if (request.getNote() != null) {
            asset.setNote(request.getNote());
        }
        
        Asset updated = assetRepository.save(asset);
        log.info("Asset updated successfully: {}", updated.getId());
        return AssetResponse.from(updated);
    }
    
    /**
     * Delete an asset (soft delete)
     */
    @Transactional
    public void deleteAsset(String id, String userId) {
        log.debug("Deleting asset: {} for user: {}", id, userId);
        
        Asset asset = assetRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Asset not found"));
        
        // Soft delete
        asset.setDeleted(true);
        assetRepository.save(asset);
        log.info("Asset deleted successfully: {}", id);
    }
}
