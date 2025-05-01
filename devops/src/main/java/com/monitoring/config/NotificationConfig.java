package com.monitoring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Bildirim yapılandırması
 */
@Configuration
@ConfigurationProperties(prefix = "notifications")
@RefreshScope
@Data
public class NotificationConfig {

    /**
     * E-posta bildirim yapılandırması
     */
    private EmailConfig email;

    /**
     * Zabbix bildirim yapılandırması
     */
    private ZabbixConfig zabbix;

    /**
     * Prometheus bildirim yapılandırması
     */
    private PrometheusConfig prometheus;

    /**
     * E-posta bildirim yapılandırma sınıfı
     */
    @Data
    public static class EmailConfig {
        /**
         * E-posta bildirimi etkin mi?
         */
        private boolean enabled = true;

        /**
         * Alıcılar (virgülle ayrılmış)
         */
        private String recipients;

        /**
         * Gönderen e-posta adresi
         */
        private String sender;

        /**
         * E-posta şablonları
         */
        private Map<String, String> templates;
    }

    /**
     * Zabbix bildirim yapılandırma sınıfı
     */
    @Data
    public static class ZabbixConfig {
        /**
         * Zabbix bildirimi etkin mi?
         */
        private boolean enabled = true;

        /**
         * Zabbix sunucu adresi
         */
        private String server;

        /**
         * Zabbix sunucu portu
         */
        private int port = 10051;

        /**
         * Host adı
         */
        private String host;

        /**
         * İzleme öğeleri
         */
        private Map<String, String> items;
    }

    /**
     * Prometheus bildirim yapılandırma sınıfı
     */
    @Data
    public static class PrometheusConfig {
        /**
         * Prometheus bildirimi etkin mi?
         */
        private boolean enabled = true;

        /**
         * Endpoint
         */
        private String endpoint = "/metrics";

        /**
         * Port
         */
        private int port = 9090;

        /**
         * Etiketler
         */
        private Map<String, String> labels;
    }
}