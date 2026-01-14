package com.portfolio.security.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT Token Provider for generating and validating JWT tokens
 */
@Slf4j
@Component
public class JwtTokenProvider {
    
    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;
    
    @Value("${spring.security.jwt.expiration:3600}")
    private long jwtExpiration; // in seconds
    
    /**
     * Generate JWT token
     * 
     * @param userId User ID
     * @param email User email
     * @param role User role
     * @return JWT token string
     */
    public String generateToken(String userId, String email, String role) {
        return generateToken(userId, email, role, jwtExpiration);
    }
    
    /**
     * Generate JWT token with custom expiration
     * 
     * @param userId User ID
     * @param email User email
     * @param role User role
     * @param expirationSeconds Expiration time in seconds
     * @return JWT token string
     */
    public String generateToken(String userId, String email, String role, long expirationSeconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationSeconds * 1000);
        
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Extract user ID from token
     */
    public String getUserIdFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    /**
     * Extract email from token
     */
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("email", String.class));
    }
    
    /**
     * Extract role from token
     */
    public String getRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("role", String.class));
    }
    
    /**
     * Extract expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    /**
     * Extract claim from token
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Get all claims from token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Check if token is expired
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration", e);
            return true;
        }
    }
    
    /**
     * Validate token
     * 
     * @param token JWT token
     * @return true if token is valid
     */
    public Boolean validateToken(String token) {
        try {
            // Parse token to check if it's valid
            getAllClaimsFromToken(token);
            // Check if expired
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Invalid JWT token", e);
            return false;
        }
    }
    
    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        // Ensure key is at least 256 bits (32 bytes) for HS256
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 256 bits (32 characters)");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
