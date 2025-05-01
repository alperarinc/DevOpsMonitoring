package com.monitoring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Mevcut izleme grubunu güncellemek için veri transfer nesnesi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Mevcut izleme grubunu güncellemek için kullanılan DTO")
public class UpdateMonitoringGroupDTO {

    @Schema(description = "İzleme grubu adı", example = "Kritik Servisler")
    private String name;

    @Schema(description = "İzleme grubu açıklaması", example = "Kritik önemdeki servislerin izleme grubu")
    private String description;

    @Schema(description = "Grubun etkin olup olmadığı")
    private Boolean enabled;

    @Email(message = "Geçerli bir e-posta adresi girilmelidir")
    @Schema(description = "Bildirim e-posta adresi", example = "alerts@example.com")
    private String notificationEmail;

    @Schema(description = "Grup içindeki endpoint ID'leri")
    private List<Long> endpointIds;

}