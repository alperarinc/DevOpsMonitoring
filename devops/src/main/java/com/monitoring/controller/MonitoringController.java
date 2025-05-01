package com.monitoring.controller;

import com.monitoring.dto.MetricsDTO;
import com.monitoring.model.HostMetrics;
import com.monitoring.service.HostMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Monitoring API Controller.
 */
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Monitoring", description = "Monitoring API - sistem ve endpoint metriklerini yönetir")
public class MonitoringController {

    private final HostMonitoringService hostMonitoringService;

    /**
     * Tüm metrikleri getirir.
     *
     * @return Metrics DTO
     */
    @Operation(summary = "Tüm metrikleri getir", description = "Host ve endpoint metriklerini içeren tüm metrikleri getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metrikler başarıyla alındı",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MetricsDTO.class))}),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası",
                    content = @Content)
    })
    @GetMapping("/metrics")
    public ResponseEntity<MetricsDTO> getMetrics() {
        log.info("Getting metrics");

        try {
            // Host metriklerini getir
            HostMetrics hostMetrics = hostMonitoringService.getHostMetrics();

            // Host metrikleri DTO'ya dönüştür
            List<MetricsDTO.HostMetricDTO> hostMetricsDTO = new ArrayList<>();

            // CPU metriği
            hostMetricsDTO.add(MetricsDTO.HostMetricDTO.builder()
                    .resource("cpu")
                    .value(hostMetrics.getCpuUsage())
                    .threshold(80) // Varsayılan değer
                    .timestamp(hostMetrics.getTimestamp())
                    .status(hostMetrics.getCpuUsage() > 80 ? "WARNING" : "OK")
                    .build());

            // RAM metriği
            hostMetricsDTO.add(MetricsDTO.HostMetricDTO.builder()
                    .resource("memory")
                    .value(hostMetrics.getMemoryUsage())
                    .threshold(90) // Varsayılan değer
                    .timestamp(hostMetrics.getTimestamp())
                    .status(hostMetrics.getMemoryUsage() > 90 ? "WARNING" : "OK")
                    .build());

            // Disk metriği
            hostMetricsDTO.add(MetricsDTO.HostMetricDTO.builder()
                    .resource("disk")
                    .value(hostMetrics.getDiskUsage())
                    .threshold(85) // Varsayılan değer
                    .timestamp(hostMetrics.getTimestamp())
                    .status(hostMetrics.getDiskUsage() > 85 ? "WARNING" : "OK")
                    .build());

            // Endpoint metriklerini burada ekleyeceğiz, şimdilik boş liste
            List<MetricsDTO.EndpointMetricDTO> endpointMetricsDTO = new ArrayList<>();

            // Metrics DTO oluştur
            MetricsDTO metricsDTO = MetricsDTO.builder()
                    .hostMetrics(hostMetricsDTO)
                    .endpointMetrics(endpointMetricsDTO)
                    .build();

            return ResponseEntity.ok(metricsDTO);
        } catch (Exception e) {
            log.error("Error getting metrics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Host metriklerini getirir.
     *
     * @return Host metrikleri
     */
    @Operation(summary = "Host metriklerini getir", description = "Sistem kaynak kullanımını gösteren host metriklerini getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Host metrikleri başarıyla alındı",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = HostMetrics.class))}),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası",
                    content = @Content)
    })
    @GetMapping("/metrics/host")
    public ResponseEntity<HostMetrics> getHostMetrics() {
        log.info("Getting host metrics");

        try {
            HostMetrics hostMetrics = hostMonitoringService.getHostMetrics();
            return ResponseEntity.ok(hostMetrics);
        } catch (Exception e) {
            log.error("Error getting host metrics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Metrik durumunu getirir.
     *
     * @return Metrik durumu
     */
    @Operation(summary = "Sistem durumunu getir", description = "Sistemin çalışma durumunu kontrol eder")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sistem durumu başarıyla alındı",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class, example = "UP"))}),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası",
                    content = @Content)
    })
    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("UP");
    }
}