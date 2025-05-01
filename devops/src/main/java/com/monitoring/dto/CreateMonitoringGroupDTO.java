package com.monitoring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Yeni izleme grubu oluşturmak için veri transfer nesnesi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeni izleme grubu oluşturmak için kullanılan DTO")
public class CreateMonitoringGroupDTO {

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

    @Schema(description = "Grup içindeki endpoint ID'leri")
    private List<Long> endpointIds;
}