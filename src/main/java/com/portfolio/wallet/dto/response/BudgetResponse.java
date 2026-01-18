package com.portfolio.wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.portfolio.wallet.model.Budget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Response DTO for budget
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    
    private String id;
    
    @JsonFormat(pattern = "yyyy-MM")
    private YearMonth month; // Serialized as "yyyy-MM" string
    
    private String categoryId;
    private BigDecimal amount;
    private BigDecimal usedAmount;
    private BigDecimal remainingAmount; // amount - usedAmount
    private Double percentageUsed; // (usedAmount / amount) * 100
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convert Budget entity to BudgetResponse
     */
    public static BudgetResponse from(Budget budget) {
        BigDecimal usedAmount = budget.getUsedAmount() != null ? budget.getUsedAmount() : BigDecimal.ZERO;
        BigDecimal remainingAmount = budget.getAmount().subtract(usedAmount);
        Double percentageUsed = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                ? (usedAmount.divide(budget.getAmount(), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))).doubleValue()
                : 0.0;
        
        return BudgetResponse.builder()
                .id(budget.getId())
                .month(budget.getMonth())
                .categoryId(budget.getCategoryId())
                .amount(budget.getAmount())
                .usedAmount(usedAmount)
                .remainingAmount(remainingAmount)
                .percentageUsed(percentageUsed)
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}
