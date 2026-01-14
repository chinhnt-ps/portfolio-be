package com.portfolio.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Optional;

/**
 * MongoDB configuration
 * 
 * Connection pooling và other settings are configured via application.yml
 * Spring Boot 3.x auto-configures MongoDB connection from spring.data.mongodb.uri
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.portfolio")
@EnableMongoAuditing
public class MongoConfig {
    // MongoDB connection is configured via application.yml
    // Connection pooling is handled automatically by Spring Data MongoDB
    // Default connection pool settings:
    // - maxPoolSize: 100
    // - minPoolSize: 0
    // - maxIdleTimeMS: 0
    // - maxLifeTimeMS: 0
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        // For @CreatedBy and @LastModifiedBy (not used in this project)
        return () -> Optional.of("system");
    }
}
