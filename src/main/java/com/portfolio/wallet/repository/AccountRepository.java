package com.portfolio.wallet.repository;

import com.portfolio.wallet.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Account repository
 */
@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    
    /**
     * Find all accounts by userId, excluding deleted
     */
    Page<Account> findByUserIdAndDeletedFalse(String userId, Pageable pageable);
    
    /**
     * Find all accounts by userId, excluding deleted
     */
    List<Account> findByUserIdAndDeletedFalse(String userId);
    
    /**
     * Find account by id and userId, excluding deleted
     */
    Optional<Account> findByIdAndUserIdAndDeletedFalse(String id, String userId);
    
    /**
     * Check if account exists by id and userId, excluding deleted
     */
    boolean existsByIdAndUserIdAndDeletedFalse(String id, String userId);
    
    /**
     * Count accounts by userId, excluding deleted
     */
    long countByUserIdAndDeletedFalse(String userId);
}
