package com.portfolio.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
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
 * Request DTO for creating a liability
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLiabilityRequest {
    
    @NotBlank(message = "Counterparty name is required")
    @Size(max = 100, message = "Counterparty name must not exceed 100 characters")
    private String counterpartyName;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @Builder.Default
    private String currency = "VND";
    
    private LocalDateTime occurredAt; // Default to now if not provided
    
    private LocalDateTime dueAt; // Optional
    
    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
