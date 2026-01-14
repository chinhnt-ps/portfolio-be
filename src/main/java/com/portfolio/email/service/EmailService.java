package com.portfolio.email.service;

/**
 * Email service interface
 */
public interface EmailService {
    
    /**
     * Send verification email với code
     * 
     * @param to Email recipient
     * @param code Verification code
     */
    void sendVerificationEmail(String to, String code);
    
    /**
     * Send password reset email với code
     * 
     * @param to Email recipient
     * @param code Verification code
     */
    void sendPasswordResetEmail(String to, String code);
}
