package com.portfolio.file.service;

import com.portfolio.common.exception.NotFoundException;
import com.portfolio.common.exception.UnauthorizedException;
import com.portfolio.common.util.FileValidator;
import com.portfolio.file.dto.response.FileResponse;
import com.portfolio.file.model.File;
import com.portfolio.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * File service với business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    
    private final FileRepository fileRepository;
    private final FirebaseStorageService firebaseStorageService;
    
    private static final String DEFAULT_FOLDER = "uploads";
    
    /**
     * Upload file lên Firebase Storage và lưu metadata vào MongoDB
     * 
     * @param file MultipartFile từ request
     * @param folder Optional folder path (default: "uploads")
     * @param userId User ID từ JWT token
     * @return FileResponse với file info và public URL
     */
    @Transactional
    public FileResponse uploadFile(MultipartFile file, String folder, String userId) {
        // Validate file
        FileValidator.validateFile(file);
        
        // Use default folder if not provided
        String uploadFolder = (folder != null && !folder.isEmpty()) ? folder : DEFAULT_FOLDER;
        
        try {
            // Upload to Firebase Storage
            String storagePath = firebaseStorageService.uploadFile(file, uploadFolder);
            
            // Get public URL
            String publicUrl = firebaseStorageService.getPublicUrl(storagePath);
            
            // Generate fileId (UUID)
            String fileId = UUID.randomUUID().toString();
            
            // Create file metadata
            File fileMetadata = File.builder()
                    .fileId(fileId)
                    .userId(userId)
                    .originalName(file.getOriginalFilename())
                    .fileName(storagePath.substring(storagePath.lastIndexOf("/") + 1))
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .folder(uploadFolder)
                    .publicUrl(publicUrl)
                    .firebaseStoragePath(storagePath)
                    .uploadedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            // Save to MongoDB
            fileMetadata = fileRepository.save(fileMetadata);
            
            log.info("File uploaded successfully. FileId: {}, UserId: {}, Path: {}", 
                    fileId, userId, storagePath);
            
            // Return response
            return FileResponse.builder()
                    .fileId(fileMetadata.getFileId())
                    .originalName(fileMetadata.getOriginalName())
                    .fileName(fileMetadata.getFileName())
                    .fileSize(fileMetadata.getFileSize())
                    .mimeType(fileMetadata.getMimeType())
                    .folder(fileMetadata.getFolder())
                    .publicUrl(fileMetadata.getPublicUrl())
                    .uploadedAt(fileMetadata.getUploadedAt())
                    .build();
                    
        } catch (IOException e) {
            log.error("Failed to upload file", e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get file metadata theo fileId
     * 
     * @param fileId File ID (UUID)
     * @return FileResponse
     */
    public FileResponse getFile(String fileId) {
        File file = fileRepository.findByFileId(fileId)
                .orElseThrow(() -> new NotFoundException("File not found with id: " + fileId));
        
        return FileResponse.builder()
                .fileId(file.getFileId())
                .originalName(file.getOriginalName())
                .fileName(file.getFileName())
                .fileSize(file.getFileSize())
                .mimeType(file.getMimeType())
                .folder(file.getFolder())
                .publicUrl(file.getPublicUrl())
                .uploadedAt(file.getUploadedAt())
                .build();
    }
    
    /**
     * Delete file từ Firebase Storage và MongoDB
     * 
     * @param fileId File ID (UUID)
     * @param userId User ID để verify ownership
     */
    @Transactional
    public void deleteFile(String fileId, String userId) {
        File file = fileRepository.findByFileId(fileId)
                .orElseThrow(() -> new NotFoundException("File not found with id: " + fileId));
        
        // Verify ownership
        if (!file.getUserId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to delete this file");
        }
        
        // Delete from Firebase Storage
        boolean deleted = firebaseStorageService.deleteFile(file.getFirebaseStoragePath());
        
        if (!deleted) {
            log.warn("File not found in Firebase Storage, but continuing with MongoDB deletion: {}", 
                    file.getFirebaseStoragePath());
        }
        
        // Delete from MongoDB
        fileRepository.deleteByFileId(fileId);
        
        log.info("File deleted successfully. FileId: {}, UserId: {}", fileId, userId);
    }
}
