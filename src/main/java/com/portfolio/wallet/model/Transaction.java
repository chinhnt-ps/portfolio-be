package com.portfolio.wallet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Transaction entity
 * 
 * Represents a financial transaction (expense, income, or transfer)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {
    
    @Id
    private String id;
    
    @Indexed
    private String userId; // User ID từ JWT token
    
    private TransactionType type; // EXPENSE, INCOME, TRANSFER
    
    private BigDecimal amount; // Số tiền
    
    private String currency; // VND, USD, etc. (default: VND)
    
    private LocalDateTime occurredAt; // Thời gian giao dịch
    
    private String categoryId; // Category ID (nullable cho TRANSFER)
    
    private String accountId; // Account ID (for EXPENSE/INCOME)
    
    private String fromAccountId; // From account ID (for TRANSFER)
    
    private String toAccountId; // To account ID (for TRANSFER)
    
    /**
     * Liên kết tới công nợ (optional)
     * - receivableId: dùng cho RECEIVABLE_SETTLEMENT
     * - liabilityId: dùng cho LIABILITY_SETTLEMENT
     */
    private String receivableId;
    
    private String liabilityId;
    
    /**
     * Liên kết ngược tới Settlement được tạo từ giao dịch này (nếu có)
     */
    private String settlementId;
    
    private String note; // Ghi chú
    
    private List<String> attachmentIds; // File attachment IDs (optional)
    
    @Builder.Default
    private Boolean deleted = false; // Soft delete
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
