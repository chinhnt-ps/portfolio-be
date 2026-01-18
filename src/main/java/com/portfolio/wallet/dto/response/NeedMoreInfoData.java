package com.portfolio.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Data for NEED_MORE_INFO response type
 * Used when more information is needed from user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NeedMoreInfoData implements NLPResponseData {
    
    /**
     * Question to ask user
     */
    private String question;
    
    /**
     * Suggested options (if applicable)
     */
    private List<String> suggestions;
    
    /**
     * Type of input expected
     */
    public enum InputType {
        TEXT,
        SELECT,
        DATE,
        AMOUNT
    }
    
    private InputType inputType;
    
    /**
     * Context data to send back with user's answer
     */
    private Map<String, Object> context;
}
