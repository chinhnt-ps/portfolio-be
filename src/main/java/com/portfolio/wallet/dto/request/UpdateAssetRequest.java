package com.portfolio.wallet.dto.request;

import com.portfolio.wallet.model.AssetType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for updating an asset
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAssetRequest {
    
    @Size(max = 100, message = "Asset name must not exceed 100 characters")
    private String name;
    
    private AssetType type;
    
    private BigDecimal estimatedValue;
    
    private String currency;
    
    private LocalDateTime acquiredAt;
    
    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
