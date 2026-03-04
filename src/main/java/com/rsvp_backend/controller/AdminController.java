package com.rsvp_backend.controller;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import com.rsvp_backend.dto.DeleteBatchRequestDto;
import com.rsvp_backend.service.EmailService;
import com.rsvp_backend.service.EmailService.Recipient;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final Firestore firestore;

    @Autowired
    private EmailService emailService;

    public AdminController(Firestore firestore) {
        this.firestore = firestore;
    }
    @GetMapping("/rsvps")
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
    @DeleteMapping("/rsvps")
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

    @DeleteMapping("/rsvps/{id}")
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

    public record AdminBulkEmailRequest(
            List<Recipient> recipients,
            String subjectTemplate,
            String htmlTemplate,
            List<String> extraBcc
    ) {}

    @PostMapping("/send-email")
public ResponseEntity<String> sendEmail(@RequestBody AdminBulkEmailRequest req) {
    if (req == null) return ResponseEntity.badRequest().body("Missing request body");

    if (req.recipients() == null || req.recipients().isEmpty()) {
        return ResponseEntity.badRequest().body("No recipients selected");
    }
    if (req.subjectTemplate() == null || req.subjectTemplate().isBlank()) {
        return ResponseEntity.badRequest().body("Subject template is required");
    }
    if (req.htmlTemplate() == null || req.htmlTemplate().isBlank()) {
        return ResponseEntity.badRequest().body("HTML template is required");
    }

    try {
        emailService.sendAdminBulkEmail(
                req.recipients(),
                req.subjectTemplate(),
                req.htmlTemplate(),
                req.extraBcc()
        );
        return ResponseEntity.ok("Sent " + req.recipients().size() + " email(s)");
    } catch (MessagingException e) {
        return ResponseEntity.status(500).body("Failed to send: " + e.getMessage());
    }
}
}
