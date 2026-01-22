package com.portfolio.wallet.service;

import com.portfolio.common.exception.NotFoundException;
import com.portfolio.wallet.dto.request.CreateLiabilityRequest;
import com.portfolio.wallet.dto.request.CreateTransactionRequest;
import com.portfolio.wallet.dto.request.UpdateLiabilityRequest;
import com.portfolio.wallet.dto.response.LiabilityResponse;
import com.portfolio.wallet.model.Liability;
import com.portfolio.wallet.model.LiabilityStatus;
import com.portfolio.wallet.model.TransactionType;
import com.portfolio.wallet.repository.AccountRepository;
import com.portfolio.wallet.repository.LiabilityRepository;
import com.portfolio.wallet.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Liability service
 */
@Slf4j
@Service
public class LiabilityService {
    
    private final LiabilityRepository liabilityRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    
    // Constructor với @Lazy để tránh circular dependency
    public LiabilityService(
            LiabilityRepository liabilityRepository,
            AccountRepository accountRepository,
            @Lazy TransactionService transactionService) {
        this.liabilityRepository = liabilityRepository;
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
    }
    
    /**
     * Get all liabilities for a user (paginated)
     */
    public Page<LiabilityResponse> getAllLiabilities(String userId, Pageable pageable) {
        log.debug("Getting all liabilities for user: {}", userId);
        Page<Liability> liabilities = liabilityRepository.findByUserIdAndDeletedFalse(userId, pageable);
        return liabilities.map(liability -> {
            // Update status for display (don't save, just for response)
            updateStatus(liability);
            return LiabilityResponse.from(liability);
        });
    }
    
