package com.portfolio.common.exception;

/**
 * Exception for resource conflict errors (e.g., duplicate email)
 */
public class ConflictException extends BusinessException {
    
    public ConflictException(String message) {
        super("CONFLICT", message);
    }
}
