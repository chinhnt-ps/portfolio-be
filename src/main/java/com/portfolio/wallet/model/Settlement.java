package com.portfolio.wallet.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Settlement entity
 * 
 * Represents a payment made for a Receivable or Liability
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "settlements")
public class Settlement {

    @Id
    private String id;

    @Indexed
    private String userId; // User ID từ JWT token

    private SettlementType type; // RECEIVABLE or LIABILITY

    private String receivableId; // ID of Receivable (if type is RECEIVABLE)

    private String liabilityId; // ID of Liability (if type is LIABILITY)

    /**
     * ID của transaction gốc tạo ra settlement này (nếu có)
     */
    private String transactionId;
    
    /**
     * Tài khoản dùng để thanh toán (optional)
     * Nếu có, khi tạo Settlement sẽ cập nhật account balance:
     * - RECEIVABLE: cộng amount vào account (nhận tiền)
     * - LIABILITY: trừ amount khỏi account (trả nợ)
     */
    private String accountId;
    
    private BigDecimal amount; // Settlement amount

    private String currency; // Currency (default: VND)

    private LocalDateTime occurredAt; // When the settlement occurred

    private String note; // Ghi chú

    @Builder.Default
    private Boolean deleted = false; // Soft delete

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
