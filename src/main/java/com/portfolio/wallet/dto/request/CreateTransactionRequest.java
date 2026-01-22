package com.portfolio.wallet.dto.request;

import com.portfolio.wallet.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for creating a transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @Builder.Default
    private String currency = "VND";
    
    private LocalDateTime occurredAt; // Default to now if not provided
    
    private String categoryId; // Required for EXPENSE/INCOME, nullable for TRANSFER
    
    private String accountId; // Required for EXPENSE/INCOME
    
    private String fromAccountId; // Required for TRANSFER
    
    private String toAccountId; // Required for TRANSFER
    
    /**
     * Liên kết tới công nợ
     * - receivableId: bắt buộc cho RECEIVABLE_SETTLEMENT
     * - liabilityId: bắt buộc cho LIABILITY_SETTLEMENT
     */
    private String receivableId;
    
    private String liabilityId;
    
    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;
    
    private List<String> attachmentIds; // Optional file attachments
}
