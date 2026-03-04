package com.rsvp_backend.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // ======================================
    // 2) ADMIN BULK SEND (subject + html + bcc)
    // ======================================
    /**
     * Send a personalised email to EACH recipient:
     * - subjectTemplate can contain {{name}} etc.
     * - htmlTemplate can contain {{name}}, {{totalGuests}}, {{email}} etc.
     * - extraBcc always BCCs these addresses on every email (optional)
     */
    public void sendAdminBulkEmail(
            List<Recipient> recipients,
            String subjectTemplate,
            String htmlTemplate,
            List<String> extraBcc
    ) throws MessagingException {

        List<String> safeBcc = (extraBcc == null) ? Collections.emptyList() : extraBcc;

        for (Recipient r : recipients) {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(r.email());
            if (!safeBcc.isEmpty()) {
                helper.setBcc(safeBcc.toArray(new String[0]));
            }

            int safeTotalGuests = clamp(r.totalGuests(), 1, 10);

            Map<String, String> vars = Map.of(
                    "name", r.name() == null ? "" : r.name(),
                    "email", r.email() == null ? "" : r.email(),
                    "totalGuests", String.valueOf(safeTotalGuests)
            );

            String subject = renderTemplate(subjectTemplate, vars);
            String html = renderTemplate(htmlTemplate, vars);

            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(mimeMessage);
        }
    }

     private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    // Simple HTML escape for user input (name/email).
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Safe template render for {{placeholders}}.
     * Unknown placeholders are left as-is (so your logic never "breaks").
     */
    private static String renderTemplate(String template, Map<String, String> vars) {
        if (template == null) return "";
        Pattern p = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*}}");
        Matcher m = p.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            String value = vars.get(key);
            if (value == null) {
                // leave unknown placeholders untouched
                m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
            } else {
                m.appendReplacement(sb, Matcher.quoteReplacement(escapeHtml(value)));
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public record Recipient(String name, String email, int totalGuests) {}
}