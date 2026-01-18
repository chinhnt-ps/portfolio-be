package com.portfolio.wallet.controller;

import com.portfolio.common.annotation.RateLimited;
import com.portfolio.common.dto.ApiResponse;
import com.portfolio.wallet.dto.request.CreateBudgetRequest;
import com.portfolio.wallet.dto.request.UpdateBudgetRequest;
import com.portfolio.wallet.dto.response.BudgetResponse;
import com.portfolio.wallet.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

/**
 * Budget controller
 * 
 * Endpoints:
 * - GET /api/v1/wallet/budgets - List budgets (paginated)
 * - GET /api/v1/wallet/budgets/month?month=2025-01 - Get budgets by month
 * - POST /api/v1/wallet/budgets - Create budget
 * - GET /api/v1/wallet/budgets/{id} - Get budget
 * - PUT /api/v1/wallet/budgets/{id} - Update budget
 * - DELETE /api/v1/wallet/budgets/{id} - Delete budget (soft delete)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/wallet/budgets")
@RequiredArgsConstructor
public class BudgetController {
    
    private final BudgetService budgetService;
    
    /**
     * Get all budgets for the authenticated user
     */
    @GetMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Page<BudgetResponse>>> getAllBudgets(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        String userId = authentication.getName();
        Page<BudgetResponse> budgets = budgetService.getAllBudgets(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(budgets, "Budgets retrieved successfully"));
    }
    
    /**
     * Get budgets by month
     */
    @GetMapping("/month")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgetsByMonth(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            Authentication authentication) {
        String userId = authentication.getName();
        List<BudgetResponse> budgets = budgetService.getBudgetsByMonth(userId, month);
        return ResponseEntity.ok(ApiResponse.success(budgets, "Budgets retrieved successfully"));
    }
    
    /**
     * Get budget by id
     */
    @GetMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        BudgetResponse budget = budgetService.getBudgetById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(budget, "Budget retrieved successfully"));
    }
    
    /**
     * Create a new budget
     */
    @PostMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @Valid @RequestBody CreateBudgetRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        BudgetResponse budget = budgetService.createBudget(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(budget, "Budget created successfully"));
    }
    
    /**
     * Update a budget
     */
    @PutMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @PathVariable String id,
            @Valid @RequestBody UpdateBudgetRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        BudgetResponse budget = budgetService.updateBudget(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success(budget, "Budget updated successfully"));
    }
    
    /**
     * Delete a budget (soft delete)
     */
    @DeleteMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        budgetService.deleteBudget(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Budget deleted successfully"));
    }
}
