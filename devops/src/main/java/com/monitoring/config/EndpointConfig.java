package com.monitoring.config;

import com.monitoring.enums.HttpMethod;
import com.monitoring.enums.Protocol;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Endpoint izleme yapılandırması
 */
@Configuration
@RefreshScope
@Data
public class EndpointConfig {

    /**
     * HTTP endpoint yapılandırması
     */
    private HttpConfig http;

    /**
     * WebSocket endpoint yapılandırması
     */
    private WebSocketConfig websocket;

    /**
     * Veritabanı endpoint yapılandırması
     */
    private DatabaseConfig database;

    /**
     * HTTP endpoint yapılandırma sınıfı
     */
    @Data
    public static class HttpConfig {
        /**
         * HTTP izleme etkin mi?
         */
        @Value("${endpoints.http.enabled:true}")
        private boolean enabled = true;

        /**
         * Varsayılan izleme aralığı (milisaniye)
         */
        @Value("${endpoints.http.default-interval:15000}")
        private long defaultInterval = 15000;

        /**
         * Varsayılan zaman aşımı süresi (milisaniye)
         */
        @Value("${endpoints.http.default-timeout:5000}")
        private long defaultTimeout = 5000;

        /**
         * Varsayılan yanıt süresi eşiği (milisaniye)
         */
        @Value("${endpoints.http.default-threshold:500}")
        private long defaultThreshold = 500;

        /**
         * Varsayılan beklenen HTTP durum kodu
         */
        @Value("${endpoints.http.default-expected-status:200}")
        private int defaultExpectedStatus = 200;

        /**
         * HTTP endpoint listesi
         */
        private List<HttpEndpoint> endpoints;

        /**
         * HTTP endpoint yapılandırma sınıfı
         */
        @Data
        public static class HttpEndpoint {
            /**
             * Endpoint benzersiz tanımlayıcısı
             */
            private String id;

            /**
             * Endpoint etkin mi?
             */
            private boolean enabled = true;

            /**
             * Endpoint URL'i
             */
            private String url;

            /**
             * HTTP metodu (GET, POST, vb.)
             */
            private org.springframework.http.HttpMethod method = org.springframework.http.HttpMethod.GET;

            /**
             * HTTP başlıkları
             */
            private Map<String, String> headers;

            /**
             * İstek gövdesi (POST, PUT için)
             */
            private String body;

            /**
             * İzleme aralığı (milisaniye)
             */
            private long interval;

            /**
             * Zaman aşımı süresi (milisaniye)
             */
            private long timeout;

            /**
             * Yanıt süresi eşiği (milisaniye)
             */
            private long threshold;

            /**
             * Beklenen HTTP durum kodu
             */
            private int expectedStatus;

            /**
             * Beklenen yanıt içeriği (isteğe bağlı)
             */
            private String expectedResponseContains;

            /**
             * HttpMethod enum'unu Spring HttpMethod'a dönüştürür
             */
            public void setMethod(HttpMethod method) {
                if (method == null) {
                    this.method = null;
                    return;
                }

                if (HttpMethod.GET.equals(method)) {
                    this.method = org.springframework.http.HttpMethod.GET;
                } else if (HttpMethod.POST.equals(method)) {
                    this.method = org.springframework.http.HttpMethod.POST;
                } else if (HttpMethod.PUT.equals(method)) {
                    this.method = org.springframework.http.HttpMethod.PUT;
                } else if (HttpMethod.DELETE.equals(method)) {
                    this.method = org.springframework.http.HttpMethod.DELETE;
                } else {
                    throw new IllegalArgumentException("Desteklenmeyen HTTP metodu: " + method);
                }
            }

