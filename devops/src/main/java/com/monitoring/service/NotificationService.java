package com.monitoring.service;

import com.monitoring.model.AlertEvent;

/**
 * Bildirim gönderme arayüzü.
 */
public interface NotificationService {

    /**
     * Uyarı bildirimi gönderir.
     *
     * @param alertEvent Uyarı olayı
     */
    void sendAlert(AlertEvent alertEvent);

    /**
     * Servisin etkin olup olmadığını kontrol eder.
     *
     * @return Servis etkin mi?
     */
    boolean isEnabled();
}