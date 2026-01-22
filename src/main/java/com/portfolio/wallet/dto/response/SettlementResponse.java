package com.portfolio.wallet.dto.response;

import com.portfolio.wallet.model.Settlement;
import com.portfolio.wallet.model.SettlementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for settlement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponse {
    
    private String id;
    private SettlementType type;
    private String receivableId;
    private String liabilityId;
    private String transactionId;
    private String accountId; // Tài khoản thanh toán (optional)
    private BigDecimal amount;
    private String currency;
    private LocalDateTime occurredAt;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convert Settlement entity to SettlementResponse
     */
    public static SettlementResponse from(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .type(settlement.getType())
                .receivableId(settlement.getReceivableId())
                .liabilityId(settlement.getLiabilityId())
                .transactionId(settlement.getTransactionId())
                .accountId(settlement.getAccountId())
                .amount(settlement.getAmount())
                .currency(settlement.getCurrency())
                .occurredAt(settlement.getOccurredAt())
                .note(settlement.getNote())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .build();
    }
}
