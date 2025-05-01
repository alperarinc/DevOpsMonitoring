package com.monitoring.dto;

import com.monitoring.enums.Status; // Yeni import
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Metrik verilerini taşıyan DTO sınıfı.
 */
@Data
@Builder
public class MetricsDTO {

    private List<HostMetricDTO> hostMetrics;
    private List<EndpointMetricDTO> endpointMetrics;

    /**
     * Host metriklerini taşıyan DTO sınıfı.
     */
    @Data
    @Builder
    public static class HostMetricDTO {
        private String resource;
        private double value;
        private double threshold;
        private LocalDateTime timestamp;
        private String status;
    }

    /**
     * Endpoint metriklerini taşıyan DTO sınıfı.
     */
    @Data
    @Builder
    public static class EndpointMetricDTO {

        @Id
        @Schema(description = "Veritabanı tarafından otomatik oluşturulur", accessMode = Schema.AccessMode.READ_ONLY)
        private Long id;

        private String type;
        private String url;
        private boolean status;
        private long responseTime;
        private long threshold;
        private LocalDateTime timestamp;
        private Status monitoringStatus;
    }
}