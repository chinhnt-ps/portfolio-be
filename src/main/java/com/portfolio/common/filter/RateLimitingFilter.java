package com.portfolio.common.filter;

import com.portfolio.common.annotation.RateLimited;
import com.portfolio.common.config.RateLimitingConfig;
import com.portfolio.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

/**
 * Rate limiting filter/interceptor
 * Checks rate limits before processing requests
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter implements HandlerInterceptor {

    private final ConcurrentMap<String, Bucket> rateLimitBuckets;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Only process if handler is a method handler
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimited rateLimited = handlerMethod.getMethodAnnotation(RateLimited.class);

        // If no rate limit annotation, allow request
        if (rateLimited == null) {
            return true;
        }

        // Get identifier (IP address or user ID)
        String identifier = getIdentifier(request);
        
        // Get or create bucket based on rate limit type
        Bucket bucket = getBucket(identifier, rateLimited.value());

        // Try to consume token
        if (bucket.tryConsume(1)) {
            log.debug("Rate limit check passed for {}: {}", rateLimited.value(), identifier);
            return true;
        }

        // Rate limit exceeded
        log.warn("Rate limit exceeded for {}: {}", rateLimited.value(), identifier);
        sendRateLimitExceededResponse(response, rateLimited.value());
        return false;
    }

    /**
     * Get identifier for rate limiting (IP address or user ID)
     */
    private String getIdentifier(HttpServletRequest request) {
        // Try to get user ID from authentication if available
        // For now, use IP address
        String ipAddress = getClientIpAddress(request);
        return ipAddress;
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Get or create bucket for identifier and rate limit type
     */
    private Bucket getBucket(String identifier, RateLimited.RateLimitType type) {
        String key = type.name() + ":" + identifier;
        
        return rateLimitBuckets.computeIfAbsent(key, k -> {
            log.debug("Creating new bucket for {}: {}", type, identifier);
            return createBucket(type);
        });
    }

    /**
     * Create bucket based on rate limit type
     */
    private Bucket createBucket(RateLimited.RateLimitType type) {
        return switch (type) {
            case LOGIN -> RateLimitingConfig.createLoginBucket();
            case FORGOT_PASSWORD -> RateLimitingConfig.createForgotPasswordBucket();
            case REGISTER -> RateLimitingConfig.createRegisterBucket();
            case FILE_UPLOAD -> RateLimitingConfig.createFileUploadBucket();
            case WALLET_API -> RateLimitingConfig.createWalletApiBucket();
        };
    }

    /**
     * Send rate limit exceeded response
     */
    private void sendRateLimitExceededResponse(HttpServletResponse response, RateLimited.RateLimitType type) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        String message = getRateLimitMessage(type);
        
        ApiResponse<Void> errorResponse = ApiResponse.error(
                "RATE_LIMIT_EXCEEDED",
                message
        );
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    /**
     * Get rate limit message based on type
     */
    private String getRateLimitMessage(RateLimited.RateLimitType type) {
        return switch (type) {
            case LOGIN -> "Quá nhiều lần đăng nhập. Vui lòng thử lại sau 1 phút.";
            case FORGOT_PASSWORD -> "Quá nhiều yêu cầu đặt lại mật khẩu. Vui lòng thử lại sau 1 giờ.";
            case REGISTER -> "Quá nhiều lần đăng ký. Vui lòng thử lại sau 1 giờ.";
            case FILE_UPLOAD -> "Quá nhiều yêu cầu upload file. Vui lòng thử lại sau 1 phút.";
            case WALLET_API -> "Quá nhiều yêu cầu API. Vui lòng thử lại sau 1 phút.";
        };
    }
}
