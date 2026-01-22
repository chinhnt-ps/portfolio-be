package com.portfolio.wallet.dto.request;

import com.portfolio.wallet.model.SettlementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating a settlement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSettlementRequest {
    
    @NotNull(message = "Settlement type is required")
    private SettlementType type; // RECEIVABLE or LIABILITY
    
    private String receivableId; // Required if type is RECEIVABLE
    
    private String liabilityId; // Required if type is LIABILITY
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @Builder.Default
    private String currency = "VND";
    
    private LocalDateTime occurredAt; // Default to now if not provided
    
    /**
     * Tài khoản dùng để thanh toán (optional)
     * Nếu có, sẽ cập nhật account balance khi tạo settlement
     */
    private String accountId;
    
    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
