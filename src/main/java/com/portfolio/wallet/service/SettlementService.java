package com.portfolio.wallet.service;

import com.portfolio.common.exception.ConflictException;
import com.portfolio.common.exception.NotFoundException;
import com.portfolio.wallet.dto.request.CreateSettlementRequest;
import com.portfolio.wallet.dto.request.UpdateSettlementRequest;
import com.portfolio.wallet.dto.response.SettlementResponse;
import com.portfolio.wallet.model.Receivable;
import com.portfolio.wallet.model.Liability;
import com.portfolio.wallet.model.Settlement;
import com.portfolio.wallet.model.SettlementType;
import com.portfolio.wallet.repository.ReceivableRepository;
import com.portfolio.wallet.repository.LiabilityRepository;
import com.portfolio.wallet.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Settlement service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {
    
    private final SettlementRepository settlementRepository;
    private final ReceivableRepository receivableRepository;
    private final LiabilityRepository liabilityRepository;
    private final ReceivableService receivableService;
    private final LiabilityService liabilityService;
    
    /**
     * Get all settlements for a user (paginated)
     */
    public Page<SettlementResponse> getAllSettlements(String userId, Pageable pageable) {
        log.debug("Getting all settlements for user: {}", userId);
        Page<Settlement> settlements = settlementRepository.findByUserIdAndDeletedFalse(userId, pageable);
        return settlements.map(SettlementResponse::from);
    }
    
    /**
     * Get all settlements for a user (list)
     */
    public List<SettlementResponse> getAllSettlements(String userId) {
        log.debug("Getting all settlements for user: {}", userId);
        List<Settlement> settlements = settlementRepository.findByUserIdAndDeletedFalse(userId);
        return settlements.stream()
                .map(SettlementResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get settlement by id
     */
    public SettlementResponse getSettlementById(String id, String userId) {
        log.debug("Getting settlement by id: {} for user: {}", id, userId);
        Settlement settlement = settlementRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Settlement not found"));
        return SettlementResponse.from(settlement);
    }
    
    /**
     * Get all settlements for a receivable
     */
    public List<SettlementResponse> getSettlementsByReceivableId(String receivableId, String userId) {
        log.debug("Getting settlements for receivable: {} for user: {}", receivableId, userId);
        
        // Verify receivable belongs to user
        Receivable receivable = receivableRepository.findByIdAndUserIdAndDeletedFalse(receivableId, userId)
                .orElseThrow(() -> new NotFoundException("Receivable not found"));
        
        List<Settlement> settlements = settlementRepository.findByReceivableIdAndDeletedFalse(receivableId);
        return settlements.stream()
                .map(SettlementResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all settlements for a liability
     */
    public List<SettlementResponse> getSettlementsByLiabilityId(String liabilityId, String userId) {
        log.debug("Getting settlements for liability: {} for user: {}", liabilityId, userId);
        
        // Verify liability belongs to user
        Liability liability = liabilityRepository.findByIdAndUserIdAndDeletedFalse(liabilityId, userId)
                .orElseThrow(() -> new NotFoundException("Liability not found"));
        
        List<Settlement> settlements = settlementRepository.findByLiabilityIdAndDeletedFalse(liabilityId);
        return settlements.stream()
                .map(SettlementResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Create a new settlement
     */
    @Transactional
    public SettlementResponse createSettlement(CreateSettlementRequest request, String userId) {
        log.debug("Creating settlement for user: {}", userId);
        
        // Validate request
        if (request.getType() == SettlementType.RECEIVABLE) {
            if (request.getReceivableId() == null || request.getReceivableId().trim().isEmpty()) {
                throw new IllegalArgumentException("Receivable ID is required for RECEIVABLE type");
            }
            if (request.getLiabilityId() != null) {
                throw new IllegalArgumentException("Liability ID should not be provided for RECEIVABLE type");
            }
        } else if (request.getType() == SettlementType.LIABILITY) {
            if (request.getLiabilityId() == null || request.getLiabilityId().trim().isEmpty()) {
                throw new IllegalArgumentException("Liability ID is required for LIABILITY type");
            }
            if (request.getReceivableId() != null) {
                throw new IllegalArgumentException("Receivable ID should not be provided for LIABILITY type");
            }
        }
        
        // Get the receivable or liability and verify ownership
        BigDecimal originalAmount;
        if (request.getType() == SettlementType.RECEIVABLE) {
            Receivable receivable = receivableRepository.findByIdAndUserIdAndDeletedFalse(
                    request.getReceivableId(), userId)
                    .orElseThrow(() -> new NotFoundException("Receivable not found"));
            originalAmount = receivable.getAmount();
            log.debug("Creating settlement for receivable: {}, original amount: {}", 
                    receivable.getId(), originalAmount);
        } else {
            Liability liability = liabilityRepository.findByIdAndUserIdAndDeletedFalse(
                    request.getLiabilityId(), userId)
                    .orElseThrow(() -> new NotFoundException("Liability not found"));
            originalAmount = liability.getAmount();
            log.debug("Creating settlement for liability: {}, original amount: {}", 
                    liability.getId(), originalAmount);
        }
        
        // Calculate total existing settlements
        BigDecimal totalSettlements = calculateTotalSettlements(
                request.getType(),
                request.getReceivableId(),
                request.getLiabilityId());
        
        // Validate: total settlements (including new one) <= original amount
        BigDecimal newTotal = totalSettlements.add(request.getAmount());
        if (newTotal.compareTo(originalAmount) > 0) {
            throw new ConflictException(
                    String.format("Total settlement amount (%.2f) exceeds original amount (%.2f)",
                            newTotal, originalAmount));
        }
        
        // Create settlement
        Settlement settlement = Settlement.builder()
                .userId(userId)
                .type(request.getType())
                .receivableId(request.getReceivableId())
                .liabilityId(request.getLiabilityId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .occurredAt(request.getOccurredAt() != null ? request.getOccurredAt() : LocalDateTime.now())
                .note(request.getNote())
                .deleted(false)
                .build();
        
        Settlement saved = settlementRepository.save(settlement);
        
        // Update paid amount in receivable/liability
        updatePaidAmount(request.getType(), request.getReceivableId(), request.getLiabilityId());
        
        log.info("Settlement created successfully: {}", saved.getId());
        return SettlementResponse.from(saved);
    }
    
    /**
     * Update a settlement
     */
    @Transactional
    public SettlementResponse updateSettlement(String id, UpdateSettlementRequest request, String userId) {
        log.debug("Updating settlement: {} for user: {}", id, userId);
        
        Settlement settlement = settlementRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Settlement not found"));
        
        // Store old amount for recalculation
        BigDecimal oldAmount = settlement.getAmount();
        
        // Update fields if provided
        if (request.getAmount() != null) {
            settlement.setAmount(request.getAmount());
        }
        if (request.getCurrency() != null) {
            settlement.setCurrency(request.getCurrency());
        }
        if (request.getOccurredAt() != null) {
            settlement.setOccurredAt(request.getOccurredAt());
        }
        if (request.getNote() != null) {
            settlement.setNote(request.getNote());
        }
        
        // If amount changed, validate total
        if (request.getAmount() != null && request.getAmount().compareTo(oldAmount) != 0) {
            BigDecimal originalAmount;
            if (settlement.getType() == SettlementType.RECEIVABLE) {
                Receivable receivable = receivableRepository.findByIdAndUserIdAndDeletedFalse(
                        settlement.getReceivableId(), userId)
                        .orElseThrow(() -> new NotFoundException("Receivable not found"));
                originalAmount = receivable.getAmount();
            } else {
                Liability liability = liabilityRepository.findByIdAndUserIdAndDeletedFalse(
                        settlement.getLiabilityId(), userId)
                        .orElseThrow(() -> new NotFoundException("Liability not found"));
                originalAmount = liability.getAmount();
            }
            
            // Calculate total settlements (excluding this one, then add new amount)
            BigDecimal totalSettlements = calculateTotalSettlements(
                    settlement.getType(),
                    settlement.getReceivableId(),
                    settlement.getLiabilityId());
            totalSettlements = totalSettlements.subtract(oldAmount).add(request.getAmount());
            
            if (totalSettlements.compareTo(originalAmount) > 0) {
                throw new ConflictException(
                        String.format("Total settlement amount (%.2f) exceeds original amount (%.2f)",
                                totalSettlements, originalAmount));
            }
        }
        
        Settlement updated = settlementRepository.save(settlement);
        
        // Update paid amount in receivable/liability
        updatePaidAmount(settlement.getType(), settlement.getReceivableId(), settlement.getLiabilityId());
        
        log.info("Settlement updated successfully: {}", updated.getId());
        return SettlementResponse.from(updated);
    }
    
    /**
     * Delete a settlement (soft delete)
     */
    @Transactional
    public void deleteSettlement(String id, String userId) {
        log.debug("Deleting settlement: {} for user: {}", id, userId);
        
        Settlement settlement = settlementRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Settlement not found"));
        
        // Soft delete
        settlement.setDeleted(true);
        settlementRepository.save(settlement);
        
        // Update paid amount in receivable/liability
        updatePaidAmount(settlement.getType(), settlement.getReceivableId(), settlement.getLiabilityId());
        
        log.info("Settlement deleted successfully: {}", id);
    }
    
    /**
     * Calculate total settlements for a receivable or liability
     */
    private BigDecimal calculateTotalSettlements(SettlementType type, String receivableId, String liabilityId) {
        List<Settlement> settlements;
        if (type == SettlementType.RECEIVABLE) {
            settlements = settlementRepository.findByReceivableIdAndDeletedFalse(receivableId);
        } else {
            settlements = settlementRepository.findByLiabilityIdAndDeletedFalse(liabilityId);
        }
        
        return settlements.stream()
                .map(Settlement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Update paid amount in receivable or liability
     */
    private void updatePaidAmount(SettlementType type, String receivableId, String liabilityId) {
        BigDecimal totalPaid = calculateTotalSettlements(type, receivableId, liabilityId);
        
        if (type == SettlementType.RECEIVABLE) {
            receivableService.updatePaidAmount(receivableId, totalPaid);
        } else {
            liabilityService.updatePaidAmount(liabilityId, totalPaid);
        }
    }
}
