package com.portfolio.wallet.dto.request;

import com.portfolio.wallet.model.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating an asset
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssetRequest {
    
    @NotBlank(message = "Asset name is required")
    @Size(max = 100, message = "Asset name must not exceed 100 characters")
    private String name;
    
    @NotNull(message = "Asset type is required")
    private AssetType type;
    
    private BigDecimal estimatedValue; // Optional
    
    @Builder.Default
    private String currency = "VND";
    
    private LocalDateTime acquiredAt; // Optional
    
    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
