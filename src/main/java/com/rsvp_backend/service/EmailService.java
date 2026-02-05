package com.rsvp_backend.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class EmailService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String from;
    private final String apiKey;
    private final String domain;
    private final String baseUrl;

    public EmailService(
            @Value("${app.mail.from}") String from,
            @Value("${mailgun.apiKey}") String apiKey,
            @Value("${mailgun.domain}") String domain,
            @Value("${mailgun.baseUrl:https://api.mailgun.net}") String baseUrl
    ) {
        this.from = from;
        this.apiKey = apiKey;
        this.domain = domain;
        this.baseUrl = baseUrl;
    }

    @PostConstruct
    public void debugMailgunConfig() {
        System.out.println("MAILGUN API KEY LOADED = " + (apiKey != null && !apiKey.isBlank()));
        System.out.println("MAILGUN DOMAIN LOADED  = " + (domain != null && !domain.isBlank()));
        System.out.println("MAILGUN BASE URL       = " + baseUrl);
        System.out.println("MAIL FROM              = " + from);
    }

    public void sendRsvpConfirmation(String to, String name, int groupNumber) {
        String subject = "RSVP received 💍";
        String text = """
                Hi %s,

                Thank you! We’ve received your RSVP.

                Group number: %d

                We can’t wait to celebrate with you ❤️

                — Wedding RSVP
                """.formatted(name, groupNumber);

        sendMailgunMessage(to, subject, text);
    }

    private void sendMailgunMessage(String to, String subject, String text) {
        try {
            if (apiKey == null || apiKey.isBlank() || domain == null || domain.isBlank()) {
                throw new IllegalStateException("Mailgun config missing: mailgun.apiKey / mailgun.domain");
            }

            String endpoint = "%s/v3/%s/messages".formatted(baseUrl, domain);

            String form = formEncode(
                    "from", from,
                    "to", to,
                    "subject", subject,
                    "text", text
            );

            String basicAuth = Base64.getEncoder()
                    .encodeToString(("api:" + apiKey).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Basic " + basicAuth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new RuntimeException("Mailgun send failed. HTTP " + resp.statusCode() + ": " + resp.body());
            }

            System.out.println("✅ Mailgun email sent to " + to);

        } catch (Exception e) {
            // Don't crash your request — just log
            System.err.println("❌ Mailgun email failed: " + e.getMessage());
        }
    }

    private static String formEncode(String... kv) {
        if (kv.length % 2 != 0) throw new IllegalArgumentException("formEncode expects even number of args");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kv.length; i += 2) {
            if (sb.length() > 0) sb.append("&");
            sb.append(url(kv[i])).append("=").append(url(kv[i + 1]));
        }
        return sb.toString();
    }

    private static String url(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
