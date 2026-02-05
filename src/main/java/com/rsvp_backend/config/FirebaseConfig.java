package com.rsvp_backend.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    /**
     * LOCAL fallback (for your computer only)
     * application.properties:
     * firebase.serviceAccount=classpath:firebase-key.json
     */
    @Value("${firebase.serviceAccount:}")
    private Resource serviceAccountFile;

    @Value("${firebase.projectId}")
    private String projectId;

    @PostConstruct
    public void init() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) return;

        GoogleCredentials creds;

        // ===== 1) Try Render ENV JSON first =====
        String json = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");

        if (json != null && !json.isBlank()) {
            InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
            creds = GoogleCredentials.fromStream(stream);
            System.out.println("🔥 Loaded Firebase credentials from ENV");
        }
        // ===== 2) Fallback to local file (dev only) =====
        else {
            if (serviceAccountFile == null || !serviceAccountFile.exists()) {
                throw new IllegalStateException("No Firebase credentials found (ENV or file)");
            }
            creds = GoogleCredentials.fromStream(serviceAccountFile.getInputStream());
            System.out.println("💻 Loaded Firebase credentials from local file");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(creds)
                .setProjectId(projectId)
                .build();

        FirebaseApp.initializeApp(options);
        System.out.println("✅ Firebase initialized successfully");
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }
}
