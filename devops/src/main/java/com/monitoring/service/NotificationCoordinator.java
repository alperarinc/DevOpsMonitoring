package com.monitoring.service;

import com.monitoring.model.AlertEvent;
import com.monitoring.model.EndpointConfigEntity;
import com.monitoring.model.MonitoringGroup;
import com.monitoring.model.MonitoringResult;
import com.monitoring.repository.AlertEventRepository;
import com.monitoring.repository.EndpointConfigRepository;
import com.monitoring.repository.MonitoringResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bildirim koordinatörü.
 * Tüm bildirim servislerini yönetir ve uyarıları ilgili servislere yönlendirir.
 * Ayrıca AlertEvent nesnesini veritabanına kaydeder.
 */
@Slf4j
@Service
@Transactional
public class NotificationCoordinator implements NotificationService {

    @Autowired
    private MonitoringResultRepository monitoringResultRepository;

    @Autowired
    private AlertEventRepository alertEventRepository;

    @Autowired
    private EndpointConfigRepository endpointConfigRepository;

    @Autowired
    private List<NotificationService> notificationServices;

    @Override
    public void sendAlert(AlertEvent alertEvent) {
        log.info("Sending alert: {}", alertEvent);

        try {
            // 0. Öncelikle endpointConfig null ise, endpoint'i bulmaya çalış
            if (alertEvent.getEndpointConfig() == null) {
                log.debug("Alert doesn't have an endpointConfig, trying to find it");

                // MonitoringResult'tan endpointConfig'i alın
                if (alertEvent.getMonitoringResult() != null &&
                        alertEvent.getMonitoringResult().getEndpointConfig() != null) {

                    alertEvent.setEndpointConfig(alertEvent.getMonitoringResult().getEndpointConfig());
                    log.debug("Found endpoint config from monitoring result");
                }

                // ResourceId'yi kullanarak endpoint'i bul
                if (alertEvent.getEndpointConfig() == null &&
                        alertEvent.getMonitoringResult() != null &&
                        alertEvent.getMonitoringResult().getResourceId() != null) {

                    String resourceIdStr = alertEvent.getMonitoringResult().getResourceId();
                    try {
                        if (!resourceIdStr.isEmpty()) {
                            Long resourceId = Long.parseLong(resourceIdStr);
                            log.debug("Looking for endpoint with ID: {}", resourceId);

                            endpointConfigRepository.findById(resourceId).ifPresent(endpoint -> {
                                alertEvent.setEndpointConfig(endpoint);
                                log.debug("Found endpoint with ID: {}", resourceId);
                            });
                        }
                    } catch (NumberFormatException ex) {
                        log.warn("resourceId parse edilemedi: {}", resourceIdStr);
                    }
                }
            }

            // 1. Grup mail adreslerini hazırla
            Set<String> groupEmailRecipients = new HashSet<>();

            // Endpoint'e bağlı bir uyarı ise, monitoring gruplarının mail adreslerini al
            if (alertEvent.getEndpointConfig() != null) {
                EndpointConfigEntity endpoint = alertEvent.getEndpointConfig();
                log.debug("Alert is related to endpoint: {}", endpoint.getId());

                if (endpoint.getMonitoringGroups() != null) {
                    log.debug("Endpoint has {} monitoring groups", endpoint.getMonitoringGroups().size());

                    for (MonitoringGroup group : endpoint.getMonitoringGroups()) {
                        log.debug("Checking group: {} (enabled={}), email={}",
                                group.getName(), group.isEnabled(), group.getNotificationEmail());

                        if (group.isEnabled() && group.getNotificationEmail() != null && !group.getNotificationEmail().isEmpty()) {
                            log.debug("Adding group email recipient: {} for group: {}", group.getNotificationEmail(), group.getName());
                            groupEmailRecipients.add(group.getNotificationEmail());
                        }
                    }
                } else {
                    log.debug("Endpoint has no monitoring groups");
                }
            } else {
                log.debug("Alert is not related to any endpoint - even after searching");
            }

            // E-posta alıcı bilgisini alert'in details alanına ekle
            if (!groupEmailRecipients.isEmpty()) {
                String emails = String.join(",", groupEmailRecipients);
                log.debug("Setting alert details with group emails: {}", emails);
                alertEvent.setDetails(emails);
            } else {
                log.debug("No group email recipients found for this alert");
            }

            // 2. Veritabanına kaydet
            AlertEvent savedAlert;

            // MonitoringResult'ı kaydet
            if (alertEvent.getMonitoringResult() != null) {
                MonitoringResult savedResult = monitoringResultRepository.save(alertEvent.getMonitoringResult());
                alertEvent.setMonitoringResult(savedResult);
                log.debug("Saved MonitoringResult to database with ID: {}", savedResult.getId());
            }

            // AlertEvent'i kaydet
            savedAlert = alertEventRepository.save(alertEvent);
            log.debug("Saved alert to database with ID: {}", savedAlert.getId());

            // 3. Email servisi için özel kontrol
            EmailNotificationService emailService = findEmailService();
            if (emailService != null && emailService.isEnabled()) {
                try {
                    log.debug("Sending alert to EmailNotificationService with details: {}", savedAlert.getDetails());
                    emailService.sendAlert(savedAlert);
                } catch (Exception e) {
                    log.error("Error sending alert via EmailNotificationService: {}", e.getMessage(), e);
                }
            }

            // 4. Diğer bildirim servislerine gönder (email hariç)
            for (NotificationService service : notificationServices) {
                if (service != this && !(service instanceof EmailNotificationService) && service.isEnabled()) {
                    try {
                        log.debug("Sending alert to service: {}", service.getClass().getSimpleName());
                        service.sendAlert(savedAlert);
                    } catch (Exception e) {
                        log.error("Error sending alert to service: {}", service.getClass().getSimpleName(), e);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Alert kaydedilirken veya gönderilirken hata oluştu: {}", e.getMessage(), e);
            throw new RuntimeException("Alert işlemi başarısız oldu", e);
        }
    }

    /**
     * EmailNotificationService servisini bul
     */
    private EmailNotificationService findEmailService() {
        for (NotificationService service : notificationServices) {
            if (service instanceof EmailNotificationService) {
                return (EmailNotificationService) service;
            }
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        // En az bir etkin bildirim servisi varsa etkin kabul edilir
        return notificationServices.stream()
                .filter(service -> service != this)
                .anyMatch(NotificationService::isEnabled);
    }
}
