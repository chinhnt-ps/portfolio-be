package com.portfolio.wallet.dto.request;

import com.portfolio.wallet.model.AccountType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating an account
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {
    
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String name;
    
    private AccountType type;
    
    private String currency;
    
    private BigDecimal openingBalance;
    
    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
