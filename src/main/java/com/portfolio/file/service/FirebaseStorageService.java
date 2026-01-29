package com.portfolio.file.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Service để upload file lên Firebase Storage và get public URL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseStorageService {
    
    private final StorageClient storageClient;
    
    @Value("${firebase.storage-bucket}")
    private String bucketName;
    
    /**
     * Upload file lên Firebase Storage
     * 
     * @param file File cần upload
     * @param folder Folder path trên Firebase Storage (ví dụ: "uploads")
     * @return Firebase Storage path (ví dụ: "uploads/uuid-timestamp.jpg")
     * @throws IOException nếu upload thất bại
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        try {
            // Generate unique filename: UUID-timestamp.extension
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFileName = generateUniqueFileName(extension);
            
            // Build storage path
            String storagePath = folder != null && !folder.isEmpty() 
                    ? folder + "/" + uniqueFileName 
                    : "uploads/" + uniqueFileName;
            
            log.info("Uploading file to Firebase Storage: {}", storagePath);
            
            // Get Storage instance
            Storage storage = storageClient.bucket().getStorage();
            
            // Create BlobId
            BlobId blobId = BlobId.of(bucketName, storagePath);
            
            // Create BlobInfo với content type
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            
            // Upload file
            storage.create(blobInfo, file.getBytes());
            
            log.info("File uploaded successfully to Firebase Storage: {}", storagePath);
            
            return storagePath;
        } catch (Exception e) {
            log.error("Failed to upload file to Firebase Storage", e);
            throw new IOException("Failed to upload file to Firebase Storage: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get public URL từ Firebase Storage path.
     * Luôn dùng format Firebase Storage (firebasestorage.googleapis.com) để áp dụng Firebase Storage Rules.
     * Không dùng blob.getMediaLink() vì đó là GCS API URL, yêu cầu OAuth2 → anonymous access bị từ chối.
     *
     * @param storagePath Firebase Storage path (vd: uploads/uuid-timestamp.jpg)
     * @return URL public, dùng được khi Firebase Storage Rules cho phép read
     */
    public String getPublicUrl(String storagePath) {
        String encodedPath = storagePath.replace("/", "%2F");
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucketName, encodedPath);
    }
    
    /**
     * Delete file từ Firebase Storage
     * 
     * @param storagePath Firebase Storage path
     * @return true nếu xóa thành công
     */
    public boolean deleteFile(String storagePath) {
        try {
            Storage storage = storageClient.bucket().getStorage();
            BlobId blobId = BlobId.of(bucketName, storagePath);
            boolean deleted = storage.delete(blobId);
            
            if (deleted) {
                log.info("File deleted successfully from Firebase Storage: {}", storagePath);
            } else {
                log.warn("File not found in Firebase Storage: {}", storagePath);
            }
            
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete file from Firebase Storage: {}", storagePath, e);
            return false;
        }
    }
    
    /**
     * Generate unique filename: UUID-timestamp.extension
     */
    private String generateUniqueFileName(String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        long timestamp = System.currentTimeMillis();
        return String.format("%s-%d.%s", uuid, timestamp, extension);
    }
    
    /**
     * Get file extension từ filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}
