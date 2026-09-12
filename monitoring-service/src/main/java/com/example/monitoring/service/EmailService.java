package com.example.monitoring.service;

import com.example.monitoring.dto.CreditBanEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("{monitoring.email.to}")
    private String recipientEmail;

    public void sendCreditBanNotification(CreditBanEvent event) {
        String emailBody = buildEmailBody(event);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject("Credit Ban Detected - " + event.getSsn());
            message.setText(emailBody);

            javaMailSender.send(message);
            log.info("Notification email sent to: {}", recipientEmail);
        } catch (MailAuthenticationException e) {
            log.error("Mail authentication failed. Check SMTP credentials.", e);
            logFallback(emailBody, event);
        } catch (MailException e) {
            log.error("Failed to send email for SSN: {}", event.getSsn(), e);
            logFallback(emailBody, event);
        }
    }

    private String buildEmailBody(CreditBanEvent event) {
        return String.format("""
            New voluntary credit ban detected in PCR.
            
            SSN: %s
            Reference: %s
            Reason: %s
            Fetch Date: %s
            Event Time: %s
            
            This is an automated notification from the monitoring service.
            """,
            event.getSsn(),
            event.getExtractReference(),
            event.getReason(),
            event.getFetchDate(),
            event.getEventTime()
        );
    }

    private void logFallback(String body, CreditBanEvent event) {
        log.warn("=== EMAIL FALLBACK (send failed, logging content) ===");
        log.warn("To: {}", recipientEmail);
        log.warn("Subject: Credit Ban Detected - {}", event.getSsn());
        log.warn("Body:\n{}", body);
    }

}
