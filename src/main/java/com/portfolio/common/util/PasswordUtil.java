package com.portfolio.common.util;

import com.portfolio.common.dto.ApiResponse;
import com.portfolio.common.exception.ValidationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for password operations
 */
public class PasswordUtil {
    
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    
    /**
     * Hash password với BCrypt (cost factor 12)
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return encoder.encode(plainPassword);
    }
    
    /**
     * Verify password matches hash
     */
    public static boolean matches(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return encoder.matches(plainPassword, hashedPassword);
    }
    
    /**
     * Validate password strength
     * 
     * @param password Password to validate
     * @throws ValidationException if password doesn't meet requirements
     */
    public static void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password is required");
        }
        
        List<ApiResponse.FieldError> errors = new ArrayList<>();
        
        if (password.length() < MIN_LENGTH) {
            errors.add(ApiResponse.FieldError.builder()
                    .field("password")
                    .reason("MIN_LENGTH_8")
                    .build());
        }
        
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            errors.add(ApiResponse.FieldError.builder()
                    .field("password")
                    .reason("REQUIRE_UPPERCASE")
                    .build());
        }
        
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            errors.add(ApiResponse.FieldError.builder()
                    .field("password")
                    .reason("REQUIRE_LOWERCASE")
                    .build());
        }
        
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            errors.add(ApiResponse.FieldError.builder()
                    .field("password")
                    .reason("REQUIRE_DIGIT")
                    .build());
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException("Password does not meet requirements", errors);
        }
    }
}
