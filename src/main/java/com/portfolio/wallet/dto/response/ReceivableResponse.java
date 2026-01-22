package com.portfolio.wallet.dto.response;

import com.portfolio.wallet.model.Receivable;
import com.portfolio.wallet.model.ReceivableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for receivable
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivableResponse {
    
    private String id;
    private String counterpartyName;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime occurredAt;
    private LocalDateTime dueAt;
    private ReceivableStatus status;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount; // amount - paidAmount
    private String accountId; // Tài khoản nhận tiền (optional)
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isOverdue; // true if dueAt < now and status != PAID
    
    /**
     * Convert Receivable entity to ReceivableResponse
     */
    public static ReceivableResponse from(Receivable receivable) {
        BigDecimal paidAmount = receivable.getPaidAmount() != null ? receivable.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal remainingAmount = receivable.getAmount().subtract(paidAmount);
        
        // Check if overdue
        boolean isOverdue = false;
        if (receivable.getDueAt() != null 
                && LocalDateTime.now().isAfter(receivable.getDueAt())
                && receivable.getStatus() != ReceivableStatus.PAID) {
            isOverdue = true;
        }
        
        return ReceivableResponse.builder()
                .id(receivable.getId())
                .counterpartyName(receivable.getCounterpartyName())
                .amount(receivable.getAmount())
                .currency(receivable.getCurrency())
                .occurredAt(receivable.getOccurredAt())
                .dueAt(receivable.getDueAt())
                .status(receivable.getStatus())
                .paidAmount(paidAmount)
                .remainingAmount(remainingAmount)
                .accountId(receivable.getAccountId())
                .note(receivable.getNote())
                .createdAt(receivable.getCreatedAt())
                .updatedAt(receivable.getUpdatedAt())
                .isOverdue(isOverdue)
                .build();
    }
}
