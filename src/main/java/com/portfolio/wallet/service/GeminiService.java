package com.portfolio.wallet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.*;

/**
 * Service for interacting with Google Gemini API
 */
@Slf4j
@Service
public class GeminiService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public GeminiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    @Value("${gemini.api.key:}")
    private String apiKey;
    
    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemma-3-4b-it:generateContent}")
    private String apiUrl;
    
    @Value("${gemini.api.timeout:5000}")
    private int timeoutMs;
    
    @Value("${gemini.api.max-retries:3}")
    private int maxRetries;
    
    /**
     * Parse user text input using Gemini AI with retry logic
     * 
     * @param userText User's text input
     * @param context Context data (accounts, categories, receivables, liabilities)
     * @param timezone User's timezone
     * @param locale User's locale
     * @return Parsed JSON response from Gemini
     */
    public Map<String, Object> parseText(String userText, Map<String, Object> context, 
                                         String timezone, String locale) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Gemini API key not configured");
            throw new IllegalStateException("Gemini API key is not configured");
        }
        
        // Build prompt and request body once
        String prompt = buildPrompt(userText, context, timezone, locale);
        Map<String, Object> requestBody = buildRequestBody(prompt);
        String url = apiUrl + "?key=" + apiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        // Retry logic with exponential backoff
        Exception lastException = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    // Exponential backoff: 1s, 2s, 4s
                    long delayMs = (long) Math.pow(2, attempt - 1) * 1000;
                    log.info("Retrying Gemini API call after {}ms (attempt {}/{})", delayMs, attempt + 1, maxRetries + 1);
                    Thread.sleep(delayMs);
                }
                
                log.debug("Calling Gemini API with prompt length: {} (attempt {})", prompt.length(), attempt + 1);
                @SuppressWarnings("unchecked")
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, 
                    HttpMethod.POST, 
                    entity, 
                    (Class<Map<String, Object>>) (Class<?>) Map.class
                );
                
                // Extract response text
                String responseText = extractResponseText(response.getBody());
                log.debug("Gemini response text length: {}", responseText.length());
                
                // Parse JSON from response text
                return parseJsonResponse(responseText);
                
            } catch (RestClientException e) {
                lastException = e;
                String errorMessage = e.getMessage();
                
                // Check if it's a rate limit error (429)
                if (errorMessage != null && errorMessage.contains("429")) {
                    log.warn("Rate limit exceeded (429), will retry. Attempt {}/{}", attempt + 1, maxRetries + 1);
                    if (attempt < maxRetries) {
                        continue; // Retry
                    }
                }
                
                // For other errors or max retries reached
                log.error("Error calling Gemini API (attempt {})", attempt + 1, e);
                if (attempt == maxRetries) {
                    throw new RuntimeException("Failed to call Gemini API after " + (maxRetries + 1) + " attempts: " + e.getMessage(), e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for retry", e);
            } catch (Exception e) {
                log.error("Error parsing Gemini response", e);
                throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage(), e);
            }
        }
        
        // Should not reach here, but just in case
        throw new RuntimeException("Failed to call Gemini API after retries", lastException);
    }
    
    /**
     * Build prompt for Gemini
     */
    private String buildPrompt(String userText, Map<String, Object> context, 
                               String timezone, String locale) {
        StringBuilder prompt = new StringBuilder();
        
        // System instructions
        prompt.append("Bạn là assistant parse text thành structured data cho ứng dụng quản lý chi tiêu.\n");
        prompt.append("Luôn trả về JSON theo schema đã định nghĩa.\n");
        prompt.append("Không tự tạo data, chỉ extract từ input.\n");
        prompt.append("Khi không chắc chắn, set confidence thấp và needConfirmFields.\n\n");
        
        // Intent classification rules
        prompt.append("PHÂN LOẠI INTENT:\n");
        prompt.append("- CREATE_RECEIVABLE: Khi user \"cho vay\", \"cho mượn\", \"cho ai đó tiền\" (ví dụ: \"cho Nam vay 2tr\", \"cho a Hùng vay 50k\")\n");
        prompt.append("- CREATE_LIABILITY: Khi user \"vay\", \"mượn\", \"nợ ai đó\" (ví dụ: \"vay anh Hùng 5tr\", \"nợ Long 100k\")\n");
        prompt.append("- CREATE_SETTLEMENT: Khi user \"trả nợ\", \"nhận tiền trả nợ\", \"thanh toán\" (ví dụ: \"Trả nợ anh Hùng 80k\", \"Long trả 100k\")\n");
        prompt.append("- CREATE_TRANSACTION: Khi user chi tiêu/thu nhập thông thường (ví dụ: \"ăn bún 50k\", \"lương tháng 1 10tr\")\n");
        prompt.append("- ADJUST_BALANCE: Khi user muốn điều chỉnh số dư tài khoản về một giá trị mới (ví dụ: \"điều chỉnh số dư ví tiền mặt về 2 triệu\", \"cân lại số dư tài khoản A còn 500k\")\n");
        prompt.append("- QUERY_DATA: Khi user hỏi thông tin (ví dụ: \"tháng này chi bao nhiêu?\")\n\n");
        
        // Rules for Vietnamese currency and dates
        prompt.append("QUY TẮC:\n");
        prompt.append("- Số tiền VN: \"50k\" = 50000, \"2tr\" = 2000000, \"1.5 triệu\" = 1500000\n");
        prompt.append("- Ngày: \"hôm nay\" = current date, \"hôm qua\" = yesterday, \"ngày mai\" = tomorrow, \"16/1\" = ngày 16 tháng 1 năm hiện tại\n");
        prompt.append("- Tài khoản: match với danh sách accounts (vcb=Vietcombank, mb=MBBank, tienmat=Tiền mặt)\n");
        prompt.append("- Counterparty: Tên người trong câu (ví dụ: \"a Hùng\", \"Nam\", \"Long\")\n\n");
        
        // Examples
        prompt.append("VÍ DỤ:\n");
        prompt.append("1. \"cho a Hùng vay 50k\" → intent: CREATE_RECEIVABLE, counterparty: \"a Hùng\", amount: 50000\n");
        prompt.append("2. \"vay anh Hùng 5tr\" → intent: CREATE_LIABILITY, counterparty: \"anh Hùng\", amount: 5000000\n");
        prompt.append("3. \"Trả nợ anh Hùng 80k vietcombank\" → intent: CREATE_SETTLEMENT, type: LIABILITY, counterparty: \"anh Hùng\", amount: 80000, accountMatch: vietcombank\n");
        prompt.append("4. \"ăn bún 50k\" → intent: CREATE_TRANSACTION, type: EXPENSE, amount: 50000, categoryMatch: Ăn uống\n");
        prompt.append("5. \"Long trả 100k\" → intent: CREATE_SETTLEMENT, type: RECEIVABLE, counterparty: \"Long\", amount: 100000\n");
        prompt.append("6. \"điều chỉnh số dư ví tiền mặt về 2 triệu\" → intent: ADJUST_BALANCE, accountMatch: ví tiền mặt, targetBalance: 2000000\n\n");
        
        // Context injection
        prompt.append("CONTEXT:\n");
        prompt.append("Current date: ").append(new Date()).append("\n");
        prompt.append("Timezone: ").append(timezone != null ? timezone : "Asia/Ho_Chi_Minh").append("\n");
        prompt.append("Locale: ").append(locale != null ? locale : "vi-VN").append("\n\n");
        
        if (context != null) {
            prompt.append("User accounts: ").append(formatContext(context.get("accounts"))).append("\n");
            prompt.append("User categories: ").append(formatContext(context.get("categories"))).append("\n");
            if (context.containsKey("openReceivables")) {
                prompt.append("Open receivables: ").append(formatContext(context.get("openReceivables"))).append("\n");
            }
            if (context.containsKey("openLiabilities")) {
                prompt.append("Open liabilities: ").append(formatContext(context.get("openLiabilities"))).append("\n");
            }
            prompt.append("\n");
        }
        
        // Output schema
        prompt.append("OUTPUT SCHEMA (JSON):\n");
        prompt.append("{\n");
        prompt.append("  \"intent\": \"CREATE_TRANSACTION\" | \"CREATE_RECEIVABLE\" | \"CREATE_LIABILITY\" | \"CREATE_SETTLEMENT\" | \"ADJUST_BALANCE\" | \"QUERY_DATA\" | \"UNKNOWN\",\n");
        prompt.append("  \"confidence\": 0.0-1.0,\n");
        prompt.append("  \"entities\": {\n");
        prompt.append("    \"amount\": number (optional),\n");
        prompt.append("    \"targetBalance\": number (optional, REQUIRED for ADJUST_BALANCE),\n");
        prompt.append("    \"counterparty\": string (optional, REQUIRED cho RECEIVABLE/LIABILITY/SETTLEMENT),\n");
        prompt.append("    \"accountMatch\": {\"id\": string, \"confidence\": number} (optional, REQUIRED cho ADJUST_BALANCE),\n");
        prompt.append("    \"categoryMatch\": {\"id\": string, \"confidence\": number} (optional, only for TRANSACTION),\n");
        prompt.append("    \"receivableMatch\": {\"id\": string, \"confidence\": number} (optional, for SETTLEMENT type RECEIVABLE),\n");
        prompt.append("    \"liabilityMatch\": {\"id\": string, \"confidence\": number} (optional, for SETTLEMENT type LIABILITY),\n");
        prompt.append("    \"note\": string (optional),\n");
        prompt.append("    \"date\": string ISO format (optional, format: YYYY-MM-DDTHH:mm:ss),\n");
        prompt.append("    \"transactionType\": \"INCOME\" | \"EXPENSE\" | \"TRANSFER\" (optional, only for CREATE_TRANSACTION)\n");
        prompt.append("  },\n");
        prompt.append("  \"ambiguities\": [string] (optional),\n");
        prompt.append("  \"queryType\": string (optional, for QUERY_DATA intent)\n");
        prompt.append("}\n\n");
        prompt.append("LƯU Ý QUAN TRỌNG:\n");
        prompt.append("- Nếu text có từ \"cho vay\", \"cho mượn\", \"cho [tên] vay\" → intent phải là CREATE_RECEIVABLE\n");
        prompt.append("- Nếu text có từ \"vay\", \"mượn\", \"nợ\" (không phải \"cho vay\") → intent phải là CREATE_LIABILITY\n");
        prompt.append("- Nếu text có từ \"trả nợ\", \"nhận tiền\", \"thanh toán\" → intent phải là CREATE_SETTLEMENT\n");
        prompt.append("- Nếu text có từ \"điều chỉnh số dư\", \"chỉnh lại số dư\", \"cân bằng số dư\" → intent phải là ADJUST_BALANCE\n");
        prompt.append("- Chỉ dùng CREATE_TRANSACTION cho chi tiêu/thu nhập thông thường (ăn uống, mua sắm, lương, v.v.)\n\n");
        
        // User input
        prompt.append("USER INPUT:\n");
        prompt.append("\"").append(userText).append("\"\n\n");
        
        prompt.append("Trả về JSON theo schema trên:");
        
        return prompt.toString();
    }
    
    /**
     * Format context data for prompt
     */
    private String formatContext(Object data) {
        if (data == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.warn("Failed to format context data", e);
            return "[]";
        }
    }
    
    /**
     * Build request body for Gemini API
     */
    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        
        // Contents
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        parts.add(part);
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);
        
        // Generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.1); // Lower temperature for more deterministic output
        generationConfig.put("topK", 1);
        generationConfig.put("topP", 0.8);
        generationConfig.put("maxOutputTokens", 2048);
        requestBody.put("generationConfig", generationConfig);
        
        return requestBody;
    }
    
    /**
     * Extract response text from Gemini API response
     */
    @SuppressWarnings("unchecked")
    private String extractResponseText(Map<String, Object> responseBody) {
        if (responseBody == null) {
            throw new RuntimeException("Response body is null");
        }
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("No candidates in Gemini response");
            }
            
            Map<String, Object> firstCandidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            if (content == null) {
                throw new RuntimeException("No content in candidate");
            }
            
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                throw new RuntimeException("No parts in content");
            }
            
            String text = (String) parts.get(0).get("text");
            if (text == null) {
                throw new RuntimeException("No text in parts");
            }
            
            return text;
        } catch (Exception e) {
            log.error("Failed to extract response text from Gemini response", e);
            throw new RuntimeException("Failed to extract response text: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse JSON from response text
     * Gemini might return text with markdown code blocks, so we need to extract JSON
     */
    private Map<String, Object> parseJsonResponse(String responseText) {
        try {
            // Remove markdown code blocks if present
            String cleaned = responseText.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();
            
            // Find JSON object in text
            int jsonStart = cleaned.indexOf("{");
            int jsonEnd = cleaned.lastIndexOf("}");
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                cleaned = cleaned.substring(jsonStart, jsonEnd + 1);
            }
            
            // Parse JSON
            JsonNode jsonNode = objectMapper.readTree(cleaned);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.convertValue(jsonNode, Map.class);
            return result;
        } catch (Exception e) {
            log.error("Failed to parse JSON from response text: {}", responseText, e);
            throw new RuntimeException("Failed to parse JSON response: " + e.getMessage(), e);
        }
    }
}
