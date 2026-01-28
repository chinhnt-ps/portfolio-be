package com.portfolio.wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data for CONFIRM_DRAFT response type
 * Contains draft data for user to confirm before creating
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmDraftData implements NLPResponseData {
    
    public enum DraftType {
        TRANSACTION,
        RECEIVABLE,
        LIABILITY,
        SETTLEMENT,
        BALANCE_ADJUSTMENT
    }
    
    private DraftType draftType;
    
    // Draft data - can be any of the draft types
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "draftType", visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = TransactionDraft.class, name = "TRANSACTION"),
        @JsonSubTypes.Type(value = ReceivableDraft.class, name = "RECEIVABLE"),
        @JsonSubTypes.Type(value = LiabilityDraft.class, name = "LIABILITY"),
        @JsonSubTypes.Type(value = SettlementDraft.class, name = "SETTLEMENT"),
        @JsonSubTypes.Type(value = BalanceAdjustmentDraft.class, name = "BALANCE_ADJUSTMENT")
    })
    private Object draft;
    
    /**
     * Fields that need user confirmation (missing or ambiguous)
     */
    private List<String> needConfirmFields;
    
    /**
     * Fields that were auto-filled with confidence scores
     */
    private List<AutoFilledField> autoFilledFields;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutoFilledField {
        private String field;
        private Object value;
        private Double confidence;
    }
    
    // Draft classes
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDraft {
        private String type; // "EXPENSE", "INCOME", "TRANSFER"
        private BigDecimal amount;
        private String currency;
        private String occurredAt; // ISO format with timezone: "YYYY-MM-DDTHH:mm:ss+07:00"
        private String categoryId;
        private String categoryName; // For display
        private String accountId;
        private String accountName; // For display
        private String fromAccountId;
        private String fromAccountName;
        private String toAccountId;
        private String toAccountName;
        private String note;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceivableDraft {
        private String counterpartyName;
        private BigDecimal amount;
        private String currency;
        private String occurredAt; // ISO format with timezone: "YYYY-MM-DDTHH:mm:ss+07:00"
        private String dueAt; // ISO format with timezone: "YYYY-MM-DDTHH:mm:ss+07:00"
        private String note;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LiabilityDraft {
        private String counterpartyName;
        private BigDecimal amount;
        private String currency;
        private String occurredAt; // ISO format with timezone: "YYYY-MM-DDTHH:mm:ss+07:00"
        private String dueAt; // ISO format with timezone: "YYYY-MM-DDTHH:mm:ss+07:00"
        private String note;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementDraft {
        private String type; // "RECEIVABLE" or "LIABILITY"
        private String receivableId; // If type is RECEIVABLE
        private String liabilityId; // If type is LIABILITY
        private String counterpartyName; // For display
        private BigDecimal amount;
        private String currency;
        private String accountId;
        private String accountName; // For display
        private String settledAt; // ISO format with timezone: "YYYY-MM-DDTHH:mm:ss+07:00"
        private String note;
    }

    /**
     * Draft cho điều chỉnh số dư tài khoản
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceAdjustmentDraft {
        private String accountId;
        private String accountName;
        /**
         * Số dư mục tiêu mà user muốn điều chỉnh về (actual balance)
         */
        private BigDecimal targetBalance;
        private String note;
    }
}
