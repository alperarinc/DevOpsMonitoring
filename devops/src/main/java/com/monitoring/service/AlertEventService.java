package com.monitoring.service;

import com.monitoring.enums.AlertLevel;
import com.monitoring.model.AlertEvent;
import com.monitoring.repository.AlertEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AlertEventService {

    private final AlertEventRepository alertEventRepository;

    @Autowired
    public AlertEventService(AlertEventRepository alertEventRepository) {
        this.alertEventRepository = alertEventRepository;
    }

    /**
     * Tüm uyarı olaylarını sayfalayarak getirir
     */
    public Page<AlertEvent> getAllAlerts(Pageable pageable) {
        return alertEventRepository.findAll(pageable);
    }

    /**
     * Belirli bir ID'ye sahip uyarı olayını getirir
     */
    public Optional<AlertEvent> getAlertById(Long id) {
        return alertEventRepository.findById(id);
    }

    /**
     * Çözülme durumuna göre uyarıları filtreler
     */
    public Page<AlertEvent> getAlertsByResolvedStatus(boolean resolved, Pageable pageable) {
        return alertEventRepository.findByResolved(resolved, pageable);
    }

    /**
     * Uyarı seviyesine göre uyarıları filtreler
     */
    public Page<AlertEvent> getAlertsByLevel(AlertLevel level, Pageable pageable) {
        return alertEventRepository.findByLevel(level, pageable);
    }

    /**
     * Hem seviyeye hem çözülme durumuna göre uyarıları filtreler
     */
    public Page<AlertEvent> getAlertsByLevelAndResolvedStatus(AlertLevel level, boolean resolved, Pageable pageable) {
        return alertEventRepository.findByLevelAndResolved(level, resolved, pageable);
    }

    /**
     * Belirli bir zaman aralığındaki uyarıları getirir
     */
    public Page<AlertEvent> getAlertsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return alertEventRepository.findByTimestampBetween(startTime, endTime, pageable);
    }

    /**
     * Belirli bir endpoint konfigürasyonuna ait uyarıları getirir
     */
    public Page<AlertEvent> getAlertsByEndpointId(Long endpointId, Pageable pageable) {
        return alertEventRepository.findByEndpointConfig_Id(endpointId, pageable);
    }

    /**
     * Uyarıyı çözüldü olarak işaretler
     */
    public AlertEvent markAsResolved(Long id) {
        AlertEvent alertEvent = alertEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + id));

        alertEvent.setResolved(true);
        alertEvent.setResolvedAt(LocalDateTime.now());

        return alertEventRepository.save(alertEvent);
    }
}