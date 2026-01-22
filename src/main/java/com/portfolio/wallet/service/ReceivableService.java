package com.portfolio.wallet.service;

import com.portfolio.common.exception.NotFoundException;
import com.portfolio.wallet.dto.request.CreateReceivableRequest;
import com.portfolio.wallet.dto.request.CreateTransactionRequest;
import com.portfolio.wallet.dto.request.UpdateReceivableRequest;
import com.portfolio.wallet.dto.response.ReceivableResponse;
import com.portfolio.wallet.model.Receivable;
import com.portfolio.wallet.model.ReceivableStatus;
import com.portfolio.wallet.model.TransactionType;
import com.portfolio.wallet.repository.AccountRepository;
import com.portfolio.wallet.repository.ReceivableRepository;
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
 * Receivable service
 */
@Slf4j
@Service
public class ReceivableService {
    
    private final ReceivableRepository receivableRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    
    // Constructor với @Lazy để tránh circular dependency
    public ReceivableService(
            ReceivableRepository receivableRepository,
            AccountRepository accountRepository,
            @Lazy TransactionService transactionService) {
        this.receivableRepository = receivableRepository;
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
    }
    
    /**
     * Get all receivables for a user (paginated)
     */
    public Page<ReceivableResponse> getAllReceivables(String userId, Pageable pageable) {
        log.debug("Getting all receivables for user: {}", userId);
        Page<Receivable> receivables = receivableRepository.findByUserIdAndDeletedFalse(userId, pageable);
        return receivables.map(receivable -> {
            // Update status for display (don't save, just for response)
            updateStatus(receivable);
            return ReceivableResponse.from(receivable);
        });
    }
    
