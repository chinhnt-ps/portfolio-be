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
 * Receivable entity (Cho vay)
 * 
 * Represents money that others owe to the user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "receivables")
public class Receivable {

    @Id
    private String id;

    @Indexed
    private String userId; // User ID từ JWT token

    private String counterpartyName; // Tên người nợ (e.g., "Nguyen Van A")

    private BigDecimal amount; // Số tiền cho vay

    private String currency; // Currency (default: VND)

    private LocalDateTime occurredAt; // Ngày cho vay

    private LocalDateTime dueAt; // Ngày đáo hạn (optional)

    private ReceivableStatus status; // Status: OPEN, PARTIALLY_PAID, PAID, OVERDUE

    private BigDecimal paidAmount; // Tổng số tiền đã thanh toán (tính từ settlements)

    private String note; // Ghi chú

    @Builder.Default
    private Boolean deleted = false; // Soft delete

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
