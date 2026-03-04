package com.rsvp_backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.rsvp_backend.dto.RsvpRequestDto;
import com.rsvp_backend.service.EmailService;

import jakarta.validation.Valid;

@RestController
public class FirebaseController {

    private final Firestore firestore;

    @Autowired
    private EmailService emailService;

    public FirebaseController(Firestore firestore) {
        this.firestore = firestore;
    }

    @GetMapping("/firebase-test")
    public String test() throws Exception {
        firestore.collection("test")
                .add(Map.of("message", "Firebase works!", "ts", System.currentTimeMillis()))
                .get();
        return "Firebase write successful ✅";
    }

    @PostMapping("/save-rsvp")
    public ResponseEntity<?> saveRsvp(@Valid @RequestBody RsvpRequestDto rsvpRequest) throws Exception {

        String name = rsvpRequest.getName().trim();
        String email = rsvpRequest.getEmail().trim().toLowerCase();
        int totalGuests = rsvpRequest.getTotalGuests() == null ? 1 : rsvpRequest.getTotalGuests();

        // Clamp between 1 and 10
        totalGuests = Math.max(1, Math.min(10, totalGuests));

        // ✅ 1) Check if email already exists
        QuerySnapshot existing = firestore.collection("rsvps")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .get();

        if (!existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("This email has already RSVP’d.");
        }

        // ✅ 2) Save to Firestore
        Map<String, Object> doc = new HashMap<>();
        doc.put("name", name);
        doc.put("email", email);
        doc.put("totalGuests", totalGuests);
        doc.put("createdAt", FieldValue.serverTimestamp());

        firestore.collection("rsvps").add(doc).get();

        // ✅ 3) Send confirmation email (optional)
        try {
            emailService.sendRsvpConfirmation(email, name, totalGuests);
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }

        return ResponseEntity.ok("RSVP received");
    }
}