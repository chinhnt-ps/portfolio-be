package com.portfolio.wallet.controller;

import com.portfolio.common.annotation.RateLimited;
import com.portfolio.common.dto.ApiResponse;
import com.portfolio.wallet.dto.request.CreateTransactionRequest;
import com.portfolio.wallet.dto.request.TransactionFilters;
import com.portfolio.wallet.dto.request.UpdateTransactionRequest;
import com.portfolio.wallet.dto.response.TransactionResponse;
import com.portfolio.wallet.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Transaction controller
 * 
 * Endpoints:
 * - GET /api/v1/wallet/transactions - List transactions (with filters, pagination, sort)
 * - POST /api/v1/wallet/transactions - Create transaction
 * - GET /api/v1/wallet/transactions/{id} - Get transaction
 * - PUT /api/v1/wallet/transactions/{id} - Update transaction
 * - DELETE /api/v1/wallet/transactions/{id} - Delete transaction (soft delete)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/wallet/transactions")
@RequiredArgsConstructor
public class TransactionController {
    
    private final TransactionService transactionService;
    
    /**
     * Get all transactions for the authenticated user with filters
     */
    @GetMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAllTransactions(
            Authentication authentication,
            @ModelAttribute TransactionFilters filters,
            @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String userId = authentication.getName();
        Page<TransactionResponse> transactions = transactionService.getAllTransactions(userId, filters, pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions, "Transactions retrieved successfully"));
    }
    
    /**
     * Get transaction by id
     */
    @GetMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        TransactionResponse transaction = transactionService.getTransactionById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(transaction, "Transaction retrieved successfully"));
    }
    
    /**
     * Create a new transaction
     */
    @PostMapping
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        TransactionResponse transaction = transactionService.createTransaction(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(transaction, "Transaction created successfully"));
    }
    
    /**
     * Update a transaction
     */
    @PutMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @PathVariable String id,
            @Valid @RequestBody UpdateTransactionRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        TransactionResponse transaction = transactionService.updateTransaction(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success(transaction, "Transaction updated successfully"));
    }
    
    /**
     * Delete a transaction (soft delete)
     */
    @DeleteMapping("/{id}")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        transactionService.deleteTransaction(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Transaction deleted successfully"));
    }
}
