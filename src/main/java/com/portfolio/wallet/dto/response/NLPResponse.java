package com.portfolio.wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Main NLP response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NLPResponse {
    
    /**
     * Response type determines how frontend should handle the response
     */
    public enum ResponseType {
        CONFIRM_DRAFT,      // Show draft preview for user to confirm
        SELECT_OPTION,       // Show options for user to pick (ambiguity)
        QUERY_RESULT,       // Show query result (read-only)
        NEED_MORE_INFO,     // Ask user for more information
        ERROR               // Show error message
    }
    
    /**
     * Intent parsed from user input
     */
    public enum Intent {
        CREATE_TRANSACTION,
        CREATE_RECEIVABLE,
        CREATE_LIABILITY,
        CREATE_SETTLEMENT,
        QUERY_DATA,
        UNKNOWN
    }
    
    private ResponseType responseType;
    private Intent intent;
    private Double confidence; // 0.0 to 1.0
    private String message; // User-friendly message
    
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = ConfirmDraftData.class, name = "CONFIRM_DRAFT"),
        @JsonSubTypes.Type(value = SelectOptionData.class, name = "SELECT_OPTION"),
        @JsonSubTypes.Type(value = QueryResultData.class, name = "QUERY_RESULT"),
        @JsonSubTypes.Type(value = NeedMoreInfoData.class, name = "NEED_MORE_INFO"),
        @JsonSubTypes.Type(value = ErrorData.class, name = "ERROR")
    })
    private NLPResponseData data;
}
