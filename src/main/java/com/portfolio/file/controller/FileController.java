package com.portfolio.file.controller;

import com.portfolio.common.annotation.RateLimited;
import com.portfolio.common.dto.ApiResponse;
import com.portfolio.file.dto.response.FileResponse;
import com.portfolio.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * File controller với các endpoints upload, get, delete file
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    /**
     * Upload file lên Firebase Storage
     * Chỉ ADMIN mới có quyền upload file
     * 
     * POST /api/v1/files/upload
     * Content-Type: multipart/form-data
     * 
     * @param file MultipartFile từ request
     * @param folder Optional folder path (default: "uploads")
     * @param authentication Authentication từ JWT filter
     * @return FileResponse với file info và public URL
     */
    @PostMapping("/upload")
    @RateLimited(RateLimited.RateLimitType.FILE_UPLOAD)
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "uploads") String folder,
            Authentication authentication) {
        
        // Get userId từ JWT token
        String userId = authentication.getName(); // JWT token chứa userId trong subject
        
        log.info("Upload file request. UserId: {}, Folder: {}, OriginalName: {}", 
                userId, folder, file.getOriginalFilename());
        
        FileResponse response = fileService.uploadFile(file, folder, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Upload thành công"));
    }
    
    /**
     * Get file metadata theo fileId
     * 
     * GET /api/v1/files/{fileId}
     * 
     * @param fileId File ID (UUID)
     * @return FileResponse
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse<FileResponse>> getFile(@PathVariable String fileId) {
        log.info("Get file request. FileId: {}", fileId);
        
        FileResponse response = fileService.getFile(fileId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Delete file từ Firebase Storage và MongoDB
     * 
     * DELETE /api/v1/files/{fileId}
     * 
     * @param fileId File ID (UUID)
     * @param authentication Authentication từ JWT filter
     * @return Success message
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable String fileId,
            Authentication authentication) {
        
        // Get userId từ JWT token
        String userId = authentication.getName();
        
        log.info("Delete file request. FileId: {}, UserId: {}", fileId, userId);
        
        fileService.deleteFile(fileId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa file thành công"));
    }
}
