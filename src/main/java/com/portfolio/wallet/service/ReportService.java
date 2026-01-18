package com.portfolio.wallet.service;

import com.portfolio.wallet.dto.response.DashboardReportResponse;
import com.portfolio.wallet.model.Transaction;
import com.portfolio.wallet.model.TransactionType;
import com.portfolio.wallet.repository.AccountRepository;
import com.portfolio.wallet.repository.CategoryRepository;
import com.portfolio.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * Report service
 * 
 * Handles dashboard reports and analytics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final MongoTemplate mongoTemplate;
    
    /**
     * Get dashboard report for a user
     * 
     * @param userId User ID
     * @param period Period: "day", "week", "month", "year"
     * @param startDateStr Optional start date string (ISO format: YYYY-MM-DDTHH:mm:ss)
     * @param endDateStr Optional end date string (ISO format: YYYY-MM-DDTHH:mm:ss)
     * @return Dashboard report
     */
    public DashboardReportResponse getDashboardReport(String userId, String period, String startDateStr, String endDateStr) {
        log.debug("Getting dashboard report for user: {}, period: {}, startDate: {}, endDate: {}", 
                userId, period, startDateStr, endDateStr);
        
        LocalDateTime startDate;
        LocalDateTime endDate;
        LocalDateTime now = LocalDateTime.now();
        
        // Parse startDate if provided
        LocalDateTime parsedStartDate = null;
        if (startDateStr != null && !startDateStr.isEmpty()) {
            try {
                parsedStartDate = LocalDateTime.parse(startDateStr);
            } catch (Exception e) {
                log.warn("Failed to parse startDate: {}, will calculate from period", e.getMessage());
            }
        }
        
        // Parse endDate if provided
        LocalDateTime parsedEndDate = null;
        if (endDateStr != null && !endDateStr.isEmpty()) {
            try {
                parsedEndDate = LocalDateTime.parse(endDateStr);
            } catch (Exception e) {
                log.warn("Failed to parse endDate: {}, will calculate from period", e.getMessage());
            }
        }
        
        // Determine final date range
        if (parsedStartDate != null && parsedEndDate != null) {
            // Both dates provided
            startDate = parsedStartDate;
            endDate = parsedEndDate;
            log.debug("Using provided date range: {} to {}", startDate, endDate);
        } else if (parsedStartDate != null) {
            // Only startDate provided, use it to now
            startDate = parsedStartDate;
            endDate = now;
            log.debug("Using provided startDate: {}, endDate: now ({})", startDate, endDate);
        } else if (parsedEndDate != null) {
            // Only endDate provided, use from 30 days before endDate to endDate
            startDate = parsedEndDate.minusDays(30).toLocalDate().atStartOfDay();
            endDate = parsedEndDate;
            log.debug("Using provided endDate: {}, startDate: 30 days before ({})", endDate, startDate);
        } else {
            // No dates provided, calculate from period
            LocalDateTime[] dateRange = calculateDateRange(period);
            startDate = dateRange[0];
            endDate = dateRange[1];
            log.debug("Calculated date range from period '{}': {} to {}", period, startDate, endDate);
        }
        
        // Get all transactions for the period
        List<Transaction> transactions = transactionRepository.findByUserIdAndOccurredAtBetweenAndDeletedFalse(
                userId, startDate, endDate);
        
        // Calculate totals
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netSavings = totalIncome.subtract(totalExpense);
        
        // Get accounts overview
        List<DashboardReportResponse.AccountBalance> accountsOverview = getAccountsOverview(userId, transactions);
        
        // Get top categories
        List<DashboardReportResponse.CategorySummary> topCategories = getTopCategories(userId, transactions);
        
        return DashboardReportResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(netSavings)
                .accountsOverview(accountsOverview)
                .topCategories(topCategories)
                .build();
    }
    
    /**
     * Calculate date range based on period
     */
    private LocalDateTime[] calculateDateRange(String period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;
        
        switch (period.toLowerCase()) {
            case "day":
                startDate = endDate.toLocalDate().atStartOfDay();
                break;
            case "week":
                startDate = endDate.minusWeeks(1);
                break;
            case "month":
                startDate = endDate.toLocalDate().withDayOfMonth(1).atStartOfDay();
                break;
            case "year":
                startDate = endDate.toLocalDate().withDayOfYear(1).atStartOfDay();
                break;
            default:
                // Default to month
                startDate = endDate.toLocalDate().withDayOfMonth(1).atStartOfDay();
        }
        
        return new LocalDateTime[]{startDate, endDate};
    }
    
    /**
     * Get accounts overview with balances
     */
    private List<DashboardReportResponse.AccountBalance> getAccountsOverview(
            String userId, List<Transaction> transactions) {
        
        // Get all accounts for user
        var accounts = accountRepository.findByUserIdAndDeletedFalse(userId);
        
        return accounts.stream()
                .map(account -> {
                    // Calculate balance: openingBalance + income - expense
                    BigDecimal balance = account.getOpeningBalance();
                    
                    // Add income transactions
                    BigDecimal income = transactions.stream()
                            .filter(t -> t.getType() == TransactionType.INCOME 
                                    && account.getId().equals(t.getAccountId()))
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    // Subtract expense transactions
                    BigDecimal expense = transactions.stream()
                            .filter(t -> t.getType() == TransactionType.EXPENSE 
                                    && account.getId().equals(t.getAccountId()))
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    // Handle transfers
                    BigDecimal transferIn = transactions.stream()
                            .filter(t -> t.getType() == TransactionType.TRANSFER 
                                    && account.getId().equals(t.getToAccountId()))
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    BigDecimal transferOut = transactions.stream()
                            .filter(t -> t.getType() == TransactionType.TRANSFER 
                                    && account.getId().equals(t.getFromAccountId()))
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    balance = balance.add(income).subtract(expense).add(transferIn).subtract(transferOut);
                    
                    return DashboardReportResponse.AccountBalance.builder()
                            .accountId(account.getId())
                            .accountName(account.getName())
                            .balance(balance)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get top categories by expense
     */
    private List<DashboardReportResponse.CategorySummary> getTopCategories(
            String userId, List<Transaction> transactions) {
        
        // Filter expense transactions with category
        Map<String, List<Transaction>> transactionsByCategory = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE && t.getCategoryId() != null)
                .collect(Collectors.groupingBy(Transaction::getCategoryId));
        
        // Get category names
        var categories = categoryRepository.findAllCategoriesForUser(userId);
        Map<String, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(
                        c -> c.getId(),
                        c -> c.getName(),
                        (existing, replacement) -> existing
                ));
        
        // Calculate totals per category
        List<DashboardReportResponse.CategorySummary> categorySummaries = transactionsByCategory.entrySet().stream()
                .map(entry -> {
                    String categoryId = entry.getKey();
                    List<Transaction> categoryTransactions = entry.getValue();
                    
                    BigDecimal totalAmount = categoryTransactions.stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    return DashboardReportResponse.CategorySummary.builder()
                            .categoryId(categoryId)
                            .categoryName(categoryNameMap.getOrDefault(categoryId, "Unknown"))
                            .totalAmount(totalAmount)
                            .transactionCount((long) categoryTransactions.size())
                            .build();
                })
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount())) // Sort by amount desc
                .limit(5) // Top 5
                .collect(Collectors.toList());
        
        return categorySummaries;
    }
}
