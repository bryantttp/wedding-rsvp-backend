package com.rsvp_backend.config;

import java.io.IOException;

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

    @Value("${firebase.serviceAccount}")
    private Resource serviceAccount;

    @Value("${firebase.projectId}")
    private String projectId;

    @PostConstruct
    public void init() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) return;

        GoogleCredentials creds = GoogleCredentials.fromStream(serviceAccount.getInputStream());

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
