package com.rsvp_backend.service;

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

    public void sendRsvpConfirmation(String to, String name, int groupNumber) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("RSVP received 💍");
        msg.setText("""
                Hi %s,

                Thank you! We’ve received your RSVP.

                Group number: %d

                We can’t wait to celebrate with you ❤️

                — Wedding RSVP
                """.formatted(name, groupNumber));

        mailSender.send(msg);
    }
}
