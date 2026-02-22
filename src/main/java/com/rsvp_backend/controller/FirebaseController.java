package com.rsvp_backend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.rsvp_backend.dto.DeleteBatchRequestDto;
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
        int additionalCount = rsvpRequest.getAdditionalCount(); // validated 0..10

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
        doc.put("additionalCount", additionalCount);
        doc.put("totalAttendees", 1 + additionalCount); // handy for admin view
        doc.put("createdAt", FieldValue.serverTimestamp());

        firestore.collection("rsvps").add(doc).get();

        // ✅ 3) Send confirmation email (optional)
        try {
            emailService.sendRsvpConfirmation(email, name, additionalCount);
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
    @DeleteMapping("/admin/rsvps")
    public ResponseEntity<?> deleteRsvpsBatch(@Valid @RequestBody DeleteBatchRequestDto req) throws Exception {
        List<String> ids = req.getIds();

        // sanitize
        List<String> clean = new ArrayList<>();
        for (String id : ids) {
            if (id != null && !id.isBlank()) clean.add(id.trim());
        }

        if (clean.isEmpty()) {
            return ResponseEntity.badRequest().body("ids must not be empty");
        }

        // Firestore batch limit = 500 operations
        int deleted = 0;
        int idx = 0;

        while (idx < clean.size()) {
            int end = Math.min(idx + 500, clean.size());
            List<String> chunk = clean.subList(idx, end);

            WriteBatch batch = firestore.batch();
            for (String id : chunk) {
                var ref = firestore.collection("rsvps").document(id);
                batch.delete(ref); // hard delete
            }

            batch.commit().get();
            deleted += chunk.size();
            idx = end;
        }

        return ResponseEntity.ok("Deleted " + deleted + " RSVP(s)");
    }

    @DeleteMapping("/admin/rsvps/{id}")
    public ResponseEntity<?> deleteRsvp(@PathVariable String id) throws Exception {
        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest().body("Missing document id");
        }

        var ref = firestore.collection("rsvps").document(id);
        var snap = ref.get().get();

        if (!snap.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RSVP not found");
        }

        ref.delete().get(); // ✅ hard delete
        return ResponseEntity.ok("Deleted");
    }
}