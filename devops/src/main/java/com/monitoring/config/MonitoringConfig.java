package com.monitoring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Ana izleme yapılandırması
 */
@Configuration
@ConfigurationProperties(prefix = "monitoring")
@RefreshScope
@Data
public class MonitoringConfig {

    /**
     * Host izleme yapılandırması
     */
    private HostConfig host;

    /**
     * Host izleme yapılandırma sınıfı
     */
    @Data
    public static class HostConfig {
        /**
         * Host izleme etkin mi?
         */
        private boolean enabled = true;

        /**
         * İzleme aralığı (milisaniye)
         */
        private long interval = 60000;

        /**
         * Eşik değerleri
         */
        private Map<String, Integer> thresholds;
    }
}