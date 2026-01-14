package com.portfolio.common.exception;

import com.portfolio.common.dto.ApiResponse;
import lombok.Getter;

import java.util.List;

/**
 * Exception for validation errors
 */
@Getter
public class ValidationException extends BusinessException {
    
    private final List<ApiResponse.FieldError> fieldErrors;
    
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
        this.fieldErrors = null;
    }
    
    public ValidationException(String message, List<ApiResponse.FieldError> fieldErrors) {
        super("VALIDATION_ERROR", message);
        this.fieldErrors = fieldErrors;
    }
    
    public ValidationException(List<ApiResponse.FieldError> fieldErrors) {
        super("VALIDATION_ERROR", "Validation failed");
        this.fieldErrors = fieldErrors;
    }
}
