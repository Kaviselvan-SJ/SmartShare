package com.smartshare.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseInitializer.class);

    /**
     * Production:
     * Loaded from Render environment variable
     */
    @Value("${FIREBASE_SERVICE_ACCOUNT_JSON:}")
    private String firebaseServiceAccountJson;

    @PostConstruct
    public void initialize() {

        if (!FirebaseApp.getApps().isEmpty()) {
            logger.info(
                    "Firebase Admin SDK already initialized, skipping.");
            return;
        }

        try (InputStream credentialsStream = resolveCredentials()) {

            if (credentialsStream == null) {
                logger.error(
                        "Firebase credentials not found.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(
                            GoogleCredentials.fromStream(
                                    credentialsStream))
                    .build();

            FirebaseApp.initializeApp(options);

            logger.info(
                    "Firebase Admin SDK initialized successfully.");

        } catch (IOException e) {

            logger.error(
                    "Failed to initialize Firebase Admin SDK",
                    e);
        }
    }

    /**
     * Credential loading priority:
     *
     * 1. Render production env variable
     * 2. Local classpath fallback
     */
    private InputStream resolveCredentials() throws IOException {

        /*
         * Production Render deployment
         */
        if (firebaseServiceAccountJson != null
                && !firebaseServiceAccountJson.isBlank()) {

            logger.info(
                    "Loading Firebase credentials from environment variable.");

            return new ByteArrayInputStream(
                    firebaseServiceAccountJson.getBytes(
                            StandardCharsets.UTF_8));
        }

        /*
         * Local development fallback
         */
        logger.info(
                "Loading Firebase credentials from classpath resource.");

        return new ClassPathResource(
                "firebase-service-account.json").getInputStream();
    }
}