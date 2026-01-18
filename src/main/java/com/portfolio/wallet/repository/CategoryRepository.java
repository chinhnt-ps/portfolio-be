package com.portfolio.wallet.repository;

import com.portfolio.wallet.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Category repository
 */
@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    
    /**
     * Find all categories by userId (user categories) and system categories, excluding deleted
     * Note: This query finds user categories OR system categories
     */
    @org.springframework.data.mongodb.repository.Query("{'$or': [{'userId': ?0, 'deleted': false}, {'isSystem': true, 'deleted': false}]}")
    List<Category> findAllCategoriesForUser(String userId);
    
    /**
     * Find all user categories, excluding deleted
     */
    List<Category> findByUserIdAndDeletedFalse(String userId);
    
    /**
     * Find all system categories, excluding deleted
     */
    List<Category> findByIsSystemTrueAndDeletedFalse();
    
    /**
     * Find category by id and userId or system category, excluding deleted
     */
    Optional<Category> findByIdAndDeletedFalse(String id);
    
    /**
     * Check if category exists by id, excluding deleted
     */
    boolean existsByIdAndDeletedFalse(String id);
    
    /**
     * Find category by name and userId (for duplicate check)
     */
    Optional<Category> findByNameAndUserIdAndDeletedFalse(String name, String userId);
}
