package com.portfolio.wallet.repository;

import com.portfolio.wallet.model.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Budget repository
 */
@Repository
public interface BudgetRepository extends MongoRepository<Budget, String> {
    
    /**
     * Find all budgets by userId, excluding deleted
     */
    Page<Budget> findByUserIdAndDeletedFalse(String userId, Pageable pageable);
    
    /**
     * Find all budgets by userId, excluding deleted
     */
    List<Budget> findByUserIdAndDeletedFalse(String userId);
    
    /**
     * Find budget by id and userId, excluding deleted
     */
    Optional<Budget> findByIdAndUserIdAndDeletedFalse(String id, String userId);
    
    /**
     * Find budget by userId, month, and categoryId (null for total), excluding deleted
     */
    Optional<Budget> findByUserIdAndMonthAndCategoryIdAndDeletedFalse(
            String userId, YearMonth month, String categoryId);
    
    /**
     * Find budgets by userId and month, excluding deleted
     */
    List<Budget> findByUserIdAndMonthAndDeletedFalse(String userId, YearMonth month);
    
    /**
     * Find total budget (categoryId is null) by userId and month, excluding deleted
     */
    Optional<Budget> findByUserIdAndMonthAndCategoryIdIsNullAndDeletedFalse(
            String userId, YearMonth month);
    
    /**
     * Check if budget exists by userId, month, and categoryId, excluding deleted
     */
    boolean existsByUserIdAndMonthAndCategoryIdAndDeletedFalse(
            String userId, YearMonth month, String categoryId);
}
