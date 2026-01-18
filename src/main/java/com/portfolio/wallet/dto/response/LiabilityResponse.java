package com.portfolio.wallet.dto.response;

import com.portfolio.wallet.model.Liability;
import com.portfolio.wallet.model.LiabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for liability
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiabilityResponse {
    
    private String id;
    private String counterpartyName;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime occurredAt;
    private LocalDateTime dueAt;
    private LiabilityStatus status;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount; // amount - paidAmount
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isOverdue; // true if dueAt < now and status != PAID
    
    /**
     * Convert Liability entity to LiabilityResponse
     */
    public static LiabilityResponse from(Liability liability) {
        BigDecimal paidAmount = liability.getPaidAmount() != null ? liability.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal remainingAmount = liability.getAmount().subtract(paidAmount);
        
        // Check if overdue
        boolean isOverdue = false;
        if (liability.getDueAt() != null 
                && LocalDateTime.now().isAfter(liability.getDueAt())
                && liability.getStatus() != LiabilityStatus.PAID) {
            isOverdue = true;
        }
        
        return LiabilityResponse.builder()
                .id(liability.getId())
                .counterpartyName(liability.getCounterpartyName())
                .amount(liability.getAmount())
                .currency(liability.getCurrency())
                .occurredAt(liability.getOccurredAt())
                .dueAt(liability.getDueAt())
                .status(liability.getStatus())
                .paidAmount(paidAmount)
                .remainingAmount(remainingAmount)
                .note(liability.getNote())
                .createdAt(liability.getCreatedAt())
                .updatedAt(liability.getUpdatedAt())
                .isOverdue(isOverdue)
                .build();
    }
}
