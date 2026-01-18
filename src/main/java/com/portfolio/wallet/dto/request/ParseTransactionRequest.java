package com.portfolio.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for parsing transaction text using NLP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseTransactionRequest {
    
    @NotBlank(message = "Text input is required")
    private String text;
    
    private String timezone; // Optional, default to "Asia/Ho_Chi_Minh"
    
    private String locale; // Optional, default to "vi-VN"
}
