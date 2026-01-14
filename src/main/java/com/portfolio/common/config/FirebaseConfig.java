package com.portfolio.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase configuration để initialize Firebase Admin SDK
 */
@Slf4j
@Configuration
public class FirebaseConfig {
    
    @Value("${firebase.service-account}")
    private String serviceAccountJson;
    
    @Value("${firebase.storage-bucket}")
    private String storageBucket;
    
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // Kiểm tra xem Firebase đã được initialize chưa
        if (FirebaseApp.getApps().isEmpty()) {
            log.info("Initializing Firebase App with bucket: {}", storageBucket);
            
            // Parse service account JSON từ string
            InputStream serviceAccountStream = new ByteArrayInputStream(serviceAccountJson.getBytes());
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .setStorageBucket(storageBucket)
                    .build();
            
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase App initialized successfully");
            
            return app;
        } else {
            log.info("Firebase App already initialized");
            return FirebaseApp.getInstance();
        }
    }
    
    @Bean
    public StorageClient storageClient(FirebaseApp firebaseApp) {
        return StorageClient.getInstance(firebaseApp);
    }
}
