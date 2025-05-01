package com.monitoring.controller;

import com.monitoring.dto.CreateServiceDTO;
import com.monitoring.dto.ServiceDTO;
import com.monitoring.dto.UpdateServiceDTO;
import com.monitoring.service.ServiceEntityService;
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
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Services", description = "İzlenen servisleri yönetir")
public class ServiceController {

    private final ServiceEntityService serviceEntityService;

    @Operation(summary = "Tüm servisleri listele", description = "Sistemde tanımlı tüm servisleri getirir")
    @ApiResponse(responseCode = "200", description = "Servisler başarıyla listelendi",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceDTO.class))))
    @GetMapping
    public ResponseEntity<List<ServiceDTO>> getAllServices() {
        log.debug("REST isteği - tüm servisler getiriliyor");
        return ResponseEntity.ok(serviceEntityService.getAllServices());
    }

    @Operation(summary = "ID'ye göre servis getir", description = "Belirtilen ID'ye sahip servisi getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servis başarıyla bulundu", content = @Content(schema = @Schema(implementation = ServiceDTO.class))),
            @ApiResponse(responseCode = "404", description = "Servis bulunamadı")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable Long id) {
        log.debug("REST isteği - ID: {} olan servis getiriliyor", id);
        return serviceEntityService.getServiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Yeni servis oluştur",
            description = "Verilen bilgilerle yeni bir servis oluşturur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Servis başarıyla oluşturuldu",
                    content = @Content(schema = @Schema(implementation = ServiceDTO.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek")
    })
    @PostMapping
    public ResponseEntity<ServiceDTO> createService(@Valid @RequestBody CreateServiceDTO createServiceDTO) {
        log.debug("REST isteği - yeni servis oluşturuluyor: {}", createServiceDTO);
        return new ResponseEntity<>(serviceEntityService.createService(createServiceDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Servisi güncelle",
            description = "Belirtilen ID'ye sahip servisi günceller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servis başarıyla güncellendi",
                    content = @Content(schema = @Schema(implementation = ServiceDTO.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek"),
            @ApiResponse(responseCode = "404", description = "Servis bulunamadı")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ServiceDTO> updateService(@PathVariable Long id, @Valid @RequestBody UpdateServiceDTO updateServiceDTO) {
        log.debug("REST isteği - ID: {} olan servis güncelleniyor: {}", id, updateServiceDTO);
        return ResponseEntity.ok(serviceEntityService.updateService(id, updateServiceDTO));
    }

    @Operation(summary = "Servis durumunu değiştir",
            description = "Belirtilen ID'ye sahip servisin etkin/devre dışı durumunu değiştirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servis durumu başarıyla güncellendi",
                    content = @Content(schema = @Schema(implementation = ServiceDTO.class))),
            @ApiResponse(responseCode = "404", description = "Servis bulunamadı")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceDTO> changeServiceStatus(
            @Parameter(description = "Servis ID", required = true) @PathVariable Long id,
            @Parameter(description = "Servisin etkin olup olmadığı", required = true) @RequestParam boolean enabled) {
        log.debug("REST isteği - ID: {} olan servisin durumu güncelleniyor: {}", id, enabled);
        return ResponseEntity.ok(serviceEntityService.changeServiceStatus(id, enabled));
    }

    @Operation(summary = "Servis operasyon durumunu değiştir",
            description = "Belirtilen ID'ye sahip servisin çalışma durumunu değiştirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servis çalışma durumu başarıyla güncellendi",
                    content = @Content(schema = @Schema(implementation = ServiceDTO.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz durum değeri"),
            @ApiResponse(responseCode = "404", description = "Servis bulunamadı")
    })
    @PatchMapping("/{id}/operation-status")
    public ResponseEntity<ServiceDTO> changeServiceOperationStatus(
            @Parameter(description = "Servis ID", required = true) @PathVariable Long id,
            @Parameter(description = "Servisin çalışma durumu (OPERATIONAL, DEGRADED, OUTAGE, MAINTENANCE,  INACTIVE,  ACTIVE,  UNKNOWN)", required = true) @RequestParam String status) {
        log.debug("REST isteği - ID: {} olan servisin çalışma durumu güncelleniyor: {}", id, status);
        return ResponseEntity.ok(serviceEntityService.changeServiceOperationStatus(id, status));
    }

    @Operation(summary = "Servisi sil",
            description = "Belirtilen ID'ye sahip servisi sistemden kaldırır")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Servis başarıyla silindi"),
            @ApiResponse(responseCode = "404", description = "Servis bulunamadı")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(
            @Parameter(description = "Servis ID", required = true) @PathVariable Long id) {
        log.debug("REST isteği - ID: {} olan servis siliniyor", id);
        serviceEntityService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Servise ait endpoint'leri getir",
            description = "Belirtilen ID'ye sahip servisin tüm endpoint'lerini getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Endpoint'ler başarıyla listelendi"),
            @ApiResponse(responseCode = "404", description = "Servis bulunamadı")
    })
    @GetMapping("/{id}/endpoints")
    public ResponseEntity<List<Object>> getServiceEndpoints(
            @Parameter(description = "Servis ID", required = true) @PathVariable Long id) {
        log.debug("REST isteği - ID: {} olan servisin endpoint'leri getiriliyor", id);
        return ResponseEntity.ok(serviceEntityService.getServiceEndpoints(id));
    }

    @Operation(summary = "İzleme grubuna göre servisleri listele",
            description = "Belirtilen izleme grubuna ait tüm servisleri getirir")
    @ApiResponse(responseCode = "200", description = "Servisler başarıyla listelendi",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceDTO.class))))
    @GetMapping("/by-group/{groupId}")
    public ResponseEntity<List<ServiceDTO>> getServicesByGroup(
            @Parameter(description = "İzleme grubu ID", required = true) @PathVariable Long groupId) {
        log.debug("REST isteği - Grup ID: {} olan servisler getiriliyor", groupId);
        return ResponseEntity.ok(serviceEntityService.getServicesByGroup(groupId));
    }

    @Operation(summary = "Servisin izleme grubunu güncelle",
            description = "Belirtilen ID'ye sahip servisin bağlı olduğu izleme grubunu değiştirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servis grubu başarıyla güncellendi",
                    content = @Content(schema = @Schema(implementation = ServiceDTO.class))),
            @ApiResponse(responseCode = "404", description = "Servis veya grup bulunamadı")
    })
    @PatchMapping("/{id}/owner-group")
    public ResponseEntity<ServiceDTO> changeServiceOwnerGroup(
            @Parameter(description = "Servis ID", required = true) @PathVariable Long id,
            @Parameter(description = "İzleme grubu ID (null ise grup ilişkisi kaldırılır)", required = false) @RequestParam(required = false) Long groupId) {
        log.debug("REST isteği - ID: {} olan servisin grubu güncelleniyor: {}", id, groupId);
        return ResponseEntity.ok(serviceEntityService.changeServiceOwnerGroup(id, groupId));
    }

    @Operation(summary = "Gruba atanmamış servisleri listele",
            description = "Herhangi bir izleme grubuna atanmamış tüm servisleri getirir")
    @ApiResponse(responseCode = "200", description = "Servisler başarıyla listelendi",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceDTO.class))))
    @GetMapping("/unassigned")
    public ResponseEntity<List<ServiceDTO>> getUnassignedServices() {
        log.debug("REST isteği - Gruba atanmamış servisler getiriliyor");
        return ResponseEntity.ok(serviceEntityService.getUnassignedServices());
    }
}