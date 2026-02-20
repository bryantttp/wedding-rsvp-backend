package com.rsvp_backend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public ResponseEntity<?> saveRsvp(@Valid @RequestBody RsvpRequestDto rsvpRequest) throws Exception{
        String email = rsvpRequest.getEmail().trim().toLowerCase();

        // ✅ 1) Check if email already exists
        QuerySnapshot existing = firestore.collection("rsvps")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .get();

        if (!existing.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("This email has already RSVP’d.");
        }

        List<String> cleanedAttendees = rsvpRequest.getListOfAttendees().stream()
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .toList();

        Map<String, Object> doc = new HashMap<>();
        doc.put("name", rsvpRequest.getName());
        doc.put("email", rsvpRequest.getEmail());
        if (cleanedAttendees != null && !cleanedAttendees.isEmpty()) {
            doc.put("listOfAttendees", cleanedAttendees);
        }
        doc.put("createdAt", FieldValue.serverTimestamp());

        firestore.collection("rsvps").add(doc).get();

        try {
            emailService.sendRsvpConfirmation(
                email,
                rsvpRequest.getName().trim(),
                cleanedAttendees
            );
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }

        return ResponseEntity.ok("RSVP received");
    }

    @GetMapping("/admin/rsvps")
    public ResponseEntity<?> getRsvps() throws Exception {
        var snapshots = firestore.collection("rsvps")
                .orderBy("createdAt") // ✅ only sort by created time now
                .get()
                .get();

        List<Map<String, Object>> result = new ArrayList<>();
        for (var doc : snapshots.getDocuments()) {
            Map<String, Object> data = doc.getData();
            data.put("id", doc.getId());
            result.add(data);
        }

        return ResponseEntity.ok(result);
    }
}