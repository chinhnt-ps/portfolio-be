package com.portfolio.wallet.dto.response;

import com.portfolio.wallet.model.Account;
import com.portfolio.wallet.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for account
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    
    private String id;
    private String name;
    private AccountType type;
    private String currency;
    private BigDecimal openingBalance;
    private BigDecimal currentBalance; // Calculated: openingBalance + transactions
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convert Account entity to AccountResponse
     * Note: currentBalance should be calculated separately using AccountService.calculateCurrentBalance()
     */
    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .currency(account.getCurrency())
                .openingBalance(account.getOpeningBalance())
                .currentBalance(account.getOpeningBalance()) // Default to openingBalance, will be calculated
                .note(account.getNote())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert Account entity to AccountResponse with calculated currentBalance
     */
    public static AccountResponse from(Account account, BigDecimal currentBalance) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .currency(account.getCurrency())
                .openingBalance(account.getOpeningBalance())
                .currentBalance(currentBalance)
                .note(account.getNote())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
