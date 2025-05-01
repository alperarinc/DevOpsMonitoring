package com.monitoring.service;

import com.monitoring.config.NotificationConfig;
import com.monitoring.model.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.*;

/**
 * E-posta bildirim servisi.
 */
@Service
@ConditionalOnProperty(value = "notifications.email.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

    private final NotificationConfig notificationConfig;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendAlert(AlertEvent alertEvent) {
        try {
            log.debug("Sending email alert: {}", alertEvent);

            // E-posta yapılandırmasını al
            NotificationConfig.EmailConfig emailConfig = notificationConfig.getEmail();
            if (emailConfig == null) {
                log.error("Email configuration is null, cannot send emails");
                return;
            }

            // Alıcıları hazırla
            Set<String> recipients = new HashSet<>(); // HashSet kullanarak duplikasyonu önle

            // Grup alıcılarını details kısmından ekle (eğer varsa)
            if (alertEvent.getDetails() != null && !alertEvent.getDetails().isEmpty()) {
                log.debug("Found details for alert: {}", alertEvent.getDetails());
                String[] groupEmails = alertEvent.getDetails().split(",");
                for (String email : groupEmails) {
                    if (email != null && !email.trim().isEmpty()) {
                        log.debug("Adding group email from details: {}", email.trim());
                        recipients.add(email.trim());
                    }
                }
            } else {
                log.debug("No details (group emails) found for alert");
            }

            // Varsayılan yapılandırma alıcılarını ekle
            if (emailConfig.getRecipients() != null && !emailConfig.getRecipients().isEmpty()) {
                log.debug("Adding configured recipients: {}", emailConfig.getRecipients());
                String[] configEmails = emailConfig.getRecipients().split(",");
                for (String email : configEmails) {
                    if (email != null && !email.trim().isEmpty()) {
                        log.debug("Adding configured email: {}", email.trim());
                        recipients.add(email.trim());
                    }
                }
            } else {
                log.debug("No configured recipients found");
            }

            if (recipients.isEmpty()) {
                log.warn("No recipients found for alert: {}", alertEvent.getId());
                return;
            }

            // Log all recipients for debugging
            log.debug("Final recipient list: {}", recipients);

            // Şablonu belirle
            String templateName;
            switch (alertEvent.getLevel()) {
                case CRITICAL:
                    templateName = emailConfig.getTemplates().getOrDefault("critical", "critical-email");
                    break;
                case WARNING:
                    templateName = emailConfig.getTemplates().getOrDefault("warning", "warning-email");
                    break;
                default:
                    templateName = emailConfig.getTemplates().getOrDefault("info", "info-email");
                    break;
            }

            // Şablon değişkenlerini hazırla
            Map<String, Object> variables = new HashMap<>();
            variables.put("alert", alertEvent);
            variables.put("result", alertEvent.getMonitoringResult());
            variables.put("timestamp", alertEvent.getTimestamp());
            variables.put("level", alertEvent.getLevel().name());
            variables.put("message", alertEvent.getMessage());

            // Thymeleaf template'i işle
            Context context = new Context();
            context.setVariables(variables);
            String emailContent = templateEngine.process(templateName, context);

            // E-posta konusunu hazırla
            String subject = "[" + alertEvent.getLevel() + "] Monitoring Alert";
            if (alertEvent.getMonitoringResult() != null && alertEvent.getMonitoringResult().getResourceId() != null) {
                subject += ": " + alertEvent.getMonitoringResult().getResourceId();
            }

            // Her bir alıcıya ayrı e-posta gönder (gizlilik için)
            for (String recipient : recipients) {
                try {
                    sendEmailToRecipient(emailConfig, subject, emailContent, recipient);
                } catch (Exception e) {
                    log.error("Error sending email to recipient {}: {}", recipient, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error sending email alert: {}", e.getMessage(), e);
        }
    }

    /**
     * Belirli bir alıcıya e-posta gönderir.
     */
    private void sendEmailToRecipient(NotificationConfig.EmailConfig emailConfig,
                                      String subject,
                                      String content,
                                      String recipient) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(emailConfig.getSender());
        helper.setSubject(subject);
        helper.setText(content, true);
        helper.setTo(recipient);

        mailSender.send(mimeMessage);
        log.debug("Email alert sent to: {}", recipient);
    }

    @Override
    public boolean isEnabled() {
        return notificationConfig.getEmail() != null && notificationConfig.getEmail().isEnabled();
    }
}