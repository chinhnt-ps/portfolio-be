package com.portfolio.wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base interface for NLP response data
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ConfirmDraftData.class, name = "CONFIRM_DRAFT"),
    @JsonSubTypes.Type(value = SelectOptionData.class, name = "SELECT_OPTION"),
    @JsonSubTypes.Type(value = QueryResultData.class, name = "QUERY_RESULT"),
    @JsonSubTypes.Type(value = NeedMoreInfoData.class, name = "NEED_MORE_INFO"),
    @JsonSubTypes.Type(value = ErrorData.class, name = "ERROR")
})
public interface NLPResponseData {
}
