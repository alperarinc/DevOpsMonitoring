package com.monitoring.mapper;

import com.monitoring.dto.AlertEventDTO;
import com.monitoring.model.AlertEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AlertEvent ve AlertEventDTO arasında dönüşüm sağlayan sınıf
 */
@Component
public class AlertEventMapper {

    /**
     * Bir AlertEvent varlığını AlertEventDTO'ya dönüştürür
     */
    public AlertEventDTO toDto(AlertEvent alertEvent) {
        if (alertEvent == null) {
            return null;
        }

        return AlertEventDTO.builder()
                .id(alertEvent.getId())
                .timestamp(alertEvent.getTimestamp())
                .monitoringResultId(alertEvent.getMonitoringResult() != null ?
                        alertEvent.getMonitoringResult().getId() : null)
                .monitoringResultSummary(alertEvent.getMonitoringResult() != null ?
                        generateMonitoringSummary(alertEvent) : null)
                .endpointId(alertEvent.getEndpointConfig() != null ?
                        alertEvent.getEndpointConfig().getId() : null)
                .endpointUrl(alertEvent.getEndpointConfig() != null ?
                        alertEvent.getEndpointConfig().getUrl() : null)
                .level(alertEvent.getLevel())
                .message(alertEvent.getMessage())
                .details(alertEvent.getDetails())
                .resolved(alertEvent.isResolved())
                .resolvedAt(alertEvent.getResolvedAt())
                .build();
    }

    /**
     * AlertEvent listesini AlertEventDTO listesine dönüştürür
     */
    public List<AlertEventDTO> toDtoList(List<AlertEvent> alertEvents) {
        if (alertEvents == null) {
            return null;
        }

        return alertEvents.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * İzleme sonucu için özet bilgi oluşturur
     */
    private String generateMonitoringSummary(AlertEvent alertEvent) {
        if (alertEvent.getMonitoringResult() == null) {
            return null;
        }

        // Bu kısmı MonitoringResult sınıfınızın yapısına göre özelleştirebilirsiniz
        return String.format("Status: %s, Response Time: %d ms",
                alertEvent.getMonitoringResult().getStatus(),  // Status için bir String değer
                alertEvent.getMonitoringResult().getResponseTime());  // ResponseTime için sayısal değer
    }
}