    /**
     * Get all liabilities for a user (list)
     */
    public List<LiabilityResponse> getAllLiabilities(String userId) {
        log.debug("Getting all liabilities for user: {}", userId);
        List<Liability> liabilities = liabilityRepository.findByUserIdAndDeletedFalse(userId);
        return liabilities.stream()
                .map(liability -> {
                    // Update status for display (don't save, just for response)
                    updateStatus(liability);
                    return LiabilityResponse.from(liability);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get liability by id
     */
    public LiabilityResponse getLiabilityById(String id, String userId) {
        log.debug("Getting liability by id: {} for user: {}", id, userId);
        Liability liability = liabilityRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Liability not found"));
        // Update status for display (don't save, just for response)
        updateStatus(liability);
        return LiabilityResponse.from(liability);
    }
    
    /**
     * Create a new liability
     */
    @Transactional
    public LiabilityResponse createLiability(CreateLiabilityRequest request, String userId) {
        log.debug("Creating liability for user: {}", userId);
        
        try {
            if (request == null) {
                throw new IllegalArgumentException("Request cannot be null");
            }
            if (request.getAmount() == null) {
                throw new IllegalArgumentException("Amount cannot be null");
            }
            
            // Validate accountId nếu có
            if (request.getAccountId() != null && !request.getAccountId().isEmpty()) {
                if (!accountRepository.existsByIdAndUserIdAndDeletedFalse(request.getAccountId(), userId)) {
                    throw new NotFoundException("Account not found");
                }
            }
            
            Liability liability = Liability.builder()
                    .userId(userId)
                    .counterpartyName(request.getCounterpartyName())
                    .amount(request.getAmount())
                    .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                    .occurredAt(request.getOccurredAt() != null ? request.getOccurredAt() : LocalDateTime.now())
                    .dueAt(request.getDueAt())
                    .accountId(request.getAccountId())
                    .status(LiabilityStatus.OPEN)
                    .paidAmount(BigDecimal.ZERO)
                    .note(request.getNote())
                    .deleted(false)
                    .build();
            
            // Update status before saving
            updateStatus(liability);
            Liability saved = liabilityRepository.save(liability);
            
            // Nếu có accountId, tự động tạo transaction INCOME để ghi nhận tiền vay (tiền vào)
            if (saved.getAccountId() != null && !saved.getAccountId().isEmpty()) {
                try {
                    CreateTransactionRequest transactionRequest = CreateTransactionRequest.builder()
                            .type(TransactionType.INCOME)
                            .amount(saved.getAmount())
                            .currency(saved.getCurrency())
                            .occurredAt(saved.getOccurredAt())
                            .accountId(saved.getAccountId())
                            .liabilityId(saved.getId())
                            .note(saved.getNote() != null ? 
                                    "Vay: " + saved.getNote() : 
                                    "Vay: " + saved.getCounterpartyName())
                            .build();
                    
                    transactionService.createTransaction(transactionRequest, userId);
                    log.debug("Auto-created INCOME transaction for liability: {}", saved.getId());
                } catch (Exception e) {
                    log.error("Failed to create transaction for liability {}: {}", 
                            saved.getId(), e.getMessage(), e);
                    // Không throw exception để không ảnh hưởng đến việc tạo liability
                }
            }
            
            log.info("Liability created successfully: {}", saved.getId());
            return LiabilityResponse.from(saved);
        } catch (Exception e) {
            log.error("Error creating liability: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Update a liability
     */
    @Transactional
    public LiabilityResponse updateLiability(String id, UpdateLiabilityRequest request, String userId) {
        log.debug("Updating liability: {} for user: {}", id, userId);
        
        Liability liability = liabilityRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Liability not found"));
        
        // Update fields if provided
        if (request.getCounterpartyName() != null) {
            liability.setCounterpartyName(request.getCounterpartyName());
        }
        if (request.getAmount() != null) {
            liability.setAmount(request.getAmount());
        }
        if (request.getCurrency() != null) {
            liability.setCurrency(request.getCurrency());
        }
        if (request.getOccurredAt() != null) {
            liability.setOccurredAt(request.getOccurredAt());
        }
        if (request.getDueAt() != null) {
            liability.setDueAt(request.getDueAt());
        }
        if (request.getAccountId() != null) {
            // Validate accountId nếu có
            if (!request.getAccountId().isEmpty()) {
                if (!accountRepository.existsByIdAndUserIdAndDeletedFalse(request.getAccountId(), userId)) {
                    throw new NotFoundException("Account not found");
                }
            }
            liability.setAccountId(request.getAccountId().isEmpty() ? null : request.getAccountId());
        }
        if (request.getNote() != null) {
            liability.setNote(request.getNote());
        }
        
        // Update status before saving
        updateStatus(liability);
        Liability updated = liabilityRepository.save(liability);
        log.info("Liability updated successfully: {}", updated.getId());
        return LiabilityResponse.from(updated);
    }
    
    /**
     * Delete a liability (soft delete)
     */
    @Transactional
    public void deleteLiability(String id, String userId) {
        log.debug("Deleting liability: {} for user: {}", id, userId);
        
        Liability liability = liabilityRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Liability not found"));
        
        // Soft delete
        liability.setDeleted(true);
        liabilityRepository.save(liability);
        log.info("Liability deleted successfully: {}", id);
    }
    
    /**
     * Update paid amount for a liability (called by SettlementService)
     */
    @Transactional
    public void updatePaidAmount(String liabilityId, BigDecimal paidAmount) {
        log.debug("Updating paid amount for liability: {} to {}", liabilityId, paidAmount);
        
        Liability liability = liabilityRepository.findById(liabilityId)
                .orElseThrow(() -> new NotFoundException("Liability not found"));
        
        liability.setPaidAmount(paidAmount);
        updateStatus(liability);
        liabilityRepository.save(liability);
    }
    
    /**
     * Update status based on paid amount and due date
     */
    private void updateStatus(Liability liability) {
        if (liability == null || liability.getAmount() == null) {
            log.warn("Liability or amount is null, skipping status update");
            return;
        }
        
        BigDecimal paidAmount = liability.getPaidAmount() != null ? liability.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal amount = liability.getAmount();
        
        // Determine status based on paid amount
        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            // Not paid yet
            if (liability.getDueAt() != null && LocalDateTime.now().isAfter(liability.getDueAt())) {
                liability.setStatus(LiabilityStatus.OVERDUE);
            } else {
                liability.setStatus(LiabilityStatus.OPEN);
            }
        } else if (paidAmount.compareTo(amount) >= 0) {
            // Fully paid
            liability.setStatus(LiabilityStatus.PAID);
        } else {
            // Partially paid
            if (liability.getDueAt() != null && LocalDateTime.now().isAfter(liability.getDueAt())) {
                liability.setStatus(LiabilityStatus.OVERDUE);
            } else {
                liability.setStatus(LiabilityStatus.PARTIALLY_PAID);
            }
        }
    }
}