    /**
     * Get all receivables for a user (list)
     */
    public List<ReceivableResponse> getAllReceivables(String userId) {
        log.debug("Getting all receivables for user: {}", userId);
        List<Receivable> receivables = receivableRepository.findByUserIdAndDeletedFalse(userId);
        return receivables.stream()
                .map(receivable -> {
                    // Update status for display (don't save, just for response)
                    updateStatus(receivable);
                    return ReceivableResponse.from(receivable);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get receivable by id
     */
    public ReceivableResponse getReceivableById(String id, String userId) {
        log.debug("Getting receivable by id: {} for user: {}", id, userId);
        Receivable receivable = receivableRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Receivable not found"));
        // Update status for display (don't save, just for response)
        updateStatus(receivable);
        return ReceivableResponse.from(receivable);
    }
    
    /**
     * Create a new receivable
     */
    @Transactional
    public ReceivableResponse createReceivable(CreateReceivableRequest request, String userId) {
        log.debug("Creating receivable for user: {}", userId);
        
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
            
            Receivable receivable = Receivable.builder()
                    .userId(userId)
                    .counterpartyName(request.getCounterpartyName())
                    .amount(request.getAmount())
                    .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                    .occurredAt(request.getOccurredAt() != null ? request.getOccurredAt() : LocalDateTime.now())
                    .dueAt(request.getDueAt())
                    .accountId(request.getAccountId())
                    .status(ReceivableStatus.OPEN) // Will be updated by updateStatus
                    .paidAmount(BigDecimal.ZERO)
                    .note(request.getNote())
                    .deleted(false)
                    .build();
            
            // Update status before saving
            updateStatus(receivable);
            Receivable saved = receivableRepository.save(receivable);
            
            // Nếu có accountId, tự động tạo transaction EXPENSE để ghi nhận tiền cho vay (tiền đi ra)
            if (saved.getAccountId() != null && !saved.getAccountId().isEmpty()) {
                try {
                    CreateTransactionRequest transactionRequest = CreateTransactionRequest.builder()
                            .type(TransactionType.EXPENSE)
                            .amount(saved.getAmount())
                            .currency(saved.getCurrency())
                            .occurredAt(saved.getOccurredAt())
                            .accountId(saved.getAccountId())
                            .receivableId(saved.getId())
                            .note(saved.getNote() != null ? 
                                    "Cho vay: " + saved.getNote() : 
                                    "Cho vay: " + saved.getCounterpartyName())
                            .build();
                    
                    transactionService.createTransaction(transactionRequest, userId);
                    log.debug("Auto-created EXPENSE transaction for receivable: {}", saved.getId());
                } catch (Exception e) {
                    log.error("Failed to create transaction for receivable {}: {}", 
                            saved.getId(), e.getMessage(), e);
                    // Không throw exception để không ảnh hưởng đến việc tạo receivable
                }
            }
            
            log.info("Receivable created successfully: {}", saved.getId());
            return ReceivableResponse.from(saved);
        } catch (Exception e) {
            log.error("Error creating receivable: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Update a receivable
     */
    @Transactional
    public ReceivableResponse updateReceivable(String id, UpdateReceivableRequest request, String userId) {
        log.debug("Updating receivable: {} for user: {}", id, userId);
        
        Receivable receivable = receivableRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Receivable not found"));
        
        // Update fields if provided
        if (request.getCounterpartyName() != null) {
            receivable.setCounterpartyName(request.getCounterpartyName());
        }
        if (request.getAmount() != null) {
            receivable.setAmount(request.getAmount());
        }
        if (request.getCurrency() != null) {
            receivable.setCurrency(request.getCurrency());
        }
        if (request.getOccurredAt() != null) {
            receivable.setOccurredAt(request.getOccurredAt());
        }
        if (request.getDueAt() != null) {
            receivable.setDueAt(request.getDueAt());
        }
        if (request.getAccountId() != null) {
            // Validate accountId nếu có
            if (!request.getAccountId().isEmpty()) {
                if (!accountRepository.existsByIdAndUserIdAndDeletedFalse(request.getAccountId(), userId)) {
                    throw new NotFoundException("Account not found");
                }
            }
            receivable.setAccountId(request.getAccountId().isEmpty() ? null : request.getAccountId());
        }
        if (request.getNote() != null) {
            receivable.setNote(request.getNote());
        }
        
        // Update status before saving
        updateStatus(receivable);
        Receivable updated = receivableRepository.save(receivable);
        log.info("Receivable updated successfully: {}", updated.getId());
        return ReceivableResponse.from(updated);
    }
    
    /**
     * Delete a receivable (soft delete)
     */
    @Transactional
    public void deleteReceivable(String id, String userId) {
        log.debug("Deleting receivable: {} for user: {}", id, userId);
        
        Receivable receivable = receivableRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Receivable not found"));
        
        // Soft delete
        receivable.setDeleted(true);
        receivableRepository.save(receivable);
        log.info("Receivable deleted successfully: {}", id);
    }
    
    /**
     * Update paid amount for a receivable (called by SettlementService)
     */
    @Transactional
    public void updatePaidAmount(String receivableId, BigDecimal paidAmount) {
        log.debug("Updating paid amount for receivable: {} to {}", receivableId, paidAmount);
        
        Receivable receivable = receivableRepository.findById(receivableId)
                .orElseThrow(() -> new NotFoundException("Receivable not found"));
        
        receivable.setPaidAmount(paidAmount);
        updateStatus(receivable);
        receivableRepository.save(receivable);
    }
    
    /**
     * Update status based on paid amount and due date
     */
    private void updateStatus(Receivable receivable) {
        if (receivable == null || receivable.getAmount() == null) {
            log.warn("Receivable or amount is null, skipping status update");
            return;
        }
        
        BigDecimal paidAmount = receivable.getPaidAmount() != null ? receivable.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal amount = receivable.getAmount();
        
        // Determine status based on paid amount
        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            // Not paid yet
            if (receivable.getDueAt() != null && LocalDateTime.now().isAfter(receivable.getDueAt())) {
                receivable.setStatus(ReceivableStatus.OVERDUE);
            } else {
                receivable.setStatus(ReceivableStatus.OPEN);
            }
        } else if (paidAmount.compareTo(amount) >= 0) {
            // Fully paid
            receivable.setStatus(ReceivableStatus.PAID);
        } else {
            // Partially paid
            if (receivable.getDueAt() != null && LocalDateTime.now().isAfter(receivable.getDueAt())) {
                receivable.setStatus(ReceivableStatus.OVERDUE);
            } else {
                receivable.setStatus(ReceivableStatus.PARTIALLY_PAID);
            }
        }
    }
}
