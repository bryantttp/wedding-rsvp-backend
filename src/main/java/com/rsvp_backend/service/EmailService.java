package com.rsvp_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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

        // CID name you reference in <img src="cid:...">
        String imageCid = "heroImage";

        String html = """
        <div style="font-family: Arial, sans-serif; background:#f6f6f6; padding:24px;">
            <div style="max-width:640px; margin:0 auto; background:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 8px 24px rgba(0,0,0,.08);">
            
            <img src="cid:%s" alt="Bryant & Cindy" style="width:100%%; display:block;">

            <div style="padding:24px;">
                <h1 style="margin:0 0 8px; font-size:24px; color:#5A3E2B;">RSVP Confirmed 💍</h1>
                <p style="margin:0 0 16px; color:#5A3E2B;">Hi %s, thank you! We’ve received your RSVP.</p>

                <div style="padding:16px; background:#FFF8E7; border-radius:12px;">
                <h2 style="margin:0 0 10px; font-size:16px; letter-spacing:.12em; color:#5A3E2B;">RSVP SUMMARY</h2>
                <p style="margin:0; color:#5A3E2B;">Total guests: <b>%d</b></p>
                </div>

                <div style="height:16px;"></div>

                <div style="padding:16px; border:1px solid #F6C453; border-radius:12px;">
                <h2 style="margin:0 0 10px; font-size:16px; letter-spacing:.12em; color:#5A3E2B;">WEDDING DETAILS</h2>
                <p style="margin:0 0 6px; color:#5A3E2B;"><b>Date:</b> 13 June 2026</p>
                <p style="margin:0 0 6px; color:#5A3E2B;"><b>Venue:</b> Orchard Hotel Singapore</p>
                <p style="margin:0 0 6px; color:#5A3E2B;"><b>Address:</b> 442 Orchard Road, Singapore 238879</p>
                <p style="margin:0; color:#5A3E2B;"><b>Reception:</b> 6:30 PM</p>
                </div>

                <p style="margin:18px 0 0; color:#5A3E2B;">See you soon ❤️<br>— Bryant & Cindy</p>
            </div>
            </div>
        </div>
        """.formatted(imageCid, name, totalGuests);

        helper.setText(html, true);

        // Put your image in: src/main/resources/static/email/hero.jpg (or resources/email/hero.jpg)
        var img = new ClassPathResource("email/hero.jpg");
        helper.addInline(imageCid, img, "image/jpeg");

        mailSender.send(mimeMessage);
    }
}