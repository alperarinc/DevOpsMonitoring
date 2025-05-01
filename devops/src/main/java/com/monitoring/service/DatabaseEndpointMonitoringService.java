package com.monitoring.service;

import com.monitoring.enums.AlertLevel;
import com.monitoring.enums.DatabaseType;
import com.monitoring.enums.MonitoringType;
import com.monitoring.enums.Status;
import com.monitoring.model.AlertEvent;
import com.monitoring.model.EndpointConfigEntity;
import com.monitoring.model.MonitoringResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

/**
 * Veritabanı endpoint'leri izleyen servis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseEndpointMonitoringService {

    private final NotificationCoordinator notificationCoordinator;
    private final Random random = new Random(); // ID üretimi için

    /**
     * Long türünde ID üretir
     *
     * @return Benzersiz Long ID
     */
    private Long generateLongId() {
        return Math.abs(random.nextLong());
    }

    /**
     * Veritabanı endpoint'i izler ve sonucu döndürür.
     *
     * @param endpoint İzlenecek endpoint konfigürasyonu
     * @return İzleme sonucu
     */
    public MonitoringResult monitorEndpoint(EndpointConfigEntity endpoint) {
        log.debug("Veritabanı endpoint izleme başlatıldı: {}", endpoint.getId());

        MonitoringResult result = MonitoringResult.builder()
                .id(generateLongId())
                .timestamp(LocalDateTime.now())
                .type(MonitoringType.DATABASE)
                .resourceId(String.valueOf(generateLongId()))
                .endpointConfig(endpoint)
                .build();

        long startTime = System.currentTimeMillis();

        try {
            DriverManagerDataSource dataSource = createDataSource(endpoint);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            testConnection(dataSource);

            String query = endpoint.getQuery();
            if (query == null || query.isEmpty()) {
                if (endpoint.getDbType() == DatabaseType.POSTGRESQL ||
                        endpoint.getDbType() == DatabaseType.MYSQL ||
                        endpoint.getDbType() == DatabaseType.MSSQL) {
                    query = "SELECT 1";
                } else if (endpoint.getDbType() == DatabaseType.ORACLE) {
                    query = "SELECT 1 FROM DUAL";
                } else {
                    query = "SELECT 1";
                }
            }

            Map<String, Object> queryResult = jdbcTemplate.queryForMap(query);

            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            log.debug("Veritabanı yanıtı alındı: {} ms", responseTime);

            boolean success = true;
            Status status = Status.OK;

            if (endpoint.getThresholdMs() != null && responseTime > endpoint.getThresholdMs()) {
                status = Status.WARNING;
            }

            result.setSuccess(success);
            result.setResponseTime(responseTime);
            result.setStatus(status);
            result.setData("Response Time: " + responseTime + "ms, Result: " + queryResult);

            if (status != Status.OK) {
                sendAlert(result, status);
            }

            return result;

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            log.error("Veritabanı endpoint izleme hatası: {}", endpoint.getUrl(), e);

            result.setSuccess(false);
            result.setResponseTime(responseTime);
            result.setStatus(Status.CRITICAL);
            result.setErrorMessage(e.getMessage());

            sendAlert(result, Status.CRITICAL);

            return result;
        }
    }

    /**
     * JDBC veri kaynağı oluşturur.
     *
     * @param endpoint Endpoint konfigürasyonu
     * @return JDBC veri kaynağı
     */
    private DriverManagerDataSource createDataSource(EndpointConfigEntity endpoint) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(endpoint.getUrl());

        if (endpoint.getDbUsername() != null) {
            dataSource.setUsername(endpoint.getDbUsername());
        }
        if (endpoint.getDbPassword() != null) {
            dataSource.setPassword(endpoint.getDbPassword());
        }

        if (endpoint.getDbType() != null) {
            switch (endpoint.getDbType()) {
                case POSTGRESQL:
                    dataSource.setDriverClassName("org.postgresql.Driver");
                    break;
                case MYSQL:
                    dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
                    break;
                case ORACLE:
                    dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
                    break;
                case MSSQL:
                    dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    break;
                default:
                    dataSource.setDriverClassName("org.h2.Driver");
            }
        }

        return dataSource;
    }

    /**
     * Veritabanı bağlantısını test eder.
     *
     * @param dataSource Veri kaynağı
     * @throws SQLException Bağlantı hatası
     */
    private void testConnection(DriverManagerDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // Bağlantı başarılı
        }
    }

    /**
     * Uyarı gönderir.
     *
     * @param result İzleme sonucu
     * @param status Durum
     */
    private void sendAlert(MonitoringResult result, Status status) {
        AlertLevel level = status == Status.CRITICAL ? AlertLevel.CRITICAL : AlertLevel.WARNING;

        String message = String.format(
                "Veritabanı endpoint izleme uyarısı: %s - %s - %s",
                result.getEndpointConfig().getUrl(),
                status,
                result.isSuccess()
                        ? "Yanıt süresi: " + result.getResponseTime() + "ms"
                        : "Hata: " + result.getErrorMessage()
        );

        AlertEvent alertEvent = AlertEvent.builder()
                .id(generateLongId())
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
