package com.monitoring.endpoint;

import com.monitoring.model.MonitoringResult;

/**
 * Endpoint izleme arayüzü.
 * Tüm spesifik endpoint izleyicilerinin uygulaması gereken arayüz.
 */
public interface EndpointMonitor<T> {

    /**
     * Belirtilen endpoint'i izler.
     *
     * @param endpoint İzlenecek endpoint yapılandırması
     * @return İzleme sonucu
     */
    MonitoringResult monitor(T endpoint);

    /**
     * İzleme görevini başlatır.
     */
    void startMonitoring();

    /**
     * İzleme görevini durdurur.
     */
    void stopMonitoring();

    /**
     * İzleme görevinin durumunu kontrol eder.
     *
     * @return İzleme görevi çalışıyor mu?
     */
    boolean isRunning();
}