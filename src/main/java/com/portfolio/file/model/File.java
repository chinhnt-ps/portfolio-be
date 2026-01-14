package com.portfolio.file.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * File metadata document trong MongoDB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "files")
public class File {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String fileId; // UUID
    
    @Indexed
    private String userId; // User ID từ JWT token
    
    private String originalName; // Tên file gốc từ user
    
    private String fileName; // Tên file unique trên Firebase Storage
    
    private Long fileSize; // Size in bytes
    
    private String mimeType; // MIME type (image/jpeg, application/pdf, etc.)
    
    private String folder; // Folder path trên Firebase Storage (default: "uploads")
    
    private String publicUrl; // Public URL từ Firebase Storage
    
    private String firebaseStoragePath; // Full path trên Firebase Storage
    
    @Indexed
    private LocalDateTime uploadedAt; // Timestamp khi upload
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
