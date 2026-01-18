package com.portfolio.wallet.service;

import com.portfolio.common.exception.NotFoundException;
import com.portfolio.wallet.dto.request.CreateAccountRequest;
import com.portfolio.wallet.dto.request.UpdateAccountRequest;
import com.portfolio.wallet.dto.response.AccountResponse;
import com.portfolio.wallet.model.Account;
import com.portfolio.wallet.model.Transaction;
import com.portfolio.wallet.model.TransactionType;
import com.portfolio.wallet.repository.AccountRepository;
import com.portfolio.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Account service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    
    /**
     * Calculate current balance for an account
     * currentBalance = openingBalance + INCOME - EXPENSE + TRANSFER_IN - TRANSFER_OUT
     */
    private BigDecimal calculateCurrentBalance(Account account) {
        BigDecimal balance = account.getOpeningBalance();
        
        // Get all transactions for this account
        List<Transaction> accountTransactions = transactionRepository.findByAccountIdAndDeletedFalse(account.getId());
        List<Transaction> fromAccountTransactions = transactionRepository.findByFromAccountIdAndDeletedFalse(account.getId());
        List<Transaction> toAccountTransactions = transactionRepository.findByToAccountIdAndDeletedFalse(account.getId());
        
        // Add INCOME transactions
        for (Transaction t : accountTransactions) {
            if (t.getType() == TransactionType.INCOME) {
                balance = balance.add(t.getAmount());
            } else if (t.getType() == TransactionType.EXPENSE) {
                balance = balance.subtract(t.getAmount());
            }
        }
        
        // Subtract TRANSFER_OUT (from this account)
        for (Transaction t : fromAccountTransactions) {
            if (t.getType() == TransactionType.TRANSFER) {
                balance = balance.subtract(t.getAmount());
            }
        }
        
        // Add TRANSFER_IN (to this account)
        for (Transaction t : toAccountTransactions) {
            if (t.getType() == TransactionType.TRANSFER) {
                balance = balance.add(t.getAmount());
            }
        }
        
        return balance;
    }
    
    /**
     * Get all accounts for a user (paginated)
     */
    public Page<AccountResponse> getAllAccounts(String userId, Pageable pageable) {
        log.debug("Getting all accounts for user: {}", userId);
        Page<Account> accounts = accountRepository.findByUserIdAndDeletedFalse(userId, pageable);
        return accounts.map(account -> {
            BigDecimal currentBalance = calculateCurrentBalance(account);
            return AccountResponse.from(account, currentBalance);
        });
    }
    
    /**
     * Get all accounts for a user (list)
     */
    public List<AccountResponse> getAllAccounts(String userId) {
        log.debug("Getting all accounts for user: {}", userId);
        List<Account> accounts = accountRepository.findByUserIdAndDeletedFalse(userId);
        return accounts.stream()
                .map(account -> {
                    BigDecimal currentBalance = calculateCurrentBalance(account);
                    return AccountResponse.from(account, currentBalance);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get account by id
     */
    public AccountResponse getAccountById(String id, String userId) {
        log.debug("Getting account by id: {} for user: {}", id, userId);
        Account account = accountRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        BigDecimal currentBalance = calculateCurrentBalance(account);
        return AccountResponse.from(account, currentBalance);
    }
    
    /**
     * Create a new account
     */
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, String userId) {
        log.debug("Creating account for user: {}", userId);
        
        Account account = Account.builder()
                .userId(userId)
                .name(request.getName())
                .type(request.getType())
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .openingBalance(request.getOpeningBalance() != null ? request.getOpeningBalance() : java.math.BigDecimal.ZERO)
                .note(request.getNote())
                .deleted(false)
                .build();
        
        Account saved = accountRepository.save(account);
        log.info("Account created successfully: {}", saved.getId());
        BigDecimal currentBalance = calculateCurrentBalance(saved);
        return AccountResponse.from(saved, currentBalance);
    }
    
    /**
     * Update an account
     */
    @Transactional
    public AccountResponse updateAccount(String id, UpdateAccountRequest request, String userId) {
        log.debug("Updating account: {} for user: {}", id, userId);
        
        Account account = accountRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        
        // Update fields if provided
        if (request.getName() != null) {
            account.setName(request.getName());
        }
        if (request.getType() != null) {
            account.setType(request.getType());
        }
        if (request.getCurrency() != null) {
            account.setCurrency(request.getCurrency());
        }
        if (request.getOpeningBalance() != null) {
            account.setOpeningBalance(request.getOpeningBalance());
        }
        if (request.getNote() != null) {
            account.setNote(request.getNote());
        }
        
        Account updated = accountRepository.save(account);
        log.info("Account updated successfully: {}", updated.getId());
        BigDecimal currentBalance = calculateCurrentBalance(updated);
        return AccountResponse.from(updated, currentBalance);
    }
    
    /**
     * Delete an account (soft delete)
     */
    @Transactional
    public void deleteAccount(String id, String userId) {
        log.debug("Deleting account: {} for user: {}", id, userId);
        
        Account account = accountRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        
        // Soft delete
        account.setDeleted(true);
        accountRepository.save(account);
        log.info("Account deleted successfully: {}", id);
    }
}
