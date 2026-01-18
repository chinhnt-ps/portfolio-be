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
 * Asset entity (Sở hữu)
 * 
 * Represents assets owned by the user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "assets")
public class Asset {

    @Id
    private String id;

    @Indexed
    private String userId; // User ID từ JWT token

    private String name; // Tên tài sản

    private AssetType type; // CASH, ITEM, DEVICE, OTHER

    private BigDecimal estimatedValue; // Giá trị ước tính

    private String currency; // Currency (default: VND)

    private LocalDateTime acquiredAt; // Ngày mua/sở hữu (optional)

    private String note; // Ghi chú

    @Builder.Default
    private Boolean deleted = false; // Soft delete

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
