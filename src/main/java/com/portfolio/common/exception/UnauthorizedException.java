package com.portfolio.common.exception;

/**
 * Exception for authentication/authorization errors
 */
public class UnauthorizedException extends BusinessException {
    
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
    
    public UnauthorizedException() {
        super("UNAUTHORIZED", "Authentication required");
    }
}
