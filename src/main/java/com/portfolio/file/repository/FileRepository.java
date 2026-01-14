package com.portfolio.file.repository;

import com.portfolio.file.model.File;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho File metadata
 */
@Repository
public interface FileRepository extends MongoRepository<File, String> {
    
    /**
     * Tìm file theo fileId (UUID)
     */
    Optional<File> findByFileId(String fileId);
    
    /**
     * Tìm tất cả files của một user
     */
    List<File> findByUserId(String userId);
    
    /**
     * Kiểm tra fileId đã tồn tại chưa
     */
    boolean existsByFileId(String fileId);
    
    /**
     * Xóa file theo fileId
     */
    void deleteByFileId(String fileId);
}
