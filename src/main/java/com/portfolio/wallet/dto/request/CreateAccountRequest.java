package com.portfolio.wallet.dto.request;

import com.portfolio.wallet.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating an account
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    
    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String name;
    
    @NotNull(message = "Account type is required")
    private AccountType type;
    
    @Builder.Default
    private String currency = "VND";
    
    @Builder.Default
    private BigDecimal openingBalance = BigDecimal.ZERO;
    
    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
