package com.portfolio.wallet.dto.request;

import com.portfolio.wallet.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction filters for search/query
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFilters {
    
    private TransactionType type;
    
    private String categoryId;
    
    private String accountId;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private BigDecimal minAmount;
    
    private BigDecimal maxAmount;
    
    private String keyword; // Search in note
}
