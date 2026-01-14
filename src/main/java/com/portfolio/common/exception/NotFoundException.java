package com.portfolio.common.exception;

/**
 * Exception for resource not found errors
 */
public class NotFoundException extends BusinessException {
    
    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }
    
    public NotFoundException(String resource, String identifier) {
        super("NOT_FOUND", String.format("%s with identifier '%s' not found", resource, identifier));
    }
}
