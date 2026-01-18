package com.portfolio.wallet.repository;

import com.portfolio.wallet.model.Receivable;
import com.portfolio.wallet.model.ReceivableStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Receivable repository
 */
@Repository
public interface ReceivableRepository extends MongoRepository<Receivable, String> {

    /**
     * Find all receivables by userId, excluding deleted
     */
    Page<Receivable> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    /**
     * Find all receivables by userId, excluding deleted
     */
    List<Receivable> findByUserIdAndDeletedFalse(String userId);

    /**
     * Find receivable by id and userId, excluding deleted
     */
    Optional<Receivable> findByIdAndUserIdAndDeletedFalse(String id, String userId);

    /**
     * Find receivables by status, excluding deleted
     */
    List<Receivable> findByUserIdAndStatusAndDeletedFalse(String userId, ReceivableStatus status);

    /**
     * Find overdue receivables (dueAt < now and status != PAID)
     */
    List<Receivable> findByUserIdAndDueAtBeforeAndStatusNotAndDeletedFalse(
            String userId,
            LocalDateTime now,
            ReceivableStatus status);

    /**
     * Count receivables by userId, excluding deleted
     */
    long countByUserIdAndDeletedFalse(String userId);
}
