package com.monitoring.endpoint;

import com.monitoring.config.EndpointConfig;
import com.monitoring.enums.AlertLevel;
import com.monitoring.enums.MonitoringType;
import com.monitoring.enums.Status;
import com.monitoring.model.AlertEvent;
import com.monitoring.model.MonitoringResult;
import com.monitoring.service.NotificationService;
import com.monitoring.service.DatabaseEndpointConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.http.HttpMethod.*;

/**
 * HTTP endpoint izleme servisi.
 */
@Service
@ConditionalOnProperty(value = "endpoints.http.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class HttpEndpointMonitor implements EndpointMonitor<EndpointConfig.HttpConfig.HttpEndpoint> {

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
            log.info("Starting HTTP endpoint monitoring from database...");

            List<EndpointConfig.HttpConfig.HttpEndpoint> httpEndpoints = databaseEndpointConfigService.getHttpEndpoints();

            if (httpEndpoints == null || httpEndpoints.isEmpty()) {
                log.warn("No HTTP endpoints configured in the database for monitoring");
                return;
            }

            httpEndpoints.forEach(endpoint -> {
                if (endpoint.isEnabled()) {
                    scheduleEndpointMonitoring(endpoint);
                }
            });
        }
    }

    @Override
    public void stopMonitoring() {
        if (running.compareAndSet(true, false)) {
            log.info("Stopping HTTP endpoint monitoring...");

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

    private void scheduleEndpointMonitoring(EndpointConfig.HttpConfig.HttpEndpoint endpoint) {
        log.info("Scheduling HTTP endpoint monitoring: {}", endpoint.getId());

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
                        log.error("Error while monitoring HTTP endpoint: {}", endpoint.getId(), e);
                    }
                },
                endpoint.getInterval()
        );

        scheduledTasks.put(endpoint.getId(), future);
    }

    private WebClient createWebClient(long timeoutMillis) {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofMillis(timeoutMillis))))
                .build();
    }

    @Override
    public MonitoringResult monitor(EndpointConfig.HttpConfig.HttpEndpoint endpoint) {
        log.debug("Monitoring HTTP endpoint: {}", endpoint.getId());

        long startTime = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        int statusCode = 0;
        String responseBody = null;

        try {
            String url = resolveProperties(endpoint.getUrl());
            HttpMethod method = endpoint.getMethod();

            WebClient client = createWebClient(endpoint.getTimeout());

            WebClient.RequestBodySpec requestSpec = client.method(method)
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON);

            if (endpoint.getHeaders() != null) {
                endpoint.getHeaders().forEach((key, value) -> {
                    requestSpec.header(key, resolveProperties(value));
                });
            }

            WebClient.RequestHeadersSpec<?> headersSpec;
            if (endpoint.getBody() != null && !endpoint.getBody().isEmpty() &&
                    (method == POST || method == PUT || method == PATCH)) {
                headersSpec = requestSpec.bodyValue(resolveProperties(endpoint.getBody()));
            } else {
                headersSpec = requestSpec;
            }

            WebClient.ResponseSpec responseSpec = headersSpec.retrieve();

            int expectedStatus = endpoint.getExpectedStatus();
            statusCode = responseSpec.toEntity(String.class).block().getStatusCode().value();
            responseBody = responseSpec.toEntity(String.class).block().getBody();

            if (statusCode != expectedStatus) {
                success = false;
                errorMessage = "Unexpected status code: " + statusCode + " (expected: " + expectedStatus + ")";
            } else {
                success = true;
                if (endpoint.getExpectedResponseContains() != null && !responseBody.contains(endpoint.getExpectedResponseContains())) {
                    success = false;
                    errorMessage = "Expected response content not found: " + endpoint.getExpectedResponseContains();
                }
            }

        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();

            if (e instanceof TimeoutException) {
                errorMessage = "Request timed out after " + endpoint.getTimeout() + "ms";
            }
        }

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        Status status;
        if (!success) {
            status = Status.CRITICAL;
        } else if (responseTime > endpoint.getThreshold()) {
            status = Status.WARNING;
            errorMessage = "Response time exceeded threshold: " + responseTime + "ms (threshold: " + endpoint.getThreshold() + "ms)";
        } else {
            status = Status.OK;
        }

        MonitoringResult result = MonitoringResult.builder()
                .id(generateLongId()) // String UUID yerine Long ID
                .timestamp(LocalDateTime.now())
                .type(MonitoringType.HTTP)
                .resourceId(endpoint.getId())
                .success(success)
                .responseTime(responseTime)
                .status(status)
                .errorMessage(errorMessage)
                .data("URL: " + endpoint.getUrl() +
                        ", Method: " + endpoint.getMethod() +
                        ", Status Code: " + statusCode +
                        (responseBody != null ? ", Body: " + responseBody.substring(0, Math.min(responseBody.length(), 100)) + "..." : ""))
                .build();

        log.debug("HTTP monitoring result: {}", result);
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
        if (result.isSuccess()) {
            message = "Response time exceeded threshold for HTTP endpoint: " + result.getResourceId() +
                    " (" + result.getResponseTime() + "ms)";
        } else {
            message = "Failed to connect to HTTP endpoint: " + result.getResourceId() +
                    " - " + result.getErrorMessage();
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