package com.portfolio.common.util;

import com.portfolio.common.dto.ApiResponse;
import com.portfolio.common.exception.ValidationException;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for file validation
 */
public class FileValidator {
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    // Supported image MIME types
    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );
    
    // Supported document MIME types
    private static final Set<String> DOCUMENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    
    // Supported file extensions
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of("pdf", "doc", "docx");
    
    private static final Set<String> ALLOWED_TYPES;
    private static final Set<String> ALLOWED_EXTENSIONS;
    
    static {
        ALLOWED_TYPES = new HashSet<>();
        ALLOWED_TYPES.addAll(IMAGE_TYPES);
        ALLOWED_TYPES.addAll(DOCUMENT_TYPES);
        
        ALLOWED_EXTENSIONS = new HashSet<>();
        ALLOWED_EXTENSIONS.addAll(IMAGE_EXTENSIONS);
        ALLOWED_EXTENSIONS.addAll(DOCUMENT_EXTENSIONS);
    }
    
    /**
     * Validate file size and type
     * 
     * @param file File to validate
     * @throws ValidationException if validation fails
     */
    public static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File is required");
        }
        
        List<ApiResponse.FieldError> errors = new ArrayList<>();
        
        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            errors.add(ApiResponse.FieldError.builder()
                    .field("file")
                    .reason("FILE_SIZE_EXCEEDED")
                    .build());
        }
        
        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            errors.add(ApiResponse.FieldError.builder()
                    .field("file")
                    .reason("FILE_TYPE_NOT_ALLOWED")
                    .build());
        }
        
        // Validate extension matches MIME type
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                errors.add(ApiResponse.FieldError.builder()
                        .field("file")
                        .reason("FILE_EXTENSION_NOT_ALLOWED")
                        .build());
            }
            
            // Check extension matches MIME type
            if (contentType != null && !isExtensionMatchesMimeType(extension, contentType)) {
                errors.add(ApiResponse.FieldError.builder()
                        .field("file")
                        .reason("EXTENSION_MIME_TYPE_MISMATCH")
                        .build());
            }
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException("File validation failed", errors);
        }
    }
    
    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
    
    private static boolean isExtensionMatchesMimeType(String extension, String mimeType) {
        if (IMAGE_EXTENSIONS.contains(extension)) {
            return IMAGE_TYPES.contains(mimeType);
        }
        if (DOCUMENT_EXTENSIONS.contains(extension)) {
            return DOCUMENT_TYPES.contains(mimeType);
        }
        return false;
    }
}
