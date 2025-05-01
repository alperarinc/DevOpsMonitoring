package com.monitoring.service;

import com.monitoring.enums.AlertLevel;
import com.monitoring.model.AlertEvent;
import com.monitoring.model.EndpointConfigEntity;
import com.monitoring.model.MonitoringResult;
import com.monitoring.enums.MonitoringType;
import com.monitoring.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket endpoint'leri izleyen servis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketEndpointMonitoringService {

    private final NotificationCoordinator notificationCoordinator;

    /**
     * WebSocket endpoint'i izler ve sonucu döndürür.
     *
     * @param endpoint İzlenecek endpoint konfigürasyonu
     * @return İzleme sonucu
     */
    public MonitoringResult monitorEndpoint(EndpointConfigEntity endpoint) {
        log.debug("WebSocket endpoint izleme başlatıldı: {}", endpoint.getId());

        // Sonuç nesnesi oluştur
        MonitoringResult result = MonitoringResult.builder()
                // ID veritabanı tarafından üretilecek, bu yüzden burada set edilmiyor
                .timestamp(LocalDateTime.now())
                .type(MonitoringType.WEBSOCKET)
                .resourceId(String.valueOf(endpoint.getId()))
                .endpointConfig(endpoint)
                .build();

        long startTime = System.currentTimeMillis();

        try {
            // WebSocket istemcisi ve işleyici oluştur
            StandardWebSocketClient client = new StandardWebSocketClient();
            CompletableFuture<String> messageFuture = new CompletableFuture<>();

            WebSocketHandler handler = new TestWebSocketHandler(messageFuture, endpoint.getTestMessage());

            // WebSocket bağlantısı kur ve süreyi ölç
            CompletableFuture<WebSocketSession> sessionFuture = client.execute(handler,
                    endpoint.getProtocol() != null ? endpoint.getProtocol().name() : null,
                    URI.create(endpoint.getUrl()));

            // Bağlantıyı belirli bir süre içinde kurmayı dene
            WebSocketSession session = sessionFuture.get(
                    endpoint.getTimeoutMs() != null ? endpoint.getTimeoutMs() : 5000,
                    TimeUnit.MILLISECONDS);

            // Yanıt bekle
            String response = null;
            if (endpoint.getTestMessage() != null && !endpoint.getTestMessage().isEmpty()) {
                response = messageFuture.get(
                        endpoint.getTimeoutMs() != null ? endpoint.getTimeoutMs() : 5000,
                        TimeUnit.MILLISECONDS);
            }

            // Oturumu kapat
            session.close();

            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            log.debug("WebSocket yanıtı alındı: {} ms", responseTime);

            // Beklenen yanıt kontrolü
            boolean contentOk = true;
            if (endpoint.getExpectedResponse() != null && !endpoint.getExpectedResponse().isEmpty()) {
                contentOk = response != null && response.equals(endpoint.getExpectedResponse());
            } else if (endpoint.getExpectedResponseContains() != null && !endpoint.getExpectedResponseContains().isEmpty()) {
                contentOk = response != null && response.contains(endpoint.getExpectedResponseContains());
            }

            boolean success = contentOk;
            Status status = success ? Status.OK : Status.CRITICAL;

            // Yanıt süresini threshold ile karşılaştır
            if (success && endpoint.getThresholdMs() != null && responseTime > endpoint.getThresholdMs()) {
                status = Status.WARNING;
            }

            // Sonuç oluştur
            result.setSuccess(success);
            result.setResponseTime(responseTime);
            result.setStatus(status);
            result.setData("Response Time: " + responseTime + "ms" +
                    (response != null ? ", Response: " + response : ""));

            // Başarısız veya yanıt süresi uzun ise uyarı gönder
            if (status != Status.OK) {
                sendAlert(result, status);
            }

            return result;

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            log.error("WebSocket endpoint izleme hatası: {}", endpoint.getUrl(), e);

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
                "WebSocket endpoint izleme uyarısı: %s - %s - %s",
                result.getEndpointConfig().getUrl(),
                status,
                result.isSuccess() ? "Yanıt süresi: " + result.getResponseTime() + "ms" :
                        "Hata: " + result.getErrorMessage()
        );

        AlertEvent alertEvent = AlertEvent.builder()
                // ID veritabanı tarafından üretilecek
                .timestamp(LocalDateTime.now())
                .monitoringResult(result)
                .endpointConfig(result.getEndpointConfig())
                .level(level)
                .message(message)
                .resolved(false)
                .build();

        notificationCoordinator.sendAlert(alertEvent);
    }

    /**
     * WebSocket test işleyicisi.
     */
    private static class TestWebSocketHandler implements WebSocketHandler {

        private final CompletableFuture<String> messageFuture;
        private final String testMessage;

        public TestWebSocketHandler(CompletableFuture<String> messageFuture, String testMessage) {
            this.messageFuture = messageFuture;
            this.testMessage = testMessage;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            if (testMessage != null && !testMessage.isEmpty()) {
                session.sendMessage(new TextMessage(testMessage));
            }
        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
            if (message instanceof TextMessage) {
                messageFuture.complete(((TextMessage) message).getPayload());
            } else {
                messageFuture.complete(message.toString());
            }
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            messageFuture.completeExceptionally(exception);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
            if (!messageFuture.isDone()) {
                messageFuture.complete(null);
            }
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }
    }
}