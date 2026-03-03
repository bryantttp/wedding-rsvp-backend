package com.rsvp_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String from;

    @Value("${spring.mail.password}")
    private String mailPassword; // 👈 DEBUG ONLY (remove later)

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

    // ✅ NEW signature: additionalCount instead of List<String>
    public void sendRsvpConfirmation(String to, String name, int totalGuests) throws MessagingException{
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        // true = multipart (needed for inline images)
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject("Here are the event details from Cindy & Bryant's Wedding");

        String html = """
        <div style="font-family: Arial, sans-serif; background:#f6f6f6; padding:40px 0; text-align:center;">
            <div style="max-width:640px; margin:0 auto; background:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 8px 24px rgba(0,0,0,.08);">

                <div style="padding:24px;background:#FFF8E7;">
                    <h1 style="margin:0 0 8px; font-size:24px; color:#5A3E2B;">Cindy & Bryant's Wedding💍</h1>
    
                    <div style="height:16px;"></div>
    
                    <div style="padding:16px; ">
                        <p style="margin:0 0 6px; color:#5A3E2B;"> Saturday, 13 June, 2026</p>
                        <p style="margin:0 0 6px; color:#5A3E2B;"> 442 Orchard Road, Singapore 238879</p>
                        <p style="margin:0; color:#5A3E2B;"> 6:30 PM</p>
                    </div>
                    <div style="height:16px;"></div>
                </div>
                <img src="https://bryant-and-cindy.vercel.app/hero.jpg" alt="Cindy & Bryant's Wedding" style="width:100%; display:block;">
                <div style="padding:24px;background:#FFF8E7;">
                <p style="margin:18px 0 0; color:#5A3E2B;">Hi %s, thank you for your response. See you soon ❤️</p>
                <div style="height:16px;"></div>
                </div>
            </div>
        </div>
        """.formatted(name);

        helper.setText(html, true);

        mailSender.send(mimeMessage);
    }
}