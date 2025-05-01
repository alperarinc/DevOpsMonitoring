package com.monitoring.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Konfigürasyon verilerini taşıyan DTO sınıfı.
 */
@Data
@Builder
public class ConfigurationDTO {

    private HostConfigDTO host;
    private EndpointsConfigDTO endpoints;
    private NotificationsConfigDTO notifications;

    /**
     * Host konfigürasyonu DTO sınıfı.
     */
    @Data
    @Builder
    public static class HostConfigDTO {
        private boolean enabled;
        private long interval;
        private Map<String, Integer> thresholds;
    }

    /**
     * Endpoints konfigürasyonu DTO sınıfı.
     */
    @Data
    @Builder
    public static class EndpointsConfigDTO {
        private HttpConfigDTO http;
        private WebSocketConfigDTO websocket;
        private DatabaseConfigDTO database;

        /**
         * HTTP konfigürasyonu DTO sınıfı.
         */
        @Data
        @Builder
        public static class HttpConfigDTO {
            private boolean enabled;
            private List<HttpEndpointDTO> endpoints;

            /**
             * HTTP endpoint DTO sınıfı.
             */
            @Data
            @Builder
            public static class HttpEndpointDTO {
                private String id;
                private boolean enabled;
                private String url;
                private String method;
                private Map<String, String> headers;
                private String body;
                private long interval;
                private long timeout;
                private long threshold;
                private int expectedStatus;
            }
        }

        /**
         * WebSocket konfigürasyonu DTO sınıfı.
         */
        @Data
        @Builder
        public static class WebSocketConfigDTO {
            private boolean enabled;
            private List<WebSocketEndpointDTO> endpoints;

            /**
             * WebSocket endpoint DTO sınıfı.
             */
            @Data
            @Builder
            public static class WebSocketEndpointDTO {
                private String id;
                private boolean enabled;
                private String url;
                private String protocol;
                private long interval;
                private long timeout;
                private String testMessage;
                private String expectedResponse;
            }
        }

        /**
         * Veritabanı konfigürasyonu DTO sınıfı.
         */
        @Data
        @Builder
        public static class DatabaseConfigDTO {
            private boolean enabled;
            private List<DatabaseEndpointDTO> endpoints;

            /**
             * Veritabanı endpoint DTO sınıfı.
             */
            @Data
            @Builder
            public static class DatabaseEndpointDTO {
                private String id;
                private boolean enabled;
                private String type;
                private String url;
                private String username;
                private String password;
                private long interval;
                private String query;
                private long threshold;
            }
        }
    }

    /**
     * Bildirim konfigürasyonu DTO sınıfı.
     */
    @Data
    @Builder
    public static class NotificationsConfigDTO {
        private EmailConfigDTO email;
        private ZabbixConfigDTO zabbix;
        private PrometheusConfigDTO prometheus;

        /**
         * E-posta konfigürasyonu DTO sınıfı.
         */
        @Data
        @Builder
        public static class EmailConfigDTO {
            private boolean enabled;
            private String recipients;
            private String sender;
            private Map<String, String> templates;
        }

        /**
         * Zabbix konfigürasyonu DTO sınıfı.
         */
        @Data
        @Builder
        public static class ZabbixConfigDTO {
            private boolean enabled;
            private String server;
            private int port;
            private String host;
            private Map<String, String> items;
        }

        /**
         * Prometheus konfigürasyonu DTO sınıfı.
         */
        @Data
        @Builder
        public static class PrometheusConfigDTO {
            private boolean enabled;
            private String endpoint;
            private int port;
            private Map<String, String> labels;
        }
    }
}