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
    private BigDecimal currentBalance; // Calculated: sum of all transactions
    
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
                .currentBalance(BigDecimal.ZERO) // Default to 0, will be calculated
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
     * - currentBalance từ calculateCurrentBalance() = tổng transactions (EXPENSE trừ đi, TRANSFER_IN cộng vào)
     * - currentBalance trả về 0 (không có tiền thật)
     * - currentDebt = -currentBalance (nếu currentBalance âm, tức là có nợ)
     * - availableLimit = creditLimit - currentDebt
     */
    public static AccountResponse from(Account account, BigDecimal currentBalance) {
        AccountResponseBuilder builder = AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .currency(account.getCurrency())
                .creditLimit(account.getCreditLimit())
                .note(account.getNote())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt());
        
        if (account.getType() == AccountType.POSTPAID) {
            // Với POSTPAID:
            // - currentBalance từ calculateCurrentBalance() = tổng transactions
            //   + EXPENSE làm giảm balance (âm) → tăng nợ
            //   + TRANSFER_IN làm tăng balance (dương) → giảm nợ
            //   + BALANCE_ADJUSTMENT (initialBalance) làm tăng balance (dương) → giảm nợ
            // - Nếu currentBalance < 0 → có nợ → debt = -currentBalance (dương)
            // - Nếu currentBalance >= 0 → không có nợ → debt = 0
            BigDecimal debt;
            if (currentBalance.compareTo(BigDecimal.ZERO) < 0) {
                debt = currentBalance.negate(); // currentBalance âm → debt dương
            } else {
                debt = BigDecimal.ZERO; // currentBalance >= 0 → không có nợ (đã trả hết hoặc có credit)
            }
            builder.currentDebt(debt);
            builder.currentBalance(BigDecimal.ZERO); // Không có "số dư tiền thật"
            
            // Debug log
            System.out.println(String.format(
                "[POSTPAID Debug] Account: %s (ID: %s), currentBalance (from calculateCurrentBalance): %s, calculated debt: %s",
                account.getName(), account.getId(), currentBalance, debt
            ));
            
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
