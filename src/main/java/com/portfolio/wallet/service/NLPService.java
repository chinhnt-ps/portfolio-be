package com.portfolio.wallet.service;

import com.portfolio.wallet.dto.request.ParseTransactionRequest;
import com.portfolio.wallet.dto.response.*;
import com.portfolio.wallet.model.*;
import com.portfolio.wallet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * NLP Service for parsing user text input and generating appropriate responses
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NLPService {
    
    private final GeminiService geminiService;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final ReceivableRepository receivableRepository;
    private final LiabilityRepository liabilityRepository;
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");
    private static final DateTimeFormatter DATE_SHORT_FORMATTER = DateTimeFormatter.ofPattern("d/M");
    private static final ZoneId VN_TIMEZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter ISO_WITH_TZ_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    
    /**
     * Parse user text input and return NLP response
     */
    public NLPResponse parseTransaction(ParseTransactionRequest request, String userId) {
        try {
            // Load context for user
            Map<String, Object> context = buildContext(userId);
            
            // Call Gemini to parse text
            Map<String, Object> geminiResponse = geminiService.parseText(
                request.getText(),
                context,
                request.getTimezone() != null ? request.getTimezone() : "Asia/Ho_Chi_Minh",
                request.getLocale() != null ? request.getLocale() : "vi-VN"
            );
            
            // Process Gemini response and build NLPResponse
            return processGeminiResponse(geminiResponse, userId, context);
            
        } catch (IllegalStateException e) {
            log.error("Gemini API not configured", e);
            return buildErrorResponse("API_NOT_CONFIGURED", "Tính năng AI chưa được cấu hình. Vui lòng liên hệ admin.", null);
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("429")) {
                log.error("Gemini API rate limit exceeded", e);
                return buildErrorResponse("RATE_LIMIT_EXCEEDED", "Đã vượt quá giới hạn sử dụng. Vui lòng thử lại sau vài giây.", "API quota exceeded");
            } else if (errorMessage != null && errorMessage.contains("quota")) {
                log.error("Gemini API quota exceeded", e);
                return buildErrorResponse("QUOTA_EXCEEDED", "Đã hết quota sử dụng. Vui lòng thử lại sau.", "API quota exceeded");
            } else {
                log.error("Error parsing transaction text", e);
                return buildErrorResponse("PARSE_ERROR", "Không thể phân tích lệnh. Vui lòng thử lại.", null);
            }
        } catch (Exception e) {
            log.error("Unexpected error parsing transaction text", e);
            return buildErrorResponse("UNKNOWN_ERROR", "Đã xảy ra lỗi không xác định. Vui lòng thử lại.", null);
        }
    }
    
    /**
     * Build context data for Gemini prompt
     */
    private Map<String, Object> buildContext(String userId) {
        Map<String, Object> context = new HashMap<>();
        
        // Load accounts
        List<Account> accounts = accountRepository.findByUserIdAndDeletedFalse(userId);
        List<Map<String, Object>> accountList = accounts.stream()
            .map(acc -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", acc.getId());
                map.put("name", acc.getName());
                map.put("type", acc.getType().name());
                return map;
            })
            .collect(Collectors.toList());
        context.put("accounts", accountList);
        
        // Load categories
        List<Category> categories = categoryRepository.findAllCategoriesForUser(userId);
        List<Map<String, Object>> categoryList = categories.stream()
            .map(cat -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", cat.getId());
                map.put("name", cat.getName());
                map.put("isSystem", cat.getIsSystem());
                return map;
            })
            .collect(Collectors.toList());
        context.put("categories", categoryList);
        
        // Load open receivables (not fully paid)
        List<Receivable> receivables = receivableRepository.findByUserIdAndDeletedFalse(userId);
        List<Map<String, Object>> receivableList = receivables.stream()
            .filter(rec -> rec.getStatus() != ReceivableStatus.PAID)
            .map(rec -> {
                BigDecimal remainingAmount = rec.getAmount();
                if (rec.getPaidAmount() != null) {
                    remainingAmount = rec.getAmount().subtract(rec.getPaidAmount());
                }
                Map<String, Object> map = new HashMap<>();
                map.put("id", rec.getId());
                map.put("counterpartyName", rec.getCounterpartyName());
                map.put("remainingAmount", remainingAmount);
                map.put("dueAt", rec.getDueAt() != null ? rec.getDueAt().toString() : "");
                return map;
            })
            .collect(Collectors.toList());
        context.put("openReceivables", receivableList);
        
        // Load open liabilities (not fully paid)
        List<Liability> liabilities = liabilityRepository.findByUserIdAndDeletedFalse(userId);
        List<Map<String, Object>> liabilityList = liabilities.stream()
            .filter(liab -> liab.getStatus() != LiabilityStatus.PAID)
            .map(liab -> {
                BigDecimal remainingAmount = liab.getAmount();
                if (liab.getPaidAmount() != null) {
                    remainingAmount = liab.getAmount().subtract(liab.getPaidAmount());
                }
                Map<String, Object> map = new HashMap<>();
                map.put("id", liab.getId());
                map.put("counterpartyName", liab.getCounterpartyName());
                map.put("remainingAmount", remainingAmount);
                map.put("dueAt", liab.getDueAt() != null ? liab.getDueAt().toString() : "");
                return map;
            })
            .collect(Collectors.toList());
        context.put("openLiabilities", liabilityList);
        
        return context;
    }
    
    /**
     * Process Gemini response and build NLPResponse
     */
    @SuppressWarnings("unchecked")
    private NLPResponse processGeminiResponse(Map<String, Object> geminiResponse, String userId, Map<String, Object> context) {
        String intentStr = (String) geminiResponse.getOrDefault("intent", "UNKNOWN");
        Double confidence = getDoubleValue(geminiResponse.get("confidence"));
        if (confidence == null) confidence = 0.5;
        
        NLPResponse.Intent intent;
        try {
            intent = NLPResponse.Intent.valueOf(intentStr);
        } catch (IllegalArgumentException e) {
            intent = NLPResponse.Intent.UNKNOWN;
        }
        
        Map<String, Object> entities = (Map<String, Object>) geminiResponse.getOrDefault("entities", new HashMap<>());
        
        // Handle different intents
        switch (intent) {
            case CREATE_TRANSACTION:
                return buildTransactionDraftResponse(entities, userId, context, confidence);
            case CREATE_RECEIVABLE:
                return buildReceivableDraftResponse(entities, userId, confidence);
            case CREATE_LIABILITY:
                return buildLiabilityDraftResponse(entities, userId, confidence);
            case CREATE_SETTLEMENT:
                return buildSettlementDraftResponse(entities, userId, context, confidence);
            case QUERY_DATA:
                return buildQueryResponse(entities, userId, confidence);
            default:
                return buildErrorResponse("UNKNOWN_INTENT", "Không hiểu lệnh. Vui lòng thử lại với cú pháp khác.", null);
        }
    }
    
    /**
     * Build transaction draft response
     */
    @SuppressWarnings("unchecked")
    private NLPResponse buildTransactionDraftResponse(Map<String, Object> entities, String userId, 
                                                      Map<String, Object> context, Double confidence) {
        ConfirmDraftData.TransactionDraft.TransactionDraftBuilder draftBuilder = 
            ConfirmDraftData.TransactionDraft.builder();
        
        List<String> needConfirmFields = new ArrayList<>();
        List<ConfirmDraftData.AutoFilledField> autoFilledFields = new ArrayList<>();
        
        // Amount
        BigDecimal amount = getBigDecimalValue(entities.get("amount"));
        if (amount != null) {
            draftBuilder.amount(amount);
            autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                .field("amount")
                .value(amount)
                .confidence(confidence)
                .build());
        } else {
            needConfirmFields.add("amount");
        }
        
        // Transaction type
        String transactionType = (String) entities.get("transactionType");
        if (transactionType != null) {
            draftBuilder.type(transactionType);
        } else {
            // Default to EXPENSE if amount is negative or not specified
            draftBuilder.type("EXPENSE");
        }
        
        // Category
        Map<String, Object> categoryMatch = (Map<String, Object>) entities.get("categoryMatch");
        if (categoryMatch != null) {
            String categoryId = (String) categoryMatch.get("id");
            Double catConfidence = getDoubleValue(categoryMatch.get("confidence"));
            if (categoryId != null && catConfidence != null && catConfidence > 0.7) {
                Category category = categoryRepository.findByIdAndDeletedFalse(categoryId).orElse(null);
                if (category != null) {
                    draftBuilder.categoryId(categoryId);
                    draftBuilder.categoryName(category.getName());
                    autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                        .field("categoryId")
                        .value(category.getName())
                        .confidence(catConfidence)
                        .build());
                } else {
                    needConfirmFields.add("categoryId");
                }
            } else {
                needConfirmFields.add("categoryId");
            }
        } else {
            needConfirmFields.add("categoryId");
        }
        
        // Account
        Map<String, Object> accountMatch = (Map<String, Object>) entities.get("accountMatch");
        if (accountMatch != null) {
            String accountId = (String) accountMatch.get("id");
            Double accConfidence = getDoubleValue(accountMatch.get("confidence"));
            if (accountId != null && accConfidence != null && accConfidence > 0.7) {
                Account account = accountRepository.findByIdAndUserIdAndDeletedFalse(accountId, userId).orElse(null);
                if (account != null) {
                    draftBuilder.accountId(accountId);
                    draftBuilder.accountName(account.getName());
                    autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                        .field("accountId")
                        .value(account.getName())
                        .confidence(accConfidence)
                        .build());
                } else {
                    needConfirmFields.add("accountId");
                }
            } else {
                needConfirmFields.add("accountId");
            }
        } else {
            needConfirmFields.add("accountId");
        }
        
        // Date
        String dateStr = (String) entities.get("date");
        LocalDateTime occurredAt = null;
        if (dateStr != null) {
            occurredAt = parseDate(dateStr);
        }
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
        
        // Convert to GMT+7 timezone string
        String occurredAtStr = formatDateTimeWithTimezone(occurredAt);
        draftBuilder.occurredAt(occurredAtStr);
        autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
            .field("occurredAt")
            .value(occurredAtStr)
            .confidence(confidence)
            .build());
        
        // Note
        String note = (String) entities.get("note");
        if (note != null && !note.isEmpty()) {
            draftBuilder.note(note);
            autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                .field("note")
                .value(note)
                .confidence(confidence)
                .build());
        }
        
        // Currency
        draftBuilder.currency("VND");
        
        ConfirmDraftData.TransactionDraft draft = draftBuilder.build();
        
        ConfirmDraftData confirmData = ConfirmDraftData.builder()
            .draftType(ConfirmDraftData.DraftType.TRANSACTION)
            .draft(draft)
            .needConfirmFields(needConfirmFields)
            .autoFilledFields(autoFilledFields)
            .build();
        
        return NLPResponse.builder()
            .responseType(NLPResponse.ResponseType.CONFIRM_DRAFT)
            .intent(NLPResponse.Intent.CREATE_TRANSACTION)
            .confidence(confidence)
            .message("Tạo giao dịch chi tiêu")
            .data(confirmData)
            .build();
    }
    
    /**
     * Build receivable draft response
     */
    @SuppressWarnings("unchecked")
    private NLPResponse buildReceivableDraftResponse(Map<String, Object> entities, String userId, Double confidence) {
        ConfirmDraftData.ReceivableDraft.ReceivableDraftBuilder draftBuilder = 
            ConfirmDraftData.ReceivableDraft.builder();
        
        List<String> needConfirmFields = new ArrayList<>();
        List<ConfirmDraftData.AutoFilledField> autoFilledFields = new ArrayList<>();
        
        // Counterparty name
        String counterparty = (String) entities.get("counterparty");
        if (counterparty != null && !counterparty.isEmpty()) {
            draftBuilder.counterpartyName(counterparty);
            autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                .field("counterpartyName")
                .value(counterparty)
                .confidence(confidence)
                .build());
        } else {
            needConfirmFields.add("counterpartyName");
        }
        
        // Amount
        BigDecimal amount = getBigDecimalValue(entities.get("amount"));
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            draftBuilder.amount(amount);
            autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                .field("amount")
                .value(amount)
                .confidence(confidence)
                .build());
        } else {
            needConfirmFields.add("amount");
        }
        
        // Currency
        draftBuilder.currency("VND");
        
        // Date
        String dateStr = (String) entities.get("date");
        LocalDateTime occurredAt = null;
        if (dateStr != null) {
            occurredAt = parseDate(dateStr);
        }
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
        
        // Convert to GMT+7 timezone string
        String occurredAtStr = formatDateTimeWithTimezone(occurredAt);
        draftBuilder.occurredAt(occurredAtStr);
        autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
            .field("occurredAt")
            .value(occurredAtStr)
            .confidence(confidence)
            .build());
        
        // Note
        String note = (String) entities.get("note");
        if (note != null && !note.isEmpty()) {
            draftBuilder.note(note);
            autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                .field("note")
                .value(note)
                .confidence(confidence)
                .build());
        }
        
        ConfirmDraftData.ReceivableDraft draft = draftBuilder.build();
        
        ConfirmDraftData confirmData = ConfirmDraftData.builder()
            .draftType(ConfirmDraftData.DraftType.RECEIVABLE)
            .draft(draft)
            .needConfirmFields(needConfirmFields)
            .autoFilledFields(autoFilledFields)
            .build();
        
        return NLPResponse.builder()
            .responseType(NLPResponse.ResponseType.CONFIRM_DRAFT)
            .intent(NLPResponse.Intent.CREATE_RECEIVABLE)
            .confidence(confidence)
            .message("Tạo khoản cho vay")
            .data(confirmData)
            .build();
    }
    
    /**
     * Build liability draft response
     */
    @SuppressWarnings("unchecked")
    private NLPResponse buildLiabilityDraftResponse(Map<String, Object> entities, String userId, Double confidence) {
        ConfirmDraftData.LiabilityDraft.LiabilityDraftBuilder draftBuilder = 
            ConfirmDraftData.LiabilityDraft.builder();
        
        List<String> needConfirmFields = new ArrayList<>();
        List<ConfirmDraftData.AutoFilledField> autoFilledFields = new ArrayList<>();
        
        // Counterparty name
        String counterparty = (String) entities.get("counterparty");
        if (counterparty != null && !counterparty.isEmpty()) {
            draftBuilder.counterpartyName(counterparty);
            autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                .field("counterpartyName")
                .value(counterparty)
                .confidence(confidence)
                .build());
        } else {
            needConfirmFields.add("counterpartyName");
        }
        
        // Amount
        BigDecimal amount = getBigDecimalValue(entities.get("amount"));
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            draftBuilder.amount(amount);
            autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                .field("amount")
                .value(amount)
                .confidence(confidence)
                .build());
        } else {
            needConfirmFields.add("amount");
        }
        
        // Currency
        draftBuilder.currency("VND");
        
        // Date
        String dateStr = (String) entities.get("date");
        LocalDateTime occurredAt = null;
        if (dateStr != null) {
            occurredAt = parseDate(dateStr);
        }
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
        
        // Convert to GMT+7 timezone string
        String occurredAtStr = formatDateTimeWithTimezone(occurredAt);
        draftBuilder.occurredAt(occurredAtStr);
        autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
            .field("occurredAt")
            .value(occurredAtStr)
            .confidence(confidence)
            .build());
        
        // Note
        String note = (String) entities.get("note");
        if (note != null && !note.isEmpty()) {
            draftBuilder.note(note);
            autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                .field("note")
                .value(note)
                .confidence(confidence)
                .build());
        }
        
        ConfirmDraftData.LiabilityDraft draft = draftBuilder.build();
        
        ConfirmDraftData confirmData = ConfirmDraftData.builder()
            .draftType(ConfirmDraftData.DraftType.LIABILITY)
            .draft(draft)
            .needConfirmFields(needConfirmFields)
            .autoFilledFields(autoFilledFields)
            .build();
        
        return NLPResponse.builder()
            .responseType(NLPResponse.ResponseType.CONFIRM_DRAFT)
            .intent(NLPResponse.Intent.CREATE_LIABILITY)
            .confidence(confidence)
            .message("Tạo khoản nợ")
            .data(confirmData)
            .build();
    }
    
    /**
     * Build settlement draft response
     * Settlement có thể là:
     * - Nhận tiền trả nợ (RECEIVABLE settlement) - "Long trả 100k"
     * - Trả nợ (LIABILITY settlement) - "Trả nợ anh Hùng 80k"
     */
    @SuppressWarnings("unchecked")
    private NLPResponse buildSettlementDraftResponse(Map<String, Object> entities, String userId, 
                                                      Map<String, Object> context, Double confidence) {
        ConfirmDraftData.SettlementDraft.SettlementDraftBuilder draftBuilder = 
            ConfirmDraftData.SettlementDraft.builder();
        
        List<String> needConfirmFields = new ArrayList<>();
        List<ConfirmDraftData.AutoFilledField> autoFilledFields = new ArrayList<>();
        
        // Determine settlement type: RECEIVABLE or LIABILITY
        // Check if there's a receivableMatch or liabilityMatch in entities
        Map<String, Object> receivableMatch = (Map<String, Object>) entities.get("receivableMatch");
        Map<String, Object> liabilityMatch = (Map<String, Object>) entities.get("liabilityMatch");
        
        String counterparty = (String) entities.get("counterparty");
        String settlementType = null;
        String receivableId = null;
        String liabilityId = null;
        String counterpartyName = null;
        
        // Try to match with existing receivables/liabilities
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> openReceivables = (List<Map<String, Object>>) context.get("openReceivables");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> openLiabilities = (List<Map<String, Object>>) context.get("openLiabilities");
        
        if (receivableMatch != null) {
            String recId = (String) receivableMatch.get("id");
            Double recConfidence = getDoubleValue(receivableMatch.get("confidence"));
            if (recId != null && recConfidence != null && recConfidence > 0.7) {
                Receivable receivable = receivableRepository.findByIdAndUserIdAndDeletedFalse(recId, userId).orElse(null);
                if (receivable != null) {
                    settlementType = "RECEIVABLE";
                    receivableId = recId;
                    counterpartyName = receivable.getCounterpartyName();
                    autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                        .field("receivableId")
                        .value(receivable.getCounterpartyName())
                        .confidence(recConfidence)
                        .build());
                }
            }
        } else if (liabilityMatch != null) {
            String liabId = (String) liabilityMatch.get("id");
            Double liabConfidence = getDoubleValue(liabilityMatch.get("confidence"));
            if (liabId != null && liabConfidence != null && liabConfidence > 0.7) {
                Liability liability = liabilityRepository.findByIdAndUserIdAndDeletedFalse(liabId, userId).orElse(null);
                if (liability != null) {
                    settlementType = "LIABILITY";
                    liabilityId = liabId;
                    counterpartyName = liability.getCounterpartyName();
                    autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                        .field("liabilityId")
                        .value(liability.getCounterpartyName())
                        .confidence(liabConfidence)
                        .build());
                }
            }
        } else if (counterparty != null && !counterparty.isEmpty()) {
            // Try to match by counterparty name
            // Check receivables first (nhận tiền trả nợ)
            if (openReceivables != null) {
                for (Map<String, Object> rec : openReceivables) {
                    String recCounterparty = (String) rec.get("counterpartyName");
                    if (recCounterparty != null && recCounterparty.toLowerCase().contains(counterparty.toLowerCase())) {
                        settlementType = "RECEIVABLE";
                        receivableId = (String) rec.get("id");
                        counterpartyName = recCounterparty;
                        autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                            .field("receivableId")
                            .value(recCounterparty)
                            .confidence(0.8)
                            .build());
                        break;
                    }
                }
            }
            
            // If not found in receivables, check liabilities (trả nợ)
            if (settlementType == null && openLiabilities != null) {
                for (Map<String, Object> liab : openLiabilities) {
                    String liabCounterparty = (String) liab.get("counterpartyName");
                    if (liabCounterparty != null && liabCounterparty.toLowerCase().contains(counterparty.toLowerCase())) {
                        settlementType = "LIABILITY";
                        liabilityId = (String) liab.get("id");
                        counterpartyName = liabCounterparty;
                        autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                            .field("liabilityId")
                            .value(liabCounterparty)
                            .confidence(0.8)
                            .build());
                        break;
                    }
                }
            }
            
            // If still not found, default to LIABILITY (trả nợ) if counterparty is provided
            if (settlementType == null) {
                settlementType = "LIABILITY";
                counterpartyName = counterparty;
                needConfirmFields.add("liabilityId");
            }
        } else {
            // No counterparty provided, need user to select
            needConfirmFields.add("type");
            needConfirmFields.add("receivableId");
            needConfirmFields.add("liabilityId");
        }
        
        if (settlementType != null) {
            draftBuilder.type(settlementType);
            if (receivableId != null) {
                draftBuilder.receivableId(receivableId);
            }
            if (liabilityId != null) {
                draftBuilder.liabilityId(liabilityId);
            }
            if (counterpartyName != null) {
                draftBuilder.counterpartyName(counterpartyName);
            }
        } else {
            needConfirmFields.add("type");
        }
        
        // Amount
        BigDecimal amount = getBigDecimalValue(entities.get("amount"));
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            draftBuilder.amount(amount);
            autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                .field("amount")
                .value(amount)
                .confidence(confidence)
                .build());
        } else {
            needConfirmFields.add("amount");
        }
        
        // Account
        Map<String, Object> accountMatch = (Map<String, Object>) entities.get("accountMatch");
        if (accountMatch != null) {
            String accountId = (String) accountMatch.get("id");
            Double accConfidence = getDoubleValue(accountMatch.get("confidence"));
            if (accountId != null && accConfidence != null && accConfidence > 0.7) {
                Account account = accountRepository.findByIdAndUserIdAndDeletedFalse(accountId, userId).orElse(null);
                if (account != null) {
                    draftBuilder.accountId(accountId);
                    draftBuilder.accountName(account.getName());
                    autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                        .field("accountId")
                        .value(account.getName())
                        .confidence(accConfidence)
                        .build());
                } else {
                    needConfirmFields.add("accountId");
                }
            } else {
                needConfirmFields.add("accountId");
            }
        } else {
            needConfirmFields.add("accountId");
        }
        
        // Currency
        draftBuilder.currency("VND");
        
        // Date
        String dateStr = (String) entities.get("date");
        LocalDateTime settledAt = null;
        if (dateStr != null) {
            settledAt = parseDate(dateStr);
        }
        if (settledAt == null) {
            settledAt = LocalDateTime.now();
        }
        
        // Convert to GMT+7 timezone string
        String settledAtStr = formatDateTimeWithTimezone(settledAt);
        draftBuilder.settledAt(settledAtStr);
        autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
            .field("settledAt")
            .value(settledAtStr)
            .confidence(confidence)
            .build());
        
        // Note
        String note = (String) entities.get("note");
        if (note != null && !note.isEmpty()) {
            draftBuilder.note(note);
            autoFilledFields.add(ConfirmDraftData.AutoFilledField.builder()
                .field("note")
                .value(note)
                .confidence(confidence)
                .build());
        }
        
        ConfirmDraftData.SettlementDraft draft = draftBuilder.build();
        
        ConfirmDraftData confirmData = ConfirmDraftData.builder()
            .draftType(ConfirmDraftData.DraftType.SETTLEMENT)
            .draft(draft)
            .needConfirmFields(needConfirmFields)
            .autoFilledFields(autoFilledFields)
            .build();
        
        return NLPResponse.builder()
            .responseType(NLPResponse.ResponseType.CONFIRM_DRAFT)
            .intent(NLPResponse.Intent.CREATE_SETTLEMENT)
            .confidence(confidence)
            .message(settlementType != null && settlementType.equals("RECEIVABLE") 
                ? "Nhận tiền trả nợ" 
                : "Trả nợ")
            .data(confirmData)
            .build();
    }
    
    /**
     * Build query response (simplified for Phase 1)
     */
    private NLPResponse buildQueryResponse(Map<String, Object> entities, String userId, Double confidence) {
        // TODO: Implement in Phase 3
        return buildErrorResponse("NOT_IMPLEMENTED", "Tính năng truy vấn chưa được hỗ trợ trong Phase 1", null);
    }
    
    /**
     * Build error response
     */
    private NLPResponse buildErrorResponse(String code, String message, String details) {
        ErrorData errorData = ErrorData.builder()
            .code(code)
            .message(message)
            .details(details)
            .build();
        
        return NLPResponse.builder()
            .responseType(NLPResponse.ResponseType.ERROR)
            .intent(NLPResponse.Intent.UNKNOWN)
            .confidence(0.0)
            .message(message)
            .data(errorData)
            .build();
    }
    
    // Helper methods
    private Double getDoubleValue(Object value) {
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
    
    private BigDecimal getBigDecimalValue(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Parse date string from various formats
     * Supports: ISO format (YYYY-MM-DDTHH:mm:ss), "d/M/yyyy", "d/M" (assumes current year)
     * All dates are interpreted as GMT+7 (Asia/Ho_Chi_Minh)
     */
    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        
        try {
            // Try ISO format first
            return LocalDateTime.parse(dateStr, ISO_FORMATTER);
        } catch (Exception e1) {
            try {
                // Try "d/M/yyyy" format (e.g., "10/1/2026")
                return LocalDate.parse(dateStr, DATE_FORMATTER).atStartOfDay();
            } catch (Exception e2) {
                try {
                    // Try "d/M" format (e.g., "10/1") - assume current year
                    LocalDate date = LocalDate.parse(dateStr, DATE_SHORT_FORMATTER);
                    int currentYear = LocalDate.now().getYear();
                    date = date.withYear(currentYear);
                    return date.atStartOfDay();
                } catch (Exception e3) {
                    log.warn("Failed to parse date: {}", dateStr);
                    return null;
                }
            }
        }
    }
    
    /**
     * Format LocalDateTime to ISO string with GMT+7 timezone
     * Output format: "YYYY-MM-DDTHH:mm:ss+07:00"
     */
    private String formatDateTimeWithTimezone(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        // Convert LocalDateTime to ZonedDateTime with GMT+7
        ZonedDateTime zonedDateTime = dateTime.atZone(VN_TIMEZONE);
        // Format with timezone offset
        return zonedDateTime.format(ISO_WITH_TZ_FORMATTER);
    }
}
