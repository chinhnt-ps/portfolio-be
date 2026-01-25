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

/**
 * Account/Wallet entity
 * 
 * Represents a user's account/wallet (Cash, Bank, E-wallet, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "accounts")
public class Account {
    
    @Id
    private String id;
    
    @Indexed
    private String userId; // User ID từ JWT token
    
    private String name; // Tên tài khoản/ví
    
    private AccountType type; // CASH, BANK, E_WALLET, OTHER
    
    private String currency; // VND, USD, etc. (default: VND)
    
    @Builder.Default
    private BigDecimal openingBalance = BigDecimal.ZERO; // Số dư ban đầu
    
    /**
     * Hạn mức tín dụng (chỉ dùng cho POSTPAID, nullable)
     * null = không giới hạn
     */
    private BigDecimal creditLimit;
    
    private String note; // Ghi chú
    
    @Builder.Default
    private Boolean deleted = false; // Soft delete
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
