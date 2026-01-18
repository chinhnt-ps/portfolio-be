package com.portfolio.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data for ERROR response type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorData implements NLPResponseData {
    
    /**
     * Error code
     */
    private String code;
    
    /**
     * Error message
     */
    private String message;
    
    /**
     * Optional details
     */
    private String details;
}
