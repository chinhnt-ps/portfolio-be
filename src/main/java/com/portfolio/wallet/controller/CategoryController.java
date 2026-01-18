package com.portfolio.wallet.controller;

import com.portfolio.common.annotation.RateLimited;
import com.portfolio.common.dto.ApiResponse;
import com.portfolio.wallet.dto.request.CreateCategoryRequest;
import com.portfolio.wallet.dto.request.UpdateCategoryRequest;
import com.portfolio.wallet.dto.response.CategoryResponse;
import com.portfolio.wallet.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Category controller
 * 
 * Endpoints:
 * - GET /api/v1/wallet/categories - List all categories (user + system)
 * - POST /api/v1/wallet/categories - Create category
 * - GET /api/v1/wallet/categories/{id} - Get category
 * - PUT /api/v1/wallet/categories/{id} - Update category
 * - DELETE /api/v1/wallet/categories/{id} - Delete category (soft delete)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/wallet/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * Get all categories for the authenticated user (user categories + system categories)
     */
    @GetMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(
            Authentication authentication) {
        String userId = authentication.getName();
        List<CategoryResponse> categories = categoryService.getAllCategories(userId);
        return ResponseEntity.ok(ApiResponse.success(categories, "Categories retrieved successfully"));
    }
    
    /**
     * Get category by id
     */
    @GetMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable String id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category, "Category retrieved successfully"));
    }
    
    /**
     * Create a new category
     */
    @PostMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        CategoryResponse category = categoryService.createCategory(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(category, "Category created successfully"));
    }
    
    /**
     * Update a category
     */
    @PutMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable String id,
            @Valid @RequestBody UpdateCategoryRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        CategoryResponse category = categoryService.updateCategory(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success(category, "Category updated successfully"));
    }
    
    /**
     * Delete a category (soft delete)
     */
    @DeleteMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        categoryService.deleteCategory(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }
}
