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

import java.time.LocalDateTime;

/**
 * Category entity
 * 
 * Represents a category for transactions (expense/income)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "categories")
public class Category {
    
    @Id
    private String id;
    
    @Indexed
    private String userId; // User ID từ JWT token (null nếu là system category)
    
    private String name; // Tên danh mục
    
    private String icon; // Icon name (optional)
    
    private String color; // Color hex (optional)
    
    @Builder.Default
    private Boolean isSystem = false; // Danh mục hệ thống (không thể xóa)
    
    @Builder.Default
    private Boolean deleted = false; // Soft delete
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
