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
    
    // POSTPAID specific fields
    private BigDecimal creditLimit;     // Hạn mức (nullable = unlimited)
    private BigDecimal currentDebt;     // Dư nợ hiện tại (chỉ POSTPAID)
    private BigDecimal availableLimit;  // Hạn mức còn lại = creditLimit - currentDebt
    
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
                .creditLimit(account.getCreditLimit())
                .note(account.getNote())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert Account entity to AccountResponse with calculated currentBalance
     * 
     * Với POSTPAID account:
     * - currentBalance trả về 0 (không có tiền thật)
     * - currentDebt = nợ hiện tại (tính từ balance âm)
     * - availableLimit = creditLimit - currentDebt
     */
    public static AccountResponse from(Account account, BigDecimal currentBalance) {
        AccountResponseBuilder builder = AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .currency(account.getCurrency())
                .openingBalance(account.getOpeningBalance())
                .creditLimit(account.getCreditLimit())
                .note(account.getNote())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt());
        
        if (account.getType() == AccountType.POSTPAID) {
            // Với POSTPAID:
            // - openingBalance = dư nợ ban đầu (initial debt)
            // - expense làm tăng nợ
            // - transfer_in làm giảm nợ (trả nợ)
            // 
            // calculateCurrentBalance trả về: openingBalance - expense + transfer_in
            // debt = openingBalance + expense - transfer_in
            //      = openingBalance + (openingBalance - currentBalance)
            //      = 2 * openingBalance - currentBalance
            BigDecimal openingBalance = account.getOpeningBalance() != null 
                    ? account.getOpeningBalance() 
                    : BigDecimal.ZERO;
            BigDecimal debt = openingBalance.multiply(BigDecimal.valueOf(2)).subtract(currentBalance);
            if (debt.compareTo(BigDecimal.ZERO) < 0) {
                debt = BigDecimal.ZERO; // Không có debt âm (đã trả hết nợ)
            }
            builder.currentDebt(debt);
            builder.currentBalance(BigDecimal.ZERO); // Không có "số dư tiền thật"
            
            if (account.getCreditLimit() != null) {
                BigDecimal available = account.getCreditLimit().subtract(debt);
                builder.availableLimit(available.max(BigDecimal.ZERO));
            } else {
                builder.availableLimit(null); // Không giới hạn
            }
        } else {
            builder.currentBalance(currentBalance);
            builder.currentDebt(null);
            builder.availableLimit(null);
        }
        
        return builder.build();
    }
}
