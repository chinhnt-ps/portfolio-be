package com.portfolio.wallet.controller;

import com.portfolio.common.annotation.RateLimited;
import com.portfolio.common.dto.ApiResponse;
import com.portfolio.wallet.dto.request.ParseTransactionRequest;
import com.portfolio.wallet.dto.response.NLPResponse;
import com.portfolio.wallet.service.NLPService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * NLP Controller for parsing transaction text using AI
 * 
 * Endpoints:
 * - POST /api/v1/nlp/parse-transaction - Parse text input
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/nlp")
@RequiredArgsConstructor
public class NLPController {
    
    private final NLPService nlpService;
    
    /**
     * Parse transaction text input
     * 
     * @param request Parse request with text input
     * @param authentication Authentication object (contains user info)
     * @return NLP response with draft or options
     */
    @PostMapping("/parse-transaction")
    @RateLimited(RateLimited.RateLimitType.WALLET_API)
    public ResponseEntity<ApiResponse<NLPResponse>> parseTransaction(
            @Valid @RequestBody ParseTransactionRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        
        log.debug("Parsing transaction text for user: {}, text: {}", userId, request.getText());
        
        NLPResponse response = nlpService.parseTransaction(request, userId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Text parsed successfully"));
    }
}
