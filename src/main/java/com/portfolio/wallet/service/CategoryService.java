package com.portfolio.wallet.service;

import com.portfolio.common.exception.ConflictException;
import com.portfolio.common.exception.NotFoundException;
import com.portfolio.wallet.dto.request.CreateCategoryRequest;
import com.portfolio.wallet.dto.request.UpdateCategoryRequest;
import com.portfolio.wallet.dto.response.CategoryResponse;
import com.portfolio.wallet.model.Category;
import com.portfolio.wallet.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Category service
 * 
 * Handles category CRUD operations and default categories seeding
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    /**
     * Default categories for new users
     */
    private static final List<DefaultCategory> DEFAULT_CATEGORIES = List.of(
            // Expense categories
            new DefaultCategory("Ăn uống", "Utensils", "#FF5733"),
            new DefaultCategory("Di chuyển", "Car", "#3498DB"),
            new DefaultCategory("Mua sắm", "ShoppingBag", "#9B59B6"),
            new DefaultCategory("Giải trí", "Film", "#E74C3C"),
            new DefaultCategory("Sức khỏe", "Heart", "#1ABC9C"),
            new DefaultCategory("Giáo dục", "Book", "#F39C12"),
            new DefaultCategory("Hóa đơn", "FileText", "#34495E"),
            new DefaultCategory("Khác", "MoreHorizontal", "#95A5A6"),
            // Income categories
            new DefaultCategory("Lương", "DollarSign", "#27AE60"),
            new DefaultCategory("Thưởng", "Gift", "#E67E22"),
            new DefaultCategory("Đầu tư", "TrendingUp", "#16A085"),
            new DefaultCategory("Thu nhập khác", "PlusCircle", "#2ECC71")
    );
    
    private record DefaultCategory(String name, String icon, String color) {}
    
    /**
     * Initialize default categories if they don't exist
     */
    @PostConstruct
    public void initDefaultCategories() {
        log.info("Checking default categories...");
        List<Category> existingSystemCategories = categoryRepository.findByIsSystemTrueAndDeletedFalse();
        
        if (existingSystemCategories.isEmpty()) {
            log.info("Creating default system categories...");
            for (DefaultCategory defaultCat : DEFAULT_CATEGORIES) {
                Category category = Category.builder()
                        .userId(null) // System category
                        .name(defaultCat.name())
                        .icon(defaultCat.icon())
                        .color(defaultCat.color())
                        .isSystem(true)
                        .deleted(false)
                        .build();
                categoryRepository.save(category);
            }
            log.info("Default categories created successfully");
        } else {
            log.info("Default categories already exist");
        }
    }
    
    /**
     * Get all categories for a user (user categories + system categories)
     */
    public List<CategoryResponse> getAllCategories(String userId) {
        log.debug("Getting all categories for user: {}", userId);
        
        // Get all categories (user + system)
        List<Category> categories = categoryRepository.findAllCategoriesForUser(userId);
        
        // Convert to response
        return categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get category by id
     */
    public CategoryResponse getCategoryById(String id) {
        log.debug("Getting category by id: {}", id);
        Category category = categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        return CategoryResponse.from(category);
    }
    
    /**
     * Create a new category
     */
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request, String userId) {
        log.debug("Creating category for user: {}", userId);
        
        // Check for duplicate name
        categoryRepository.findByNameAndUserIdAndDeletedFalse(request.getName(), userId)
                .ifPresent(c -> {
                    throw new ConflictException("Category with this name already exists");
                });
        
        Category category = Category.builder()
                .userId(userId)
                .name(request.getName())
                .icon(request.getIcon())
                .color(request.getColor())
                .isSystem(false)
                .deleted(false)
                .build();
        
        Category saved = categoryRepository.save(category);
        log.info("Category created successfully: {}", saved.getId());
        return CategoryResponse.from(saved);
    }
    
    /**
     * Update a category
     */
    @Transactional
    public CategoryResponse updateCategory(String id, UpdateCategoryRequest request, String userId) {
        log.debug("Updating category: {} for user: {}", id, userId);
        
        Category category = categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        
        // Check if user owns this category (not system category)
        if (category.getIsSystem()) {
            throw new ConflictException("Cannot update system category");
        }
        
        if (!category.getUserId().equals(userId)) {
            throw new NotFoundException("Category not found");
        }
        
        // Check for duplicate name if name is being updated
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            categoryRepository.findByNameAndUserIdAndDeletedFalse(request.getName(), userId)
                    .ifPresent(c -> {
                        throw new ConflictException("Category with this name already exists");
                    });
        }
        
        // Update fields if provided
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }
        
        Category updated = categoryRepository.save(category);
        log.info("Category updated successfully: {}", updated.getId());
        return CategoryResponse.from(updated);
    }
    
    /**
     * Delete a category (soft delete)
     */
    @Transactional
    public void deleteCategory(String id, String userId) {
        log.debug("Deleting category: {} for user: {}", id, userId);
        
        Category category = categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        
        // Check if user owns this category (not system category)
        if (category.getIsSystem()) {
            throw new ConflictException("Cannot delete system category");
        }
        
        if (!category.getUserId().equals(userId)) {
            throw new NotFoundException("Category not found");
        }
        
        // Soft delete
        category.setDeleted(true);
        categoryRepository.save(category);
        log.info("Category deleted successfully: {}", id);
    }
}
