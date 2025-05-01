package com.monitoring.endpoint;

import com.monitoring.config.EndpointConfig;
import com.monitoring.enums.AlertLevel;
import com.monitoring.model.AlertEvent;
import com.monitoring.model.MonitoringResult;
import com.monitoring.enums.MonitoringType; // Enum için yeni import
import com.monitoring.enums.Status; // Enum için yeni import
import com.monitoring.service.NotificationService;
import com.monitoring.service.DatabaseEndpointConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Veritabanı endpoint izleme servisi.
 */
@Service
@ConditionalOnProperty(value = "endpoints.database.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DatabaseEndpointMonitor implements EndpointMonitor<EndpointConfig.DatabaseConfig.DatabaseEndpoint> {

    private final DatabaseEndpointConfigService databaseEndpointConfigService;
    private final TaskScheduler taskScheduler;
    private final List<NotificationService> notificationServices;
    private final Environment environment;
    private final Random random = new Random(); // ID üretimi için
    // MonitoringResultRepository kaldırıldı, çünkü cascade ile otomatik kaydedilecek

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<>();
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
            log.info("Starting database endpoint monitoring from database...");

            List<EndpointConfig.DatabaseConfig.DatabaseEndpoint> databaseEndpoints = databaseEndpointConfigService.getDatabaseEndpoints();

            if (databaseEndpoints == null || databaseEndpoints.isEmpty()) {
                log.warn("No database endpoints configured in the database for monitoring");
                return;
            }

            databaseEndpoints.forEach(endpoint -> {
                if (endpoint.isEnabled()) {
                    createJdbcTemplate(endpoint);
                    scheduleEndpointMonitoring(endpoint);
                }
            });
        }
    }

    @Override
    public void stopMonitoring() {
        if (running.compareAndSet(true, false)) {
            log.info("Stopping database endpoint monitoring...");

            scheduledTasks.forEach((id, future) -> {
                future.cancel(true);
            });

            scheduledTasks.clear();
            jdbcTemplates.clear();
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private void createJdbcTemplate(EndpointConfig.DatabaseConfig.DatabaseEndpoint endpoint) {
        try {
            String url = resolveProperties(endpoint.getUrl());
            String username = resolveProperties(endpoint.getUsername());
            String password = resolveProperties(endpoint.getPassword());

            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);

            String type = endpoint.getType().toLowerCase();
            switch (type) {
                case "mysql":
                    dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
                    break;
                case "postgresql":
                    dataSource.setDriverClassName("org.postgresql.Driver");
                    break;
                case "oracle":
                    dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
                    break;
                case "sqlserver":
                    dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    break;
                default:
                    log.warn("Unknown database type: {}. Using default JDBC driver.", type);
            }

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplates.put(endpoint.getId(), jdbcTemplate);

        } catch (Exception e) {
            log.error("Error creating JdbcTemplate for database endpoint: {}", endpoint.getId(), e);
        }
    }

    private void scheduleEndpointMonitoring(EndpointConfig.DatabaseConfig.DatabaseEndpoint endpoint) {
        log.info("Scheduling database endpoint monitoring: {}", endpoint.getId());

        ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(
                () -> {
                    try {
                        MonitoringResult result = monitor(endpoint);

                        if (result.getStatus() != Status.OK) {
                            // Eşik değeri aşıldı veya hata oluştu, bildirim gönder
                            AlertEvent alertEvent = createAlertEvent(result);
                            // Tüm notification servislerini kullanarak bildirimi gönder
                            for (NotificationService service : notificationServices) {
                                try {
                                    service.sendAlert(alertEvent);
                                } catch (Exception e) {
                                    log.error("Error sending alert to service: {}", service.getClass().getSimpleName(), e);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error while monitoring database endpoint: {}", endpoint.getId(), e);
                    }
                },
                endpoint.getInterval()
        );

        scheduledTasks.put(endpoint.getId(), future);
    }

    @Override
    public MonitoringResult monitor(EndpointConfig.DatabaseConfig.DatabaseEndpoint endpoint) {
        log.debug("Monitoring database endpoint: {}", endpoint.getId());

        long startTime = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = null;
        Connection connection = null;

        try {
            JdbcTemplate jdbcTemplate = jdbcTemplates.get(endpoint.getId());

            if (jdbcTemplate == null) {
                throw new IllegalStateException("JdbcTemplate not found for endpoint: " + endpoint.getId());
            }

            String query = endpoint.getQuery();
            jdbcTemplate.queryForObject(query, Object.class);

            success = true;
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("Error closing database connection", e);
                }
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
                .type(MonitoringType.DATABASE)
                .resourceId(endpoint.getId())
                .success(success)
                .responseTime(responseTime)
                .status(status)
                .errorMessage(errorMessage)
                .data("URL: " + endpoint.getUrl() + ", Type: " + endpoint.getType() + ", Query: " + endpoint.getQuery())
                .build();

        log.debug("Database monitoring result: {}", result);
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
            message = "Response time exceeded threshold for database endpoint: " + result.getResourceId() +
                    " (" + result.getResponseTime() + "ms)";
        } else {
            message = "Failed to connect to database endpoint: " + result.getResourceId() +
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