package com.portfolio.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for dashboard report
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardReportResponse {
    
    /**
     * Total income for the period
     */
    private BigDecimal totalIncome;
    
    /**
     * Total expense for the period
     */
    private BigDecimal totalExpense;
    
    /**
     * Net savings (income - expense)
     */
    private BigDecimal netSavings;
    
    /**
     * Accounts overview with balances
     */
    private List<AccountBalance> accountsOverview;
    
    /**
     * Top categories by expense
     */
    private List<CategorySummary> topCategories;
    
    /**
     * Account balance info
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountBalance {
        private String accountId;
        private String accountName;
        private BigDecimal balance;
    }
    
    /**
     * Category summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private String categoryId;
        private String categoryName;
        private BigDecimal totalAmount;
        private Long transactionCount;
    }
}
