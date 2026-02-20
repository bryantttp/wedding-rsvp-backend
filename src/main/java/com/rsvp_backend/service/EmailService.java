package com.rsvp_backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String from;

    @Value("${spring.mail.password}")
    private String mailPassword;   // 👈 DEBUG ONLY

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }
        
    @PostConstruct
    public void debugMailConfig() {
        System.out.println("MAIL PASSWORD LOADED = " +
                (mailPassword != null && !mailPassword.isBlank()));
    }

    public void sendRsvpConfirmation(String to, String name, List<String> attendees) {

        // -----------------------------
        // Build attendees section
        // -----------------------------
        String attendeesSection = "";

        if (attendees != null && !attendees.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n\nAttendees Added:\n");

            for (int i = 0; i < attendees.size(); i++) {
                sb.append(i + 1)
                .append(") ")
                .append(attendees.get(i))
                .append("\n");
            }

            attendeesSection = sb.toString();
        }

        // -----------------------------
        // Full email text
        // -----------------------------
        String emailText = """
                Hi %s,

                Thank you! We’ve received your RSVP and are so excited to celebrate with you 💍

                Wedding Details
                -----------------------
                Date: 13 June 2026
                Venue: Orchard Hotel Singapore
                Address: 442 Orchard Road, Singapore 238879
                Reception starts at 6:30 PM

                Free-flow wine & beer will be served 🍷🍺
                No corkage fee as well.

                We truly appreciate you being part of our special day.
                See you soon ❤️
                %s

                — Bryant & Cindy
                """.formatted(name, attendeesSection);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("RSVP received 💍");
        msg.setText(emailText);

        mailSender.send(msg);
    }
}
