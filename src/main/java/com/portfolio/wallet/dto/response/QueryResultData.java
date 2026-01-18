package com.portfolio.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Data for QUERY_RESULT response type
 * Used when user asks a question (read-only query)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResultData implements NLPResponseData {
    
    public enum QueryType {
        SUMMARY,    // Summary statistics
        LIST,       // List of items
        SINGLE      // Single item
    }
    
    private QueryType queryType;
    
    /**
     * Summary text (e.g., "Tháng này bạn đã chi 2.5tr")
     */
    private String summary;
    
    /**
     * Breakdown of summary (optional)
     */
    private List<BreakdownItem> breakdown;
    
    /**
     * List of items (for LIST type)
     */
    private List<Map<String, Object>> items;
    
    /**
     * Single item (for SINGLE type)
     */
    private Map<String, Object> item;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreakdownItem {
        private String label;
        private Object value;
    }
}
