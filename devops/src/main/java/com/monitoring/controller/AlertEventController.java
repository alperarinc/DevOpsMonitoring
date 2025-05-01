package com.monitoring.controller;

import com.monitoring.dto.AlertEventDTO;
import com.monitoring.dto.MonitoringApiResponse;
import com.monitoring.enums.AlertLevel;
import com.monitoring.mapper.AlertEventMapper;
import com.monitoring.model.AlertEvent;
import com.monitoring.service.AlertEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Uyarı olayları için REST API uç noktaları sağlayan controller sınıfı
 */
@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alert Events", description = "API uç noktaları ile uyarı olaylarını yönetme")
public class AlertEventController {

    private final AlertEventService alertEventService;
    private final AlertEventMapper alertEventMapper;

    @Autowired
    public AlertEventController(AlertEventService alertEventService, AlertEventMapper alertEventMapper) {
        this.alertEventService = alertEventService;
        this.alertEventMapper = alertEventMapper;
    }

    /**
     * Tüm uyarı olaylarını sayfalama ve filtreleme seçenekleriyle listeler
     */
    @Operation(
            summary = "Uyarı olaylarını listele",
            description = "Tüm uyarı olaylarını çeşitli filtrelere ve sayfalama seçeneklerine göre listeler"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Uyarı olayları başarıyla listelendi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MonitoringApiResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<MonitoringApiResponse<List<AlertEventDTO>>> getAllAlerts(
            @Parameter(description = "Uyarı seviyesine göre filtrele")
            @RequestParam(required = false) AlertLevel level,

            @Parameter(description = "Çözülme durumuna göre filtrele")
            @RequestParam(required = false) Boolean resolved,

            @Parameter(description = "Başlangıç zamanına göre filtrele", example = "2025-04-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,

            @Parameter(description = "Bitiş zamanına göre filtrele", example = "2025-04-28T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,

            @Parameter(description = "Endpoint ID'sine göre filtrele")
            @RequestParam(required = false) Long endpointId,

            @Parameter(description = "Sayfalama parametreleri")
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        // Güvenli sıralama sağlama
        Pageable safePageable = getSafePageable(pageable);

        Page<AlertEvent> alertEventPage;

        // Parametrelere göre uygun sorguyu çalıştır
        if (level != null && resolved != null) {
            alertEventPage = alertEventService.getAlertsByLevelAndResolvedStatus(level, resolved, safePageable);
        } else if (level != null) {
            alertEventPage = alertEventService.getAlertsByLevel(level, safePageable);
        } else if (resolved != null) {
            alertEventPage = alertEventService.getAlertsByResolvedStatus(resolved, safePageable);
        } else if (startTime != null && endTime != null) {
            alertEventPage = alertEventService.getAlertsByTimeRange(startTime, endTime, safePageable);
        } else if (endpointId != null) {
            alertEventPage = alertEventService.getAlertsByEndpointId(endpointId, safePageable);
        } else {
            alertEventPage = alertEventService.getAllAlerts(safePageable);
        }

        // Entity'leri DTO'lara dönüştür
        List<AlertEventDTO> dtoList = alertEventMapper.toDtoList(alertEventPage.getContent());
        Page<AlertEventDTO> dtoPage = new PageImpl<>(dtoList, safePageable, alertEventPage.getTotalElements());

        // Standart API yanıt formatında dön
        MonitoringApiResponse<List<AlertEventDTO>> response = MonitoringApiResponse.success("Alerts retrieved successfully", dtoPage);
        return ResponseEntity.ok(response);
    }

    /**
     * Güvenli bir Pageable nesnesi oluşturur - geçersiz sıralama alanlarını filtreler
     */
    private Pageable getSafePageable(Pageable pageable) {
        // Güvenli sıralama alanlarını tanımla
        List<String> safeProperties = Arrays.asList("timestamp", "level", "resolved", "id", "message", "endpointId");

        try {
            // Sıralama var mı kontrol et
            if (pageable.getSort().isEmpty()) {
                return pageable; // Sıralama yoksa olduğu gibi döndür
            }

            // Güvenli olmayan bir sıralama alanı var mı kontrol et
            boolean hasUnsafeProperty = StreamSupport.stream(pageable.getSort().spliterator(), false)
                    .map(Sort.Order::getProperty)
                    .anyMatch(prop -> !safeProperties.contains(prop));

            // Eğer güvenli olmayan bir sıralama varsa, güvenli bir sıralama ile değiştir
            if (hasUnsafeProperty) {
                return PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "timestamp")
                );
            }

            // Sıralama güvenli ise olduğu gibi döndür
            return pageable;
        } catch (Exception e) {
            // Herhangi bir hata durumunda varsayılan güvenli sıralamaya dön
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "timestamp")
            );
        }
    }

    /**
     * Belirli bir ID'ye sahip uyarı olayını getirir
     */
    @Operation(
            summary = "ID ile uyarı olayı getir",
            description = "Belirtilen ID'ye sahip uyarı olayını getirir"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Uyarı olayı başarıyla getirildi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MonitoringApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Uyarı olayı bulunamadı",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MonitoringApiResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<MonitoringApiResponse<AlertEventDTO>> getAlertById(
            @Parameter(description = "Uyarı olayının ID'si", required = true)
            @PathVariable Long id) {
        Optional<AlertEvent> alertOptional = alertEventService.getAlertById(id);

        if (alertOptional.isPresent()) {
            AlertEventDTO dto = alertEventMapper.toDto(alertOptional.get());
            return ResponseEntity.ok(MonitoringApiResponse.success("Alert retrieved successfully", dto));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(MonitoringApiResponse.error("Alert not found with id: " + id));
        }
    }

    /**
     * Bir uyarıyı çözüldü olarak işaretler
     */
    @Operation(
            summary = "Uyarıyı çözüldü olarak işaretle",
            description = "Belirtilen ID'ye sahip uyarı olayını çözüldü olarak işaretler"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Uyarı başarıyla çözüldü olarak işaretlendi",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MonitoringApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Uyarı olayı bulunamadı",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MonitoringApiResponse.class))
            )
    })
    @PutMapping("/{id}/resolve")
    public ResponseEntity<MonitoringApiResponse<AlertEventDTO>> markAsResolved(
            @Parameter(description = "Çözülecek uyarı olayının ID'si", required = true)
            @PathVariable Long id) {
        try {
            AlertEvent resolvedAlert = alertEventService.markAsResolved(id);
            AlertEventDTO dto = alertEventMapper.toDto(resolvedAlert);
            return ResponseEntity.ok(MonitoringApiResponse.success("Alert marked as resolved", dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(MonitoringApiResponse.error("Alert not found with id: " + id));
        }
    }
}