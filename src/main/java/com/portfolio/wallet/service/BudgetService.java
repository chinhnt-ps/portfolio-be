package com.portfolio.wallet.service;

import com.portfolio.common.exception.BusinessException;
import com.portfolio.common.exception.ConflictException;
import com.portfolio.common.exception.NotFoundException;
import com.portfolio.wallet.dto.request.CreateBudgetRequest;
import com.portfolio.wallet.dto.request.UpdateBudgetRequest;
import com.portfolio.wallet.dto.response.BudgetResponse;
import com.portfolio.wallet.model.Budget;
import com.portfolio.wallet.model.Transaction;
import com.portfolio.wallet.model.TransactionType;
import com.portfolio.wallet.repository.BudgetRepository;
import com.portfolio.wallet.repository.CategoryRepository;
import com.portfolio.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Budget service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {
    
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    
    /**
     * Get all budgets for a user (paginated)
     */
    public Page<BudgetResponse> getAllBudgets(String userId, Pageable pageable) {
        log.debug("Getting all budgets for user: {}", userId);
        Page<Budget> budgets = budgetRepository.findByUserIdAndDeletedFalse(userId, pageable);
        return budgets.map(budget -> {
            updateUsedAmount(budget);
            return BudgetResponse.from(budget);
        });
    }
    
    /**
     * Get all budgets for a user (list)
     */
    public List<BudgetResponse> getAllBudgets(String userId) {
        log.debug("Getting all budgets for user: {}", userId);
        List<Budget> budgets = budgetRepository.findByUserIdAndDeletedFalse(userId);
        return budgets.stream()
                .map(budget -> {
                    updateUsedAmount(budget);
                    return BudgetResponse.from(budget);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get budgets by month
     */
    public List<BudgetResponse> getBudgetsByMonth(String userId, YearMonth month) {
        log.debug("Getting budgets for user: {}, month: {}", userId, month);
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndDeletedFalse(userId, month);
        return budgets.stream()
                .map(budget -> {
                    updateUsedAmount(budget);
                    return BudgetResponse.from(budget);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get budget by id
     */
    public BudgetResponse getBudgetById(String id, String userId) {
        log.debug("Getting budget by id: {} for user: {}", id, userId);
        Budget budget = budgetRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Budget not found"));
        updateUsedAmount(budget);
        return BudgetResponse.from(budget);
    }
    
    /**
     * Create a new budget
     */
    @Transactional
    public BudgetResponse createBudget(CreateBudgetRequest request, String userId) {
        log.debug("Creating budget for user: {}", userId);
        
        // Validate category if provided
        if (request.getCategoryId() != null) {
            if (!categoryRepository.existsByIdAndDeletedFalse(request.getCategoryId())) {
                throw new NotFoundException("Category not found");
            }
        }
        
        // Check if budget already exists for this month and category
        if (budgetRepository.existsByUserIdAndMonthAndCategoryIdAndDeletedFalse(
                userId, request.getMonth(), request.getCategoryId())) {
            throw new ConflictException("Budget already exists for this month and category");
        }
        
        // For total budget (categoryId is null), ensure only one exists per month
        if (request.getCategoryId() == null) {
            budgetRepository.findByUserIdAndMonthAndCategoryIdIsNullAndDeletedFalse(userId, request.getMonth())
                    .ifPresent(b -> {
                        throw new ConflictException("Total budget already exists for this month");
                    });
        }
        
        Budget budget = Budget.builder()
                .userId(userId)
                .month(request.getMonth())
                .categoryId(request.getCategoryId())
                .amount(request.getAmount())
                .usedAmount(BigDecimal.ZERO)
                .deleted(false)
                .build();
        
        Budget saved = budgetRepository.save(budget);
        updateUsedAmount(saved);
        log.info("Budget created successfully: {}", saved.getId());
        return BudgetResponse.from(saved);
    }
    
    /**
     * Update a budget
     */
    @Transactional
    public BudgetResponse updateBudget(String id, UpdateBudgetRequest request, String userId) {
        log.debug("Updating budget: {} for user: {}", id, userId);
        
        Budget budget = budgetRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Budget not found"));
        
        // Validate category if provided
        if (request.getCategoryId() != null && !request.getCategoryId().equals(budget.getCategoryId())) {
            if (!categoryRepository.existsByIdAndDeletedFalse(request.getCategoryId())) {
                throw new NotFoundException("Category not found");
            }
        }
        
        // Check for conflicts if month or categoryId is being changed
        if (request.getMonth() != null && !request.getMonth().equals(budget.getMonth())) {
            String categoryId = request.getCategoryId() != null ? request.getCategoryId() : budget.getCategoryId();
            if (budgetRepository.existsByUserIdAndMonthAndCategoryIdAndDeletedFalse(
                    userId, request.getMonth(), categoryId)) {
                throw new ConflictException("Budget already exists for this month and category");
            }
        }
        
        // Update fields if provided
        if (request.getMonth() != null) {
            budget.setMonth(request.getMonth());
        }
        if (request.getCategoryId() != null) {
            budget.setCategoryId(request.getCategoryId());
        }
        if (request.getAmount() != null) {
            budget.setAmount(request.getAmount());
        }
        
        Budget updated = budgetRepository.save(budget);
        updateUsedAmount(updated);
        log.info("Budget updated successfully: {}", updated.getId());
        return BudgetResponse.from(updated);
    }
    
    /**
     * Delete a budget (soft delete)
     */
    @Transactional
    public void deleteBudget(String id, String userId) {
        log.debug("Deleting budget: {} for user: {}", id, userId);
        
        Budget budget = budgetRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Budget not found"));
        
        // Soft delete
        budget.setDeleted(true);
        budgetRepository.save(budget);
        log.info("Budget deleted successfully: {}", id);
    }
    
    /**
     * Update used amount for a budget based on transactions
     */
    private void updateUsedAmount(Budget budget) {
        if (budget.getMonth() == null) {
            log.warn("Budget {} has null month, skipping used amount calculation", budget.getId());
            budget.setUsedAmount(BigDecimal.ZERO);
            return;
        }
        
        YearMonth month = budget.getMonth();
        LocalDateTime startDate = month.atDay(1).atStartOfDay();
        LocalDateTime endDate = month.atEndOfMonth().atTime(23, 59, 59);
        
        List<Transaction> transactions;
        
        if (budget.getCategoryId() == null) {
            // Total budget: sum all expense transactions for the month
            transactions = transactionRepository.findByUserIdAndOccurredAtBetweenAndDeletedFalse(
                    budget.getUserId(), startDate, endDate);
            BigDecimal usedAmount = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            budget.setUsedAmount(usedAmount);
        } else {
            // Category budget: sum expense transactions for the category in the month
            transactions = transactionRepository.findByUserIdAndOccurredAtBetweenAndDeletedFalse(
                    budget.getUserId(), startDate, endDate);
            BigDecimal usedAmount = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE
                            && budget.getCategoryId().equals(t.getCategoryId()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            budget.setUsedAmount(usedAmount);
        }
        
        // Save updated used amount
        budgetRepository.save(budget);
    }
}
