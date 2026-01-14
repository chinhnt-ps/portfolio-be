package com.portfolio.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để đánh dấu endpoint cần rate limiting
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    
    /**
     * Rate limit type
     */
    RateLimitType value();
    
    /**
     * Rate limit types
     */
    enum RateLimitType {
        LOGIN,              // 5 attempts/minute
        FORGOT_PASSWORD,    // 3 attempts/hour
        REGISTER,           // 3 attempts/hour
        FILE_UPLOAD         // 10 requests/minute
    }
}
