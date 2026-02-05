package com.rsvp_backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    private static final String BREVO_SEND_EMAIL_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.apiKey}")
    private String brevoApiKey;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.fromName}")
    private String fromName;

    public void sendRsvpConfirmation(String to, String name, int groupNumber) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);

        Map<String, Object> sender = new HashMap<>();
        sender.put("name", fromName);
        sender.put("email", fromEmail);

        Map<String, Object> toEntry = new HashMap<>();
        toEntry.put("email", to);
        toEntry.put("name", name);

        String subject = "RSVP received 💍";

        // You can switch to htmlContent if you want nicer formatting
        String textContent = String.format(
            "Hi %s,\n\n" +
            "Thank you! We’ve received your RSVP.\n\n" +
            "Group number: %d\n\n" +
            "We can’t wait to celebrate with you ❤️\n\n" +
            "— Bryant & Cindy",
            name, groupNumber
        );

        Map<String, Object> body = new HashMap<>();
        body.put("sender", sender);
        body.put("to", List.of(toEntry));
        body.put("subject", subject);
        body.put("textContent", textContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // If Brevo rejects it, Spring will throw an exception here
        restTemplate.postForEntity(BREVO_SEND_EMAIL_URL, request, String.class);
    }
}
