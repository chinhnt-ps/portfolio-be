package com.portfolio.wallet.repository;

import com.portfolio.wallet.model.Liability;
import com.portfolio.wallet.model.LiabilityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Liability repository
 */
@Repository
public interface LiabilityRepository extends MongoRepository<Liability, String> {

    /**
     * Find all liabilities by userId, excluding deleted
     */
    Page<Liability> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    /**
     * Find all liabilities by userId, excluding deleted
     */
    List<Liability> findByUserIdAndDeletedFalse(String userId);

    /**
     * Find liability by id and userId, excluding deleted
     */
    Optional<Liability> findByIdAndUserIdAndDeletedFalse(String id, String userId);

    /**
     * Find liabilities by status, excluding deleted
     */
    List<Liability> findByUserIdAndStatusAndDeletedFalse(String userId, LiabilityStatus status);

    /**
     * Find overdue liabilities (dueAt < now and status != PAID)
     */
    List<Liability> findByUserIdAndDueAtBeforeAndStatusNotAndDeletedFalse(
            String userId,
            LocalDateTime now,
            LiabilityStatus status);

    /**
     * Count liabilities by userId, excluding deleted
     */
    long countByUserIdAndDeletedFalse(String userId);
}
