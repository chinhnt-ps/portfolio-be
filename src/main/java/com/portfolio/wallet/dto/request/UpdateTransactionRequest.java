package com.portfolio.wallet.dto.request;

import com.portfolio.wallet.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for updating a transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransactionRequest {
    
    private TransactionType type;
    
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    private String currency;
    
    private LocalDateTime occurredAt;
    
    private String categoryId;
    
    private String accountId;
    
    private String fromAccountId;
    
    private String toAccountId;
    
    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;
    
    private List<String> attachmentIds;
}
