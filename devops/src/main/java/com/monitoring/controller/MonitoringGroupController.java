package com.monitoring.controller;

import com.monitoring.dto.CreateMonitoringGroupDTO;
import com.monitoring.dto.MonitoringGroupDTO;
import com.monitoring.dto.ServiceDTO;
import com.monitoring.dto.UpdateMonitoringGroupDTO;
import com.monitoring.service.MonitoringGroupService;
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

/**
 * İzleme grupları için REST controller.
 */
@RestController
@RequestMapping("/api/monitoring-groups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Monitoring Groups", description = "İzleme gruplarını yönetir")
public class MonitoringGroupController {

    private final MonitoringGroupService monitoringGroupService;

    @Operation(summary = "Tüm izleme gruplarını getir", description = "Sistemdeki tüm izleme gruplarını döner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Başarılı",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MonitoringGroupDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<MonitoringGroupDTO>> getAllGroups() {
        log.debug("REST isteği - tüm izleme grupları getiriliyor");
        return ResponseEntity.ok(monitoringGroupService.getAllGroups());
    }

    @Operation(summary = "ID ile izleme grubunu getir", description = "Verilen ID'ye sahip izleme grubunu döner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "İzleme grubu başarıyla bulundu",
                    content = @Content(schema = @Schema(implementation = MonitoringGroupDTO.class))),
            @ApiResponse(responseCode = "404", description = "İzleme grubu bulunamadı")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MonitoringGroupDTO> getGroupById(
            @Parameter(description = "İzleme grubu ID", required = true) @PathVariable Long id) {
        log.debug("REST isteği - ID: {} olan izleme grubu getiriliyor", id);
        return monitoringGroupService.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Yeni izleme grubu oluştur", description = "Verilen bilgilerle yeni bir izleme grubu oluşturur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "İzleme grubu başarıyla oluşturuldu",
                    content = @Content(schema = @Schema(implementation = MonitoringGroupDTO.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek")
    })
    @PostMapping
    public ResponseEntity<MonitoringGroupDTO> createGroup(
            @Valid @RequestBody CreateMonitoringGroupDTO createGroupDTO) {
        log.debug("REST isteği - yeni izleme grubu oluşturuluyor: {}", createGroupDTO);
        return new ResponseEntity<>(monitoringGroupService.createGroup(createGroupDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "İzleme grubunu güncelle", description = "Verilen ID'ye sahip izleme grubunu günceller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "İzleme grubu başarıyla güncellendi",
                    content = @Content(schema = @Schema(implementation = MonitoringGroupDTO.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek"),
            @ApiResponse(responseCode = "404", description = "İzleme grubu bulunamadı")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MonitoringGroupDTO> updateGroup(
            @Parameter(description = "İzleme grubu ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateMonitoringGroupDTO updateGroupDTO) {
        log.debug("REST isteği - ID: {} olan izleme grubu güncelleniyor: {}", id, updateGroupDTO);
        return ResponseEntity.ok(monitoringGroupService.updateGroup(id, updateGroupDTO));
    }

    @Operation(summary = "Grup durumunu güncelle", description = "İzleme grubunun etkin olup olmadığını değiştirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "İzleme grubu durumu başarıyla güncellendi",
                    content = @Content(schema = @Schema(implementation = MonitoringGroupDTO.class))),
            @ApiResponse(responseCode = "404", description = "İzleme grubu bulunamadı")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<MonitoringGroupDTO> changeGroupStatus(
            @Parameter(description = "İzleme grubu ID", required = true) @PathVariable Long id,
            @Parameter(description = "Etkin durumu", required = true) @RequestParam boolean enabled) {
        log.debug("REST isteği - ID: {} olan izleme grubunun durumu güncelleniyor: {}", id, enabled);
        return ResponseEntity.ok(monitoringGroupService.changeGroupStatus(id, enabled));
    }

    @Operation(summary = "İzleme grubunu sil", description = "Verilen ID'ye sahip izleme grubunu siler")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "İzleme grubu başarıyla silindi"),
            @ApiResponse(responseCode = "404", description = "İzleme grubu bulunamadı")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(
            @Parameter(description = "İzleme grubu ID", required = true) @PathVariable Long id) {
        log.debug("REST isteği - ID: {} olan izleme grubu siliniyor", id);
        monitoringGroupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Gruba ait endpoint'leri getir", description = "İzleme grubuna ait tüm endpoint'leri döner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Endpoint'ler başarıyla listelendi"),
            @ApiResponse(responseCode = "404", description = "İzleme grubu bulunamadı")
    })
    @GetMapping("/{id}/endpoints")
    public ResponseEntity<List<Object>> getGroupEndpoints(
            @Parameter(description = "İzleme grubu ID", required = true) @PathVariable Long id) {
        log.debug("REST isteği - ID: {} olan izleme grubunun endpoint'leri getiriliyor", id);
        return ResponseEntity.ok(monitoringGroupService.getGroupEndpoints(id));
    }

    @Operation(summary = "Gruba ait servisleri getir", description = "İzleme grubunun sorumlu olduğu tüm servisleri döner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servisler başarıyla listelendi",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceDTO.class)))),
            @ApiResponse(responseCode = "404", description = "İzleme grubu bulunamadı")
    })
    @GetMapping("/{id}/services")
    public ResponseEntity<List<Object>> getGroupServices(
            @Parameter(description = "İzleme grubu ID", required = true) @PathVariable Long id) {
        log.debug("REST isteği - ID: {} olan izleme grubunun servisleri getiriliyor", id);
        return ResponseEntity.ok(monitoringGroupService.getGroupServices(id));
    }

    @Operation(summary = "Gruba endpoint ekle", description = "İzleme grubuna birden fazla endpoint ekler")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Endpoint'ler başarıyla eklendi",
                    content = @Content(schema = @Schema(implementation = MonitoringGroupDTO.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek"),
            @ApiResponse(responseCode = "404", description = "İzleme grubu veya endpoint bulunamadı")
    })
    @PostMapping("/{id}/endpoints")
    public ResponseEntity<MonitoringGroupDTO> addEndpointsToGroup(
            @Parameter(description = "İzleme grubu ID", required = true) @PathVariable Long id,
            @Parameter(description = "Eklenecek endpoint ID'leri", required = true) @RequestBody List<Long> endpointIds) {
        log.debug("REST isteği - ID: {} olan izleme grubuna endpoint'ler ekleniyor: {}", id, endpointIds);
        return ResponseEntity.ok(monitoringGroupService.addEndpointsToGroup(id, endpointIds));
    }

    @Operation(summary = "Gruptan endpoint çıkar", description = "İzleme grubundan bir endpoint'i çıkarır")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Endpoint başarıyla çıkarıldı",
                    content = @Content(schema = @Schema(implementation = MonitoringGroupDTO.class))),
            @ApiResponse(responseCode = "404", description = "İzleme grubu veya endpoint bulunamadı")
    })
    @DeleteMapping("/{id}/endpoints/{endpointId}")
    public ResponseEntity<MonitoringGroupDTO> removeEndpointFromGroup(
            @Parameter(description = "İzleme grubu ID", required = true) @PathVariable Long id,
            @Parameter(description = "Çıkarılacak endpoint ID", required = true) @PathVariable Long endpointId) {
        log.debug("REST isteği - ID: {} olan izleme grubundan endpoint çıkarılıyor: {}", id, endpointId);
        return ResponseEntity.ok(monitoringGroupService.removeEndpointFromGroup(id, endpointId));
    }
}