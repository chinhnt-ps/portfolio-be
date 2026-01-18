package com.portfolio.wallet.repository;

import com.portfolio.wallet.model.Asset;
import com.portfolio.wallet.model.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Asset repository
 */
@Repository
public interface AssetRepository extends MongoRepository<Asset, String> {

    /**
     * Find all assets by userId, excluding deleted
     */
    Page<Asset> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    /**
     * Find all assets by userId, excluding deleted
     */
    List<Asset> findByUserIdAndDeletedFalse(String userId);

    /**
     * Find asset by id and userId, excluding deleted
     */
    Optional<Asset> findByIdAndUserIdAndDeletedFalse(String id, String userId);

    /**
     * Find assets by type, excluding deleted
     */
    List<Asset> findByUserIdAndTypeAndDeletedFalse(String userId, AssetType type);

    /**
     * Count assets by userId, excluding deleted
     */
    long countByUserIdAndDeletedFalse(String userId);
}
