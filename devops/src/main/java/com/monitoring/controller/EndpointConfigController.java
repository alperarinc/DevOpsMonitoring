package com.monitoring.controller;

import com.monitoring.dto.CreateEndpointConfigDTO;
import com.monitoring.dto.EndpointConfigDTO;
import com.monitoring.dto.UpdateEndpointConfigDTO;
import com.monitoring.enums.EndpointType;
import com.monitoring.service.EndpointConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Endpoints", description = "HTTP, WebSocket ve Database endpoint konfigürasyonlarını yönetir")
public class EndpointConfigController {

    private final EndpointConfigService endpointConfigService;

    @Operation(summary = "Tüm endpoint'leri listele", description = "Sistemde tanımlı tüm endpoint konfigürasyonlarını getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Endpoint'ler başarıyla listelendi",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EndpointConfigDTO.class)))})
    })
    @GetMapping
    public ResponseEntity<List<EndpointConfigDTO>> getAllEndpoints() {
        log.debug("REST isteği - tüm endpoint konfigürasyonları getiriliyor");
        return ResponseEntity.ok(endpointConfigService.getAllEndpoints());
    }

    @Operation(summary = "Belirli türdeki endpoint'leri listele",
            description = "Belirtilen türdeki (HTTP, WEBSOCKET, DATABASE) tüm endpoint konfigürasyonlarını getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Endpoint'ler başarıyla listelendi",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EndpointConfigDTO.class)))})
    })
    @GetMapping("/type/{type}")
    public ResponseEntity<List<EndpointConfigDTO>> getEndpointsByType(
            @Parameter(description = "Endpoint türü (HTTP, WEBSOCKET, DATABASE)", required = true)
            @PathVariable EndpointType type) {
        log.debug("REST isteği - {} türündeki endpoint konfigürasyonları getiriliyor", type);
        return ResponseEntity.ok(endpointConfigService.getEndpointsByType(type));
    }

    @Operation(summary = "ID'ye göre endpoint getir", description = "Belirtilen ID'ye sahip endpoint konfigürasyonunu getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Endpoint başarıyla bulundu",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = EndpointConfigDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Endpoint bulunamadı",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Geçersiz ID formatı",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EndpointConfigDTO> getEndpointById(
            @Parameter(description = "Endpoint ID'si", required = true)
            @PathVariable Long id) {
        log.debug("REST isteği - ID: {} olan endpoint konfigürasyonu getiriliyor", id);
        return endpointConfigService.getEndpointById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Yeni endpoint oluştur",
            description = "Verilen bilgilerle yeni bir endpoint konfigürasyonu oluşturur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Endpoint başarıyla oluşturuldu",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = EndpointConfigDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek parametreleri",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<EndpointConfigDTO> createEndpoint(
            @Parameter(description = "Endpoint konfigürasyon bilgileri", required = true)
            @Valid @RequestBody CreateEndpointConfigDTO endpointDTO) {
        log.debug("REST isteği - yeni endpoint konfigürasyonu oluşturuluyor: {}", endpointDTO);
        return new ResponseEntity<>(endpointConfigService.createEndpoint(endpointDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Endpoint'i güncelle", description = "Belirtilen ID'ye sahip endpoint konfigürasyonunu günceller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Endpoint başarıyla güncellendi",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = EndpointConfigDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Endpoint bulunamadı",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek parametreleri veya ID formatı",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EndpointConfigDTO> updateEndpoint(
            @Parameter(description = "Endpoint ID'si", required = true)
            @PathVariable Long id,
            @Parameter(description = "Güncellenmiş endpoint konfigürasyon bilgileri", required = true)
            @Valid @RequestBody UpdateEndpointConfigDTO endpointDTO) {
        log.debug("REST isteği - ID: {} olan endpoint güncelleniyor: {}", id, endpointDTO);
        return ResponseEntity.ok(endpointConfigService.updateEndpoint(id, endpointDTO));
    }

    @Operation(summary = "Endpoint durumunu değiştir",
            description = "Belirtilen ID'ye sahip endpoint'in etkin/devre dışı durumunu değiştirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Endpoint durumu başarıyla değiştirildi",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = EndpointConfigDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Endpoint bulunamadı",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Geçersiz ID formatı",
                    content = @Content)
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<EndpointConfigDTO> changeEndpointStatus(
            @Parameter(description = "Endpoint ID'si", required = true)
            @PathVariable Long id,
            @Parameter(description = "Durum (true=etkin, false=devre dışı)", required = true)
            @RequestParam boolean enabled) {
        log.debug("REST isteği - ID: {} olan endpoint'in durumu güncelleniyor: {}", id, enabled);
        return ResponseEntity.ok(endpointConfigService.changeEndpointStatus(id, enabled));
    }

    @Operation(summary = "Endpoint'i sil", description = "Belirtilen ID'ye sahip endpoint konfigürasyonunu siler")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Endpoint başarıyla silindi",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Endpoint bulunamadı",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Geçersiz ID formatı",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEndpoint(
            @Parameter(description = "Endpoint ID'si", required = true)
            @PathVariable Long id) {
        log.debug("REST isteği - ID: {} olan endpoint siliniyor", id);
        endpointConfigService.deleteEndpoint(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Endpoint sonuçlarını getir",
            description = "Belirtilen ID'ye sahip endpoint için son monitoring sonuçlarını getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monitoring sonuçları başarıyla listelendi",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Endpoint bulunamadı",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Geçersiz ID formatı",
                    content = @Content)
    })
    @GetMapping("/{id}/results")
    public ResponseEntity<List<Object>> getEndpointResults(
            @Parameter(description = "Endpoint ID'si", required = true)
            @PathVariable Long id,
            @Parameter(description = "En fazla kaç sonuç getirileceği", required = false)
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("REST isteği - ID: {} olan endpoint'in son {} sonucu getiriliyor", id, limit);
        return ResponseEntity.ok(endpointConfigService.getEndpointResults(id, limit));
    }

    @Operation(summary = "Endpoint'i test et",
            description = "Belirtilen ID'ye sahip endpoint için manuel test çalıştırır")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test başarıyla çalıştırıldı",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Endpoint bulunamadı",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Geçersiz ID formatı",
                    content = @Content)
    })
    @PostMapping("/{id}/test")
    public ResponseEntity<Object> testEndpoint(
            @Parameter(description = "Endpoint ID'si", required = true)
            @PathVariable Long id) {
        log.debug("REST isteği - ID: {} olan endpoint için manuel test çalıştırılıyor", id);
        return ResponseEntity.ok(endpointConfigService.testEndpoint(id));
    }
}