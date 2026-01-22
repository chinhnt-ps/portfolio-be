package com.portfolio.wallet.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for updating a liability
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLiabilityRequest {
    
    @Size(max = 100, message = "Counterparty name must not exceed 100 characters")
    private String counterpartyName;
    
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String currency;
    
    private LocalDateTime occurredAt;
    
    private LocalDateTime dueAt;
    
    /**
     * Tài khoản trả tiền khi vay (optional)
     */
    private String accountId;
    
    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
