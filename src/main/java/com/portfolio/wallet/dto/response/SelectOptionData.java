package com.portfolio.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Data for SELECT_OPTION response type
 * Used when there are multiple possible matches (ambiguity)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectOptionData implements NLPResponseData {
    
    /**
     * Question to ask user
     */
    private String question;
    
    /**
     * List of options for user to choose from
     */
    private List<Option> options;
    
    /**
     * Whether user can select multiple options
     */
    @Builder.Default
    private Boolean allowMultiple = false;
    
    /**
     * Context data to send back when user selects an option
     */
    private Map<String, Object> context;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        /**
         * Unique identifier for this option
         */
        private String id;
        
        /**
         * Main label to display
         */
        private String label;
        
        /**
         * Optional sub-label with additional details
         */
        private String subLabel;
        
        /**
         * Data to send back when this option is selected
         */
        private Map<String, Object> data;
    }
}