            /**
             * Mevcut Spring HttpMethod'unu HttpMethod enum'a dönüştürür
             */
            public HttpMethod getEntityMethod() {
                if (this.method == null) {
                    return null;
                }

                if (org.springframework.http.HttpMethod.GET.equals(this.method)) {
                    return HttpMethod.GET;
                } else if (org.springframework.http.HttpMethod.POST.equals(this.method)) {
                    return HttpMethod.POST;
                } else if (org.springframework.http.HttpMethod.PUT.equals(this.method)) {
                    return HttpMethod.PUT;
                } else if (org.springframework.http.HttpMethod.DELETE.equals(this.method)) {
                    return HttpMethod.DELETE;
                } else {
                    throw new IllegalArgumentException("Desteklenmeyen HTTP metodu: " + this.method);
                }
            }
        }
    }

    /**
     * WebSocket endpoint yapılandırma sınıfı
     */
    @Data
    public static class WebSocketConfig {
        /**
         * WebSocket izleme etkin mi?
         */
        @Value("${endpoints.websocket.enabled:true}")
        private boolean enabled = true;

        /**
         * Varsayılan izleme aralığı (milisaniye)
         */
        @Value("${endpoints.websocket.default-interval:60000}")
        private long defaultInterval = 60000;

        /**
         * Varsayılan zaman aşımı süresi (milisaniye)
         */
        @Value("${endpoints.websocket.default-timeout:10000}")
        private long defaultTimeout = 10000;

        /**
         * WebSocket endpoint listesi
         */
        private List<WebSocketEndpoint> endpoints;

        /**
         * WebSocket endpoint yapılandırma sınıfı
         */
        @Data
        public static class WebSocketEndpoint {
            /**
             * Endpoint benzersiz tanımlayıcısı
             */
            private String id;

            /**
             * Endpoint etkin mi?
             */
            private boolean enabled = true;

            /**
             * Endpoint URL'i
             */
            private String url;

            /**
             * WebSocket protokol versiyonu
             */
            @Setter
            @Enumerated(EnumType.STRING)
            @Column(name = "protocol")
            private Protocol protocol;

            /**
             * İzleme aralığı (milisaniye)
             */
            private long interval;

            /**
             * Zaman aşımı süresi (milisaniye)
             */
            private long timeout;

            /**
             * Test mesajı
             */
            private String testMessage;

            /**
             * Beklenen yanıt
             */
            private String expectedResponse;

            /**
             * Beklenen yanıt içeriği (isteğe bağlı)
             */
            private String expectedResponseContains;

            /**
             * Mevcut Protocol'ü döndürür
             */
            public Protocol getEntityProtocol() {
                return this.protocol;
            }
        }
    }

    /**
     * Veritabanı endpoint yapılandırma sınıfı
     */
    @Data
    public static class DatabaseConfig {
        /**
         * Veritabanı izleme etkin mi?
         */
        @Value("${endpoints.database.enabled:true}")
        private boolean enabled = true;

        /**
         * Varsayılan izleme aralığı (milisaniye)
         */
        @Value("${endpoints.database.default-interval:120000}")
        private long defaultInterval = 120000;

        /**
         * Varsayılan yanıt süresi eşiği (milisaniye)
         */
        @Value("${endpoints.database.default-threshold:1000}")
        private long defaultThreshold = 1000;

        /**
         * Veritabanı endpoint listesi
         */
        private List<DatabaseEndpoint> endpoints;

        /**
         * Veritabanı endpoint yapılandırma sınıfı
         */
        @Data
        public static class DatabaseEndpoint {
            /**
             * Endpoint benzersiz tanımlayıcısı
             */
            private String id;

            /**
             * Endpoint etkin mi?
             */
            private boolean enabled = true;

            /**
             * Veritabanı türü (mysql, postgresql, vb.)
             */
            private String type;

            /**
             * JDBC URL'i
             */
            private String url;

            /**
             * Veritabanı kullanıcı adı
             */
            private String username;

            /**
             * Veritabanı şifresi
             */
            private String password;

            /**
             * İzleme aralığı (milisaniye)
             */
            private long interval;

            /**
             * Test sorgusu
             */
            private String query;

            /**
             * Yanıt süresi eşiği (milisaniye)
             */
            private long threshold;
        }
    }
}