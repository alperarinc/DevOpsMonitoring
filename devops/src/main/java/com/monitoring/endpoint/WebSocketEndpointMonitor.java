package com.monitoring.endpoint;

import com.monitoring.config.EndpointConfig;
import com.monitoring.model.AlertEvent;
import com.monitoring.enums.AlertLevel;
import com.monitoring.model.MonitoringResult;
import com.monitoring.enums.MonitoringType;
import com.monitoring.enums.Status;
import com.monitoring.service.NotificationService;
import com.monitoring.service.DatabaseEndpointConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebSocket endpoint izleme servisi.
 */
@Service
@ConditionalOnProperty(value = "endpoints.websocket.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class WebSocketEndpointMonitor implements EndpointMonitor<EndpointConfig.WebSocketConfig.WebSocketEndpoint> {

    private final DatabaseEndpointConfigService databaseEndpointConfigService;
    private final TaskScheduler taskScheduler;
    private final List<NotificationService> notificationServices;
    private final Environment environment;
    private final Random random = new Random(); // ID üretimi için

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Long türünde ID üretir
     * @return Benzersiz Long ID
     */
    private Long generateLongId() {
        // Basit bir numara üreteci - gerçek projede sequence veya başka bir strateji kullanılabilir
        return Math.abs(random.nextLong());
    }

    @PostConstruct
    public void init() {
        startMonitoring();
    }

    @PreDestroy
    public void shutdown() {
        stopMonitoring();
    }

    @Override
    public void startMonitoring() {
        if (running.compareAndSet(false, true)) {
            log.info("Starting WebSocket endpoint monitoring from database...");

            List<EndpointConfig.WebSocketConfig.WebSocketEndpoint> websocketEndpoints = databaseEndpointConfigService.getWebSocketEndpoints();

            if (websocketEndpoints == null || websocketEndpoints.isEmpty()) {
                log.warn("No WebSocket endpoints configured in the database for monitoring");
                return;
            }

            websocketEndpoints.forEach(endpoint -> {
                if (endpoint.isEnabled()) {
                    scheduleEndpointMonitoring(endpoint);
                }
            });
        }
    }

    @Override
    public void stopMonitoring() {
        if (running.compareAndSet(true, false)) {
            log.info("Stopping WebSocket endpoint monitoring...");

            scheduledTasks.forEach((id, future) -> {
                future.cancel(true);
            });

            scheduledTasks.clear();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private void scheduleEndpointMonitoring(EndpointConfig.WebSocketConfig.WebSocketEndpoint endpoint) {
        log.info("Scheduling WebSocket endpoint monitoring: {}", endpoint.getId());

        ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(
                () -> {
                    try {
                        MonitoringResult result = monitor(endpoint);

                        if (result.getStatus() != Status.OK) {
                            AlertEvent alertEvent = createAlertEvent(result);
                            for (NotificationService service : notificationServices) {
                                service.sendAlert(alertEvent);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error while monitoring WebSocket endpoint: {}", endpoint.getId(), e);
                    }
                },
                endpoint.getInterval()
        );

        scheduledTasks.put(endpoint.getId(), future);
    }

    @Override
    public MonitoringResult monitor(EndpointConfig.WebSocketConfig.WebSocketEndpoint endpoint) {
        log.debug("Monitoring WebSocket endpoint: {}", endpoint.getId());

        long startTime = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        String responseData = null;

        try {
            StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
            String url = resolveProperties(endpoint.getUrl());

            CompletableFuture<String> responseFuture = new CompletableFuture<>();

            WebSocketHandler webSocketHandler = new TextWebSocketHandler() {
                @Override
                public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                    log.debug("WebSocket connection established to: {}", url);

                    if (endpoint.getTestMessage() != null && !endpoint.getTestMessage().isEmpty()) {
                        session.sendMessage(new TextMessage(endpoint.getTestMessage()));
                    }

                    taskScheduler.schedule(() -> {
                        if (!responseFuture.isDone()) {
                            try {
                                session.close(CloseStatus.NORMAL);
                                responseFuture.completeExceptionally(new RuntimeException("WebSocket response timeout"));
                            } catch (Exception e) {
                                log.error("Error closing WebSocket session", e);
                            }
                        }
                    }, Instant.ofEpochSecond(System.currentTimeMillis() + endpoint.getTimeout()));
                }

                @Override
                public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                    String payload = message.getPayload().toString();
                    log.debug("Received WebSocket message: {}", payload);

                    if (endpoint.getExpectedResponse() == null ||
                            endpoint.getExpectedResponse().isEmpty() ||
                            payload.contains(endpoint.getExpectedResponse())) {
                        responseFuture.complete(payload);
                        session.close(CloseStatus.NORMAL);
                    }
                    if (endpoint.getExpectedResponseContains() != null && !payload.contains(endpoint.getExpectedResponseContains())) {
                        responseFuture.completeExceptionally(new RuntimeException("Expected response content not found: " + endpoint.getExpectedResponseContains()));
                        session.close(CloseStatus.NORMAL);
                    }
                }

                @Override
                public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                    log.error("WebSocket transport error", exception);
                    responseFuture.completeExceptionally(exception);
                    if (session.isOpen()) {
                        session.close(CloseStatus.SERVER_ERROR);
                    }
                }

                @Override
                public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                    log.debug("WebSocket connection closed: {}", closeStatus);
                    if (!responseFuture.isDone()) {
                        responseFuture.completeExceptionally(new RuntimeException("WebSocket connection closed: " + closeStatus));
                    }
                }
            };

            webSocketClient.doHandshake(webSocketHandler, url).get(endpoint.getTimeout(), TimeUnit.MILLISECONDS);
            responseData = responseFuture.get(endpoint.getTimeout(), TimeUnit.MILLISECONDS);
            success = true;
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
        }

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        Status status;
        if (!success) {
            status = Status.CRITICAL;
        } else {
            status = Status.OK;
        }

        MonitoringResult result = MonitoringResult.builder()
                .id(generateLongId()) // String UUID yerine Long ID
                .timestamp(LocalDateTime.now())
                .type(MonitoringType.WEBSOCKET)
                .resourceId(endpoint.getId())
                .success(success)
                .responseTime(responseTime)
                .status(status)
                .errorMessage(errorMessage)
                .data("URL: " + endpoint.getUrl() + ", Response: " + (responseData != null ? responseData : "N/A"))
                .build();

        log.debug("WebSocket monitoring result: {}", result);
        return result;
    }

    private AlertEvent createAlertEvent(MonitoringResult result) {
        AlertLevel level;

        if (result.getStatus() == Status.CRITICAL) {
            level = AlertLevel.CRITICAL;
        } else if (result.getStatus() == Status.WARNING) {
            level = AlertLevel.WARNING;
        } else {
            level = AlertLevel.INFO;
        }

        String message;
        if (!result.isSuccess()) {
            message = "Failed to connect to WebSocket endpoint: " + result.getResourceId() +
                    " - " + result.getErrorMessage();
        } else {
            message = "WebSocket endpoint check issue: " + result.getResourceId();
        }

        return AlertEvent.builder()
                .id(generateLongId()) // String UUID yerine Long ID
                .timestamp(LocalDateTime.now())
                .monitoringResult(result)
                .level(level)
                .message(message)
                .resolved(false)
                .build();
    }

    private String resolveProperties(String value) {
        if (value == null || !value.contains("${")) {
            return value;
        }

        String result = value;
        int startIndex = result.indexOf("${");

        while (startIndex != -1) {
            int endIndex = result.indexOf("}", startIndex);
            if (endIndex == -1) {
                break;
            }

            String placeholder = result.substring(startIndex + 2, endIndex);
            String[] parts = placeholder.split(":", 2);
            String propertyName = parts[0];
            String defaultValue = parts.length > 1 ? parts[1] : "";

            String propertyValue = environment.getProperty(propertyName);
            if (propertyValue == null) {
                propertyValue = defaultValue;
            }

            result = result.substring(0, startIndex) + propertyValue + result.substring(endIndex + 1);
            startIndex = result.indexOf("${");
        }

        return result;
    }
}