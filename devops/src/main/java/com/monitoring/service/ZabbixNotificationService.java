package com.monitoring.service;

import com.monitoring.config.NotificationConfig;
import com.monitoring.model.AlertEvent;
import com.monitoring.model.MonitoringResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Zabbix bildirim servisi.
 * Zabbix Sender protokolü kullanarak metrik verilerini Zabbix'e gönderir.
 */
@Service
@ConditionalOnProperty(value = "notifications.zabbix.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class ZabbixNotificationService implements NotificationService {

    private final NotificationConfig notificationConfig;

    @Override
    public void sendAlert(AlertEvent alertEvent) {
        try {
            log.debug("Sending Zabbix alert: {}", alertEvent);

            // Zabbix yapılandırmasını al
            NotificationConfig.ZabbixConfig zabbixConfig = notificationConfig.getZabbix();

            // Monitoring sonucu
            MonitoringResult result = alertEvent.getMonitoringResult();

            // Metrics oluştur ve gönder
            String host = zabbixConfig.getHost();
            String resourceId = result.getResourceId();

            // Metrikleri belirle
            switch (result.getType()) {
                case HOST:
                    String itemKey = zabbixConfig.getItems().get(resourceId.toLowerCase());
                    if (itemKey != null) {
                        sendMetric(host, itemKey, getNumericValue(result));
                    }
                    break;
                case HTTP:
                    sendMetric(host, "http.response." + resourceId, result.getResponseTime());
                    sendMetric(host, "http.status." + resourceId, result.isSuccess() ? 1 : 0);
                    break;
                case WEBSOCKET:
                    sendMetric(host, "websocket.response." + resourceId, result.getResponseTime());
                    sendMetric(host, "websocket.status." + resourceId, result.isSuccess() ? 1 : 0);
                    break;
                case DATABASE:
                    sendMetric(host, "db.response." + resourceId, result.getResponseTime());
                    sendMetric(host, "db.status." + resourceId, result.isSuccess() ? 1 : 0);
                    break;
            }

            // Alert metriği gönder
            int severity;
            switch (alertEvent.getLevel()) {
                case CRITICAL:
                    severity = 5;
                    break;
                case WARNING:
                    severity = 3;
                    break;
                default:
                    severity = 1;
                    break;
            }

            sendMetric(host, "alert.severity." + result.getResourceId(), severity);
            sendMetric(host, "alert.message." + result.getResourceId(), alertEvent.getMessage());

        } catch (Exception e) {
            log.error("Error sending Zabbix alert", e);
        }
    }

    /**
     * Zabbix'e metrik değeri gönderir.
     *
     * @param host Host adı
     * @param key Metrik anahtarı
     * @param value Metrik değeri
     */
    private void sendMetric(String host, String key, Object value) {
        try {
            NotificationConfig.ZabbixConfig zabbixConfig = notificationConfig.getZabbix();
            String server = zabbixConfig.getServer();
            int port = zabbixConfig.getPort();

            // Zabbix Sender protokolü: https://www.zabbix.com/documentation/current/manual/appendix/protocols/zabbix_sender
            long timestamp = System.currentTimeMillis() / 1000;

            // JSON formatında veri hazırla
            // {"request":"sender data","data":[{"host":"Host","key":"trap","value":"test value","clock":1234567890}]}
            String json = String.format(
                    "{\"request\":\"sender data\",\"data\":[{\"host\":\"%s\",\"key\":\"%s\",\"value\":\"%s\",\"clock\":%d}]}",
                    host, key, value, timestamp
            );

            // Zabbix header hazırla
            byte[] header = new byte[]{
                    'Z', 'B', 'X', 'D', '\1',
                    (byte) (json.length() & 0xFF),
                    (byte) ((json.length() >> 8) & 0xFF),
                    (byte) ((json.length() >> 16) & 0xFF),
                    (byte) ((json.length() >> 24) & 0xFF),
                    '\0', '\0', '\0', '\0'
            };

            // Zabbix sunucusuna bağlan
            try (Socket socket = new Socket(server, port)) {
                OutputStream out = socket.getOutputStream();

                // Header ve JSON verilerini gönder
                out.write(header);
                out.write(json.getBytes(StandardCharsets.UTF_8));
                out.flush();

                // Yanıtı okuma ve işleme (basitleştirilmiş)
                byte[] buffer = new byte[1024];
                int bytesRead = socket.getInputStream().read(buffer);

                if (bytesRead > 0) {
                    log.debug("Zabbix response received");
                }
            }

            log.debug("Zabbix metric sent: host={}, key={}, value={}", host, key, value);

        } catch (IOException e) {
            log.error("Error sending metric to Zabbix: host={}, key={}", host, key, e);
        }
    }

    /**
     * Monitoring sonucundan sayısal değer çıkarır.
     *
     * @param result Monitoring sonucu
     * @return Sayısal değer
     */
    private Object getNumericValue(MonitoringResult result) {
        // Resource ID'ye göre değeri belirle
        String resourceId = result.getResourceId().toLowerCase();

        // Data alanından değeri çıkar (özel formatta saklanmış olabilir)
        String data = result.getData();

        if (data != null) {
            // CPU, RAM, Disk kullanımı
            if (resourceId.equals("cpu") || resourceId.equals("memory") || resourceId.equals("ram") || resourceId.equals("disk")) {
                // "CPU usage: 85.23%" formatından değeri çıkar
                int startIndex = data.indexOf(":") + 1;
                int endIndex = data.indexOf("%");

                if (startIndex > 0 && endIndex > startIndex) {
                    String value = data.substring(startIndex, endIndex).trim();
                    try {
                        return Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        log.warn("Could not parse numeric value from: {}", data);
                    }
                }
            }
        }

        // Varsayılan: Yanıt süresi veya 0/1 durum değeri
        return result.isSuccess() ? result.getResponseTime() : 0;
    }

    @Override
    public boolean isEnabled() {
        return notificationConfig.getZabbix() != null && notificationConfig.getZabbix().isEnabled();
    }
}