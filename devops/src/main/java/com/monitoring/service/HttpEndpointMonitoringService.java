package com.monitoring.service;

import com.monitoring.enums.AlertLevel;
import com.monitoring.model.AlertEvent;
import com.monitoring.model.EndpointConfigEntity;
import com.monitoring.model.MonitoringResult;
import com.monitoring.enums.MonitoringType;
import com.monitoring.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * HTTP endpoint'leri izleyen servis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HttpEndpointMonitoringService {

    private final NotificationCoordinator notificationCoordinator;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final Random random = new Random(); // ID üretimi için

    /**
     * Long türünde ID üretir
     * @return Benzersiz Long ID
     */
    private Long generateLongId() {
        // Basit bir numara üreteci - gerçek projede sequence veya başka bir strateji kullanılabilir
        return Math.abs(random.nextLong());
    }

    /**
     * HTTP endpoint'i izler ve sonucu döndürür.
     *
     * @param endpoint İzlenecek endpoint konfigürasyonu
     * @return İzleme sonucu
     */
    public MonitoringResult monitorEndpoint(EndpointConfigEntity endpoint) {
        log.debug("HTTP endpoint izleme başlatıldı: {}", endpoint.getId());

        // Sonuç nesnesi oluştur
        MonitoringResult result = MonitoringResult.builder()
                .id(generateLongId()) // String UUID yerine Long ID
                .timestamp(LocalDateTime.now())
                .type(MonitoringType.HTTP)
                .resourceId(endpoint.getId().toString()) // Long ID'yi String'e çevir
                .endpointConfig(endpoint)
                .build();

        long startTime = System.currentTimeMillis();

        try {
            // HTTP headers oluştur
            HttpHeaders headers = new HttpHeaders();
            if (endpoint.getHeaders() != null && !endpoint.getHeaders().isEmpty()) {
                try {
                    Map<String, String> headerMap = objectMapper.readValue(endpoint.getHeaders(),
                            objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class));
                    headerMap.forEach(headers::add);
                } catch (Exception e) {
                    log.warn("HTTP başlıkları parse edilemedi: {}", endpoint.getHeaders(), e);
                }
            }

            // HTTP body oluştur
            Object body = null;
            if (endpoint.getBody() != null && !endpoint.getBody().isEmpty()) {
                body = endpoint.getBody();
            }

            // HTTP isteği yap
            HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);
            HttpMethod method = endpoint.getMethod() != null ?
                    HttpMethod.valueOf(endpoint.getMethod().name()) : HttpMethod.GET;

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint.getUrl(),
                    method,
                    requestEntity,
                    String.class
            );

            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            log.debug("HTTP yanıtı alındı: {} - {} ms", response.getStatusCode(), responseTime);

            // Beklenen durum kodu kontrolü
            boolean statusOk = endpoint.getExpectedStatus() == null ||
                    response.getStatusCodeValue() == endpoint.getExpectedStatus();

            // Beklenen içerik kontrolü
            boolean contentOk = endpoint.getExpectedResponseContains() == null ||
                    (response.getBody() != null &&
                            response.getBody().contains(endpoint.getExpectedResponseContains()));

            boolean success = statusOk && contentOk;
            Status status = success ? Status.OK : Status.CRITICAL;

            // Yanıt süresini threshold ile karşılaştır
            if (success && endpoint.getThresholdMs() != null && responseTime > endpoint.getThresholdMs()) {
                status = Status.WARNING;
            }

            // Sonuç oluştur
            result.setSuccess(success);
            result.setResponseTime(responseTime);
            result.setStatus(status);
            result.setData("Status: " + response.getStatusCodeValue() +
                    ", Response Time: " + responseTime + "ms" +
                    (response.getBody() != null ? ", Content Length: " + response.getBody().length() : ""));

            // Başarısız veya yanıt süresi uzun ise uyarı gönder
            if (status != Status.OK) {
                sendAlert(result, status);
            }

            return result;

        } catch (RestClientException e) {
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            log.error("HTTP endpoint izleme hatası: {}", endpoint.getUrl(), e);

            // Hata sonucu oluştur
            result.setSuccess(false);
            result.setResponseTime(responseTime);
            result.setStatus(Status.CRITICAL);
            result.setErrorMessage(e.getMessage());

            // Uyarı gönder
            sendAlert(result, Status.CRITICAL);

            return result;
        }
    }

    /**
     * Uyarı gönderir.
     *
     * @param result İzleme sonucu
     * @param status Durum
     */
    private void sendAlert(MonitoringResult result, Status status) {
        AlertLevel level = status == Status.CRITICAL ?
                AlertLevel.CRITICAL : AlertLevel.WARNING;

        String message = String.format(
                "HTTP endpoint izleme uyarısı: %s - %s - %s",
                result.getEndpointConfig().getUrl(),
                status,
                result.isSuccess() ? "Yanıt süresi: " + result.getResponseTime() + "ms" :
                        "Hata: " + result.getErrorMessage()
        );

        AlertEvent alertEvent = AlertEvent.builder()
                .id(generateLongId()) // String UUID yerine Long ID
                .timestamp(LocalDateTime.now())
                .monitoringResult(result)
                .endpointConfig(result.getEndpointConfig())
                .level(level)
                .message(message)
                .resolved(false)
                .build();

        notificationCoordinator.sendAlert(alertEvent);
    }
}