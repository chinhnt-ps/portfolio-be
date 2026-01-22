package com.portfolio.wallet.dto.response;

import com.portfolio.wallet.model.Transaction;
import com.portfolio.wallet.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    
    private String id;
    private TransactionType type;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime occurredAt;
    private String categoryId;
    private String accountId;
    private String fromAccountId;
    private String toAccountId;
    private String receivableId;
    private String liabilityId;
    private String settlementId;
    private String note;
    private List<String> attachmentIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convert Transaction entity to TransactionResponse
     */
    public static TransactionResponse from(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .occurredAt(transaction.getOccurredAt())
                .categoryId(transaction.getCategoryId())
                .accountId(transaction.getAccountId())
                .fromAccountId(transaction.getFromAccountId())
                .toAccountId(transaction.getToAccountId())
                .receivableId(transaction.getReceivableId())
                .liabilityId(transaction.getLiabilityId())
                .settlementId(transaction.getSettlementId())
                .note(transaction.getNote())
                .attachmentIds(transaction.getAttachmentIds())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
