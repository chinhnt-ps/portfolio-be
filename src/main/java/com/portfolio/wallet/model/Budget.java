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
import java.time.YearMonth;

/**
 * Budget entity
 * 
 * Represents a budget for a month (and optionally for a category)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "budgets")
public class Budget {
    
    @Id
    private String id;
    
    @Indexed
    private String userId; // User ID từ JWT token
    
    private YearMonth month; // YearMonth (e.g., 2025-01) - stored as String in MongoDB
    
    private String categoryId; // Category ID (null for total budget)
    
    private BigDecimal amount; // Budget amount
    
    private BigDecimal usedAmount; // Amount used (calculated)
    
    @Builder.Default
    private Boolean deleted = false; // Soft delete
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
