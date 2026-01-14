package com.portfolio.common.util;

import java.security.SecureRandom;

/**
 * Utility class for generating verification codes
 */
public class CodeGenerator {
    
    private static final SecureRandom random = new SecureRandom();
    private static final int CODE_LENGTH = 6;
    private static final int MIN_CODE = 100000;
    private static final int MAX_CODE = 999999;
    
    /**
     * Generate 6-digit numeric verification code
     * 
     * @return 6-digit code as string
     */
    public static String generateVerificationCode() {
        int code = random.nextInt(MAX_CODE - MIN_CODE + 1) + MIN_CODE;
        return String.valueOf(code);
    }
}
