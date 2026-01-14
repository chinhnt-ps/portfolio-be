package com.portfolio.common.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Rate limiting configuration using Bucket4j
 */
@Slf4j
@Configuration
public class RateLimitingConfig {

    /**
     * In-memory cache for rate limit buckets (key: IP address or user ID, value: Bucket)
     */
    @Bean
    public ConcurrentMap<String, Bucket> rateLimitBuckets() {
        log.info("Rate limiting buckets cache initialized");
        return new ConcurrentHashMap<>();
    }

    /**
     * Create bucket for login endpoint: 5 attempts per minute
     */
    public static Bucket createLoginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                .build();
    }

    /**
     * Create bucket for forgot password endpoint: 3 attempts per hour
     */
    public static Bucket createForgotPasswordBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(3, Refill.intervally(3, Duration.ofHours(1))))
                .build();
    }

    /**
     * Create bucket for register endpoint: 3 attempts per hour
     */
    public static Bucket createRegisterBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(3, Refill.intervally(3, Duration.ofHours(1))))
                .build();
    }

    /**
     * Create bucket for file upload endpoint: 10 requests per minute
     */
    public static Bucket createFileUploadBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
                .build();
    }
}
