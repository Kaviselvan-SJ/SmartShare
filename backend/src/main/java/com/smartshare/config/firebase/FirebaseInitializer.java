package com.smartshare.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseInitializer.class);

    @Value("${FIREBASE_SERVICE_ACCOUNT_JSON:}")
    private String firebaseServiceAccountJson;

    @Value("${FIREBASE_CREDENTIALS_PATH:classpath:firebase-service-account.json}")
    private String firebaseCredentialsPath;

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            logger.info("Firebase Admin SDK already initialized, skipping.");
            return;
        }

        try {
            InputStream credentialsStream = resolveCredentials();
            if (credentialsStream == null) {
                logger.error("Firebase credentials not found. Set FIREBASE_SERVICE_ACCOUNT_JSON or FIREBASE_CREDENTIALS_PATH.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();

            FirebaseApp.initializeApp(options);
            logger.info("Firebase Admin SDK initialized successfully.");
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase Admin SDK", e);
        }
    }

    /**
     * Resolves Firebase credentials from one of two sources:
     * 1. FIREBASE_SERVICE_ACCOUNT_JSON env var (production — Render)
     * 2. FIREBASE_CREDENTIALS_PATH file path (local development)
     */
    private InputStream resolveCredentials() throws IOException {
        if (firebaseServiceAccountJson != null && !firebaseServiceAccountJson.isBlank()) {
            logger.info("Loading Firebase credentials from FIREBASE_SERVICE_ACCOUNT_JSON environment variable.");
            return new ByteArrayInputStream(firebaseServiceAccountJson.getBytes(StandardCharsets.UTF_8));
        }

        logger.info("Loading Firebase credentials from file: {}", firebaseCredentialsPath);
        return new FileInputStream(ResourceUtils.getFile(firebaseCredentialsPath));
    }
}
