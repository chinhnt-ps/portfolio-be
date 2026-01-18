package com.portfolio.wallet.repository;

import com.portfolio.wallet.model.Settlement;
import com.portfolio.wallet.model.SettlementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Settlement repository
 */
@Repository
public interface SettlementRepository extends MongoRepository<Settlement, String> {

    /**
     * Find all settlements by userId, excluding deleted
     */
    Page<Settlement> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    /**
     * Find all settlements by userId, excluding deleted
     */
    List<Settlement> findByUserIdAndDeletedFalse(String userId);

    /**
     * Find settlement by id and userId, excluding deleted
     */
    Optional<Settlement> findByIdAndUserIdAndDeletedFalse(String id, String userId);

    /**
     * Find all settlements for a receivable, excluding deleted
     */
    List<Settlement> findByReceivableIdAndDeletedFalse(String receivableId);

    /**
     * Find all settlements for a liability, excluding deleted
     */
    List<Settlement> findByLiabilityIdAndDeletedFalse(String liabilityId);

    /**
     * Find settlements by type, excluding deleted
     */
    List<Settlement> findByUserIdAndTypeAndDeletedFalse(String userId, SettlementType type);

    /**
     * Count settlements by userId, excluding deleted
     */
    long countByUserIdAndDeletedFalse(String userId);
}
