package com.portfolio.wallet.service;

import com.portfolio.common.exception.BusinessException;
import com.portfolio.common.exception.NotFoundException;
import com.portfolio.wallet.dto.request.CreateTransactionRequest;
import com.portfolio.wallet.dto.request.TransactionFilters;
import com.portfolio.wallet.dto.request.UpdateTransactionRequest;
import com.portfolio.wallet.dto.response.TransactionResponse;
import com.portfolio.wallet.model.Transaction;
import com.portfolio.wallet.model.TransactionType;
import com.portfolio.wallet.repository.AccountRepository;
import com.portfolio.wallet.repository.CategoryRepository;
import com.portfolio.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final MongoTemplate mongoTemplate;
    
    /**
     * Get all transactions for a user with filters and pagination
     */
    public Page<TransactionResponse> getAllTransactions(
            String userId,
            TransactionFilters filters,
            Pageable pageable) {
        log.debug("Getting transactions for user: {} with filters: {}", userId, filters);
        
        // Build query with filters
        Query query = buildQuery(userId, filters);
        
        // Apply pagination and sorting
        if (pageable.getSort().isUnsorted()) {
            // Default sort by occurredAt desc
            query.with(Sort.by(Sort.Direction.DESC, "occurredAt"));
        } else {
            query.with(pageable.getSort());
        }
        
        query.with(pageable);
        
        // Execute query
        List<Transaction> transactions = mongoTemplate.find(query, Transaction.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Transaction.class);
        
        // Convert to response
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionResponse::from)
                .toList();
        
        // Create Page manually
        return new org.springframework.data.domain.PageImpl<>(
                responses,
                pageable,
                total
        );
    }
    
    /**
     * Build MongoDB query from filters
     */
    private Query buildQuery(String userId, TransactionFilters filters) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("deleted").is(false);
        
        if (filters != null) {
            if (filters.getType() != null) {
                criteria.and("type").is(filters.getType());
            }
            
            if (filters.getCategoryId() != null) {
                criteria.and("categoryId").is(filters.getCategoryId());
            }
            
            if (filters.getAccountId() != null) {
                criteria.orOperator(
                        Criteria.where("accountId").is(filters.getAccountId()),
                        Criteria.where("fromAccountId").is(filters.getAccountId()),
                        Criteria.where("toAccountId").is(filters.getAccountId())
                );
            }
            
            if (filters.getStartDate() != null || filters.getEndDate() != null) {
                Criteria dateCriteria = Criteria.where("occurredAt");
                if (filters.getStartDate() != null) {
                    dateCriteria.gte(filters.getStartDate());
                }
                if (filters.getEndDate() != null) {
                    dateCriteria.lte(filters.getEndDate());
                }
                criteria.andOperator(dateCriteria);
            }
            
            if (filters.getMinAmount() != null) {
                criteria.and("amount").gte(filters.getMinAmount());
            }
            
            if (filters.getMaxAmount() != null) {
                criteria.and("amount").lte(filters.getMaxAmount());
            }
            
            if (filters.getKeyword() != null && !filters.getKeyword().trim().isEmpty()) {
                criteria.and("note").regex(filters.getKeyword().trim(), "i"); // Case-insensitive
            }
        }
        
        return new Query(criteria);
    }
    
    /**
     * Get transaction by id
     */
    public TransactionResponse getTransactionById(String id, String userId) {
        log.debug("Getting transaction by id: {} for user: {}", id, userId);
        Transaction transaction = transactionRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        return TransactionResponse.from(transaction);
    }
    
    /**
     * Create a new transaction
     */
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request, String userId) {
        log.debug("Creating transaction for user: {}", userId);
        
        // Validate transaction based on type
        validateTransactionRequest(request, userId);
        
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .type(request.getType())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .occurredAt(request.getOccurredAt() != null ? request.getOccurredAt() : LocalDateTime.now())
                .categoryId(request.getCategoryId())
                .accountId(request.getAccountId())
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .note(request.getNote())
                .attachmentIds(request.getAttachmentIds())
                .deleted(false)
                .build();
        
        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created successfully: {}", saved.getId());
        return TransactionResponse.from(saved);
    }
    
    /**
     * Update a transaction
     */
    @Transactional
    public TransactionResponse updateTransaction(String id, UpdateTransactionRequest request, String userId) {
        log.debug("Updating transaction: {} for user: {}", id, userId);
        
        Transaction transaction = transactionRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        
        // Validate update request
        validateUpdateRequest(request, userId, transaction);
        
        // Update fields if provided
        if (request.getType() != null) {
            transaction.setType(request.getType());
        }
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getCurrency() != null) {
            transaction.setCurrency(request.getCurrency());
        }
        if (request.getOccurredAt() != null) {
            transaction.setOccurredAt(request.getOccurredAt());
        }
        if (request.getCategoryId() != null) {
            transaction.setCategoryId(request.getCategoryId());
        }
        if (request.getAccountId() != null) {
            transaction.setAccountId(request.getAccountId());
        }
        if (request.getFromAccountId() != null) {
            transaction.setFromAccountId(request.getFromAccountId());
        }
        if (request.getToAccountId() != null) {
            transaction.setToAccountId(request.getToAccountId());
        }
        if (request.getNote() != null) {
            transaction.setNote(request.getNote());
        }
        if (request.getAttachmentIds() != null) {
            transaction.setAttachmentIds(request.getAttachmentIds());
        }
        
        Transaction updated = transactionRepository.save(transaction);
        log.info("Transaction updated successfully: {}", updated.getId());
        return TransactionResponse.from(updated);
    }
    
    /**
     * Delete a transaction (soft delete)
     */
    @Transactional
    public void deleteTransaction(String id, String userId) {
        log.debug("Deleting transaction: {} for user: {}", id, userId);
        
        Transaction transaction = transactionRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
        
        // Soft delete
        transaction.setDeleted(true);
        transactionRepository.save(transaction);
        log.info("Transaction deleted successfully: {}", id);
    }
    
    /**
     * Validate transaction request
     */
    private void validateTransactionRequest(CreateTransactionRequest request, String userId) {
        if (request.getType() == TransactionType.TRANSFER) {
            // TRANSFER requires fromAccountId and toAccountId
            if (request.getFromAccountId() == null || request.getToAccountId() == null) {
                throw new BusinessException("TRANSFER transactions require both fromAccountId and toAccountId");
            }
            if (request.getFromAccountId().equals(request.getToAccountId())) {
                throw new BusinessException("fromAccountId and toAccountId must be different");
            }
            // Validate accounts exist and belong to user
            if (!accountRepository.existsByIdAndUserIdAndDeletedFalse(request.getFromAccountId(), userId)) {
                throw new NotFoundException("From account not found");
            }
            if (!accountRepository.existsByIdAndUserIdAndDeletedFalse(request.getToAccountId(), userId)) {
                throw new NotFoundException("To account not found");
            }
        } else {
            // EXPENSE/INCOME require accountId and categoryId
            if (request.getAccountId() == null) {
                throw new BusinessException("Account ID is required for " + request.getType() + " transactions");
            }
            if (request.getCategoryId() == null) {
                throw new BusinessException("Category ID is required for " + request.getType() + " transactions");
            }
            // Validate account exists and belongs to user
            if (!accountRepository.existsByIdAndUserIdAndDeletedFalse(request.getAccountId(), userId)) {
                throw new NotFoundException("Account not found");
            }
            // Validate category exists
            if (!categoryRepository.existsByIdAndDeletedFalse(request.getCategoryId())) {
                throw new NotFoundException("Category not found");
            }
        }
    }
    
    /**
     * Validate update request
     */
    private void validateUpdateRequest(UpdateTransactionRequest request, String userId, Transaction existing) {
        TransactionType newType = request.getType() != null ? request.getType() : existing.getType();
        
        if (newType == TransactionType.TRANSFER) {
            String fromAccountId = request.getFromAccountId() != null ? request.getFromAccountId() : existing.getFromAccountId();
            String toAccountId = request.getToAccountId() != null ? request.getToAccountId() : existing.getToAccountId();
            
            if (fromAccountId == null || toAccountId == null) {
                throw new BusinessException("TRANSFER transactions require both fromAccountId and toAccountId");
            }
            if (fromAccountId.equals(toAccountId)) {
                throw new BusinessException("fromAccountId and toAccountId must be different");
            }
            // Validate accounts
            if (!accountRepository.existsByIdAndUserIdAndDeletedFalse(fromAccountId, userId)) {
                throw new NotFoundException("From account not found");
            }
            if (!accountRepository.existsByIdAndUserIdAndDeletedFalse(toAccountId, userId)) {
                throw new NotFoundException("To account not found");
            }
        } else {
            String accountId = request.getAccountId() != null ? request.getAccountId() : existing.getAccountId();
            String categoryId = request.getCategoryId() != null ? request.getCategoryId() : existing.getCategoryId();
            
            if (accountId == null) {
                throw new BusinessException("Account ID is required for " + newType + " transactions");
            }
            if (categoryId == null) {
                throw new BusinessException("Category ID is required for " + newType + " transactions");
            }
            // Validate account
            if (!accountRepository.existsByIdAndUserIdAndDeletedFalse(accountId, userId)) {
                throw new NotFoundException("Account not found");
            }
            // Validate category
            if (!categoryRepository.existsByIdAndDeletedFalse(categoryId)) {
                throw new NotFoundException("Category not found");
            }
        }
    }
}
