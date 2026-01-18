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
 * Liability entity (Nợ)
 * 
 * Represents money that the user owes to others
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "liabilities")
public class Liability {

    @Id
    private String id;

    @Indexed
    private String userId; // User ID từ JWT token

    private String counterpartyName; // Tên người cho vay (e.g., "Nguyen Van A")

    private BigDecimal amount; // Số tiền nợ

    private String currency; // Currency (default: VND)

    private LocalDateTime occurredAt; // Ngày vay

    private LocalDateTime dueAt; // Ngày đáo hạn (optional)

    private LiabilityStatus status; // Status: OPEN, PARTIALLY_PAID, PAID, OVERDUE

    private BigDecimal paidAmount; // Tổng số tiền đã thanh toán (tính từ settlements)

    private String note; // Ghi chú

    @Builder.Default
    private Boolean deleted = false; // Soft delete

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
