package com.monitoring.service;

import com.monitoring.config.NotificationConfig;
import com.monitoring.model.AlertEvent;
import com.monitoring.model.MonitoringResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Prometheus metrics servisi.
 * Monitoring sonuçlarını Prometheus metriklerine dönüştürür.
 */
@Service
@ConditionalOnProperty(value = "notifications.prometheus.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class PrometheusMetricsService implements NotificationService {

    private final NotificationConfig notificationConfig;
    private final MeterRegistry meterRegistry;

    // Son metrik değerlerini saklamak için cache
    private final Map<String, Double> lastMetricValues = new ConcurrentHashMap<>();

    /**
     * Servis başlatıldığında temel Prometheus metriklerini oluşturur.
     */
    @PostConstruct
    public void init() {
        log.info("Initializing Prometheus metrics service...");

        // Temel sistem metrikleri için gauge'lar oluştur
        createSystemGauges();
    }

    /**
     * Temel sistem metrikleri için Prometheus gauge'larını oluşturur.
     */
    private void createSystemGauges() {
        // CPU kullanımı
        Gauge.builder("monitoring.host.cpu.usage", lastMetricValues, values -> values.getOrDefault("cpu.usage", 0.0))
                .description("Host CPU usage percentage")
                .tags(createCommonTags())
                .register(meterRegistry);

        // RAM kullanımı
        Gauge.builder("monitoring.host.memory.usage", lastMetricValues, values -> values.getOrDefault("memory.usage", 0.0))
                .description("Host memory usage percentage")
                .tags(createCommonTags())
                .register(meterRegistry);

        // Disk kullanımı
        Gauge.builder("monitoring.host.disk.usage", lastMetricValues, values -> values.getOrDefault("disk.usage", 0.0))
                .description("Host disk usage percentage")
                .tags(createCommonTags())
                .register(meterRegistry);
    }

    @Override
    public void sendAlert(AlertEvent alertEvent) {
        try {
            log.debug("Recording Prometheus metrics for alert: {}", alertEvent);

            MonitoringResult result = alertEvent.getMonitoringResult();

            // Metrik kayıtları oluştur
            recordMetrics(result);

            // Uyarı sayacını güncelle
            incrementAlertCounter(alertEvent);

        } catch (Exception e) {
            log.error("Error recording Prometheus metrics", e);
        }
    }

    /**
     * Monitoring sonuçlarına göre Prometheus metriklerini kaydeder.
     *
     * @param result Monitoring sonucu
     */
    private void recordMetrics(MonitoringResult result) {
        String resourceId = result.getResourceId();

        // Metrik tipini belirle
        switch (result.getType()) {
            case HOST:
                recordHostMetrics(resourceId, result);
                break;
            case HTTP:
                recordHttpMetrics(resourceId, result);
                break;
            case WEBSOCKET:
                recordWebSocketMetrics(resourceId, result);
                break;
            case DATABASE:
                recordDatabaseMetrics(resourceId, result);
                break;
        }
    }

    /**
     * Host metriklerini kaydeder.
     *
     * @param resourceId Kaynak ID
     * @param result Monitoring sonucu
     */
    private void recordHostMetrics(String resourceId, MonitoringResult result) {
        if (result.getData() == null) {
            return;
        }

        // Veri alanından değerleri çıkar
        String data = result.getData();

        if (resourceId.equalsIgnoreCase("cpu")) {
            // "CPU usage: 85.23%" formatından değeri çıkar
            extractAndStorePercentage(data, "cpu.usage");
        } else if (resourceId.equalsIgnoreCase("memory") || resourceId.equalsIgnoreCase("ram")) {
            // "Memory usage: 75.87%" formatından değeri çıkar
            extractAndStorePercentage(data, "memory.usage");
        } else if (resourceId.equalsIgnoreCase("disk")) {
            // "Disk usage: 62.41%" formatından değeri çıkar
            extractAndStorePercentage(data, "disk.usage");
        }
    }

    /**
     * Veri metninden yüzde değerini çıkarır ve saklar.
     *
     * @param data Veri metni
     * @param metricKey Metrik anahtarı
     */
    private void extractAndStorePercentage(String data, String metricKey) {
        int startIndex = data.indexOf(":") + 1;
        int endIndex = data.indexOf("%");

        if (startIndex > 0 && endIndex > startIndex) {
            String value = data.substring(startIndex, endIndex).trim();
            try {
                double percentage = Double.parseDouble(value);
                lastMetricValues.put(metricKey, percentage);
            } catch (NumberFormatException e) {
                log.warn("Could not parse percentage value from: {}", data);
            }
        }
    }

    /**
     * HTTP endpoint metriklerini kaydeder.
     *
     * @param resourceId Kaynak ID
     * @param result Monitoring sonucu
     */
    private void recordHttpMetrics(String resourceId, MonitoringResult result) {
        List<Tag> tags = createEndpointTags(resourceId, result);

        // Yanıt süresi
        Timer timer = Timer.builder("monitoring.http.response.time")
                .description("HTTP endpoint response time")
                .tags(tags)
                .register(meterRegistry);

        timer.record(result.getResponseTime(), TimeUnit.MILLISECONDS);

        // Başarı durumu
        Gauge.builder("monitoring.http.status", result, r -> r.isSuccess() ? 1.0 : 0.0)
                .description("HTTP endpoint status (1 = UP, 0 = DOWN)")
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * WebSocket endpoint metriklerini kaydeder.
     *
     * @param resourceId Kaynak ID
     * @param result Monitoring sonucu
     */
    private void recordWebSocketMetrics(String resourceId, MonitoringResult result) {
        List<Tag> tags = createEndpointTags(resourceId, result);

        // Yanıt süresi
        Timer timer = Timer.builder("monitoring.websocket.response.time")
                .description("WebSocket endpoint response time")
                .tags(tags)
                .register(meterRegistry);

        timer.record(result.getResponseTime(), TimeUnit.MILLISECONDS);

        // Başarı durumu
        Gauge.builder("monitoring.websocket.status", result, r -> r.isSuccess() ? 1.0 : 0.0)
                .description("WebSocket endpoint status (1 = UP, 0 = DOWN)")
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * Veritabanı endpoint metriklerini kaydeder.
     *
     * @param resourceId Kaynak ID
     * @param result Monitoring sonucu
     */
    private void recordDatabaseMetrics(String resourceId, MonitoringResult result) {
        List<Tag> tags = createEndpointTags(resourceId, result);

        // Yanıt süresi
        Timer timer = Timer.builder("monitoring.database.response.time")
                .description("Database endpoint response time")
                .tags(tags)
                .register(meterRegistry);

        timer.record(result.getResponseTime(), TimeUnit.MILLISECONDS);

        // Başarı durumu
        Gauge.builder("monitoring.database.status", result, r -> r.isSuccess() ? 1.0 : 0.0)
                .description("Database endpoint status (1 = UP, 0 = DOWN)")
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * Uyarı sayacını artırır.
     *
     * @param alertEvent Uyarı olayı
     */
    private void incrementAlertCounter(AlertEvent alertEvent) {
        String levelName = alertEvent.getLevel().name().toLowerCase();
        MonitoringResult result = alertEvent.getMonitoringResult();
        String typeName = result.getType().name().toLowerCase();

        List<Tag> tags = Arrays.asList(
                Tag.of("level", levelName),
                Tag.of("type", typeName),
                Tag.of("resource", result.getResourceId())
        );

        Counter counter = Counter.builder("monitoring.alerts.total")
                .description("Total number of monitoring alerts")
                .tags(tags)
                .register(meterRegistry);

        counter.increment();
    }

    /**
     * Genel etiketleri oluşturur.
     *
     * @return Etiket listesi
     */
    private List<Tag> createCommonTags() {
        Map<String, String> labels = new HashMap<>();

        // Yapılandırmadan etiketleri al
        if (notificationConfig.getPrometheus() != null &&
                notificationConfig.getPrometheus().getLabels() != null) {
            labels.putAll(notificationConfig.getPrometheus().getLabels());
        }

        // Etiketleri Tag listesine dönüştür
        return labels.entrySet().stream()
                .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * Endpoint için etiketleri oluşturur.
     *
     * @param resourceId Kaynak ID
     * @param result Monitoring sonucu
     * @return Etiket listesi
     */
    private List<Tag> createEndpointTags(String resourceId, MonitoringResult result) {
        List<Tag> tags = Arrays.asList(
                Tag.of("endpoint", resourceId),
                Tag.of("status", result.getStatus().name().toLowerCase())
        );

        return tags;
    }

    @Override
    public boolean isEnabled() {
        return notificationConfig.getPrometheus() != null && notificationConfig.getPrometheus().isEnabled();
    }
}