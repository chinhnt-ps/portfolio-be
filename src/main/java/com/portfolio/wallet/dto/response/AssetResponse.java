package com.portfolio.wallet.dto.response;

import com.portfolio.wallet.model.Asset;
import com.portfolio.wallet.model.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for asset
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetResponse {
    
    private String id;
    private String name;
    private AssetType type;
    private BigDecimal estimatedValue;
    private String currency;
    private LocalDateTime acquiredAt;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convert Asset entity to AssetResponse
     */
    public static AssetResponse from(Asset asset) {
        return AssetResponse.builder()
                .id(asset.getId())
                .name(asset.getName())
                .type(asset.getType())
                .estimatedValue(asset.getEstimatedValue())
                .currency(asset.getCurrency())
                .acquiredAt(asset.getAcquiredAt())
                .note(asset.getNote())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }
}
