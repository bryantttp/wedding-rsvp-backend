package com.rsvp_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendRsvpConfirmation(String to, String name, int groupNumber) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject("RSVP received 💍");
            msg.setText(String.format(
                    "Hi %s,\n\n" +
                    "Thank you! We’ve received your RSVP.\n\n" +
                    "Group number: %d\n\n" +
                    "We can’t wait to celebrate with you ❤️\n\n" +
                    "— Bryant & Cindy",
                    name, groupNumber
            ));

            mailSender.send(msg);
            System.out.println("✅ Email sent to " + to);

        } catch (Exception e) {
            System.err.println("❌ Email failed: " + e.getMessage());
        }
    }
}
