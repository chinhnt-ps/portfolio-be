package com.portfolio.wallet.repository;

import com.portfolio.wallet.model.Transaction;
import com.portfolio.wallet.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Transaction repository
 */
@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    
    /**
     * Find all transactions by userId, excluding deleted
     */
    Page<Transaction> findByUserIdAndDeletedFalse(String userId, Pageable pageable);
    
    /**
     * Find all transactions by userId, excluding deleted
     */
    List<Transaction> findByUserIdAndDeletedFalse(String userId);
    
    /**
     * Find transaction by id and userId, excluding deleted
     */
    Optional<Transaction> findByIdAndUserIdAndDeletedFalse(String id, String userId);
    
    // Note: Complex filtering is handled in TransactionService using MongoTemplate
    
    /**
     * Find transactions by accountId, excluding deleted
     */
    List<Transaction> findByAccountIdAndDeletedFalse(String accountId);
    
    /**
     * Find transactions by fromAccountId, excluding deleted
     */
    List<Transaction> findByFromAccountIdAndDeletedFalse(String fromAccountId);
    
    /**
     * Find transactions by toAccountId, excluding deleted
     */
    List<Transaction> findByToAccountIdAndDeletedFalse(String toAccountId);
    
    /**
     * Find transactions by categoryId, excluding deleted
     */
    List<Transaction> findByCategoryIdAndDeletedFalse(String categoryId);
    
    /**
     * Find transactions by date range, excluding deleted
     */
    List<Transaction> findByUserIdAndOccurredAtBetweenAndDeletedFalse(
            String userId, LocalDateTime start, LocalDateTime end);
    
    /**
     * Count transactions by userId, excluding deleted
     */
    long countByUserIdAndDeletedFalse(String userId);
}
