package com.monitoring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * İzleme grubu için veri transfer nesnesi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "İzleme grubu bilgilerini taşır")
public class MonitoringGroupDTO {

    @Schema(description = "İzleme grubu ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Grup adı belirtilmelidir")
    @Schema(description = "İzleme grubu adı", example = "Kritik Servisler", required = true)
    private String name;

    @Schema(description = "İzleme grubu açıklaması", example = "Kritik önemdeki servislerin izleme grubu")
    private String description;

    @Schema(description = "Grubun etkin olup olmadığı", defaultValue = "true")
    private boolean enabled = true;

    @Email(message = "Geçerli bir e-posta adresi girilmelidir")
    @Schema(description = "Bildirim e-posta adresi", example = "alerts@example.com")
    private String notificationEmail;

    @Schema(description = "Grup içindeki endpoint ID'leri", accessMode = Schema.AccessMode.READ_ONLY)
    private List<Long> endpointIds;

    @Schema(description = "Grubun sahip olduğu servis ID'leri", accessMode = Schema.AccessMode.READ_ONLY)
    private List<Long> ownedServiceIds;

    @Schema(description = "Grubun sahip olduğu servis adları", accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> ownedServiceNames;
}