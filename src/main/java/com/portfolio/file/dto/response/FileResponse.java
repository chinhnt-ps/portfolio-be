package com.portfolio.file.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * File response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    
    private String fileId;
    private String originalName;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String folder;
    private String publicUrl;
    private LocalDateTime uploadedAt;
}
