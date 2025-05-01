package com.monitoring.dto;

import com.monitoring.enums.ServiceType;
import com.monitoring.enums.ServicePriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mevcut servis güncelleme için veri transfer nesnesi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Mevcut servisi güncellemek için kullanılan DTO")
public class UpdateServiceDTO {

    @Schema(description = "Servis adı", example = "Payment Service")
    private String name;

    @Schema(description = "Servis açıklaması", example = "Ödeme işlemlerini yöneten servis")
    private String description;

    @Schema(description = "Servis türü")
    private ServiceType serviceType;


    @Schema(description = "Servisin sahip olduğu izleme grubu ID'si", example = "1")
    private Long ownerGroupId;

    @Email(message = "Geçerli bir e-posta adresi girilmelidir")
    @Schema(description = "İletişim e-posta adresi", example = "platform@example.com")
    private String contactEmail;

    @Schema(description = "Servisin etkin olup olmadığı")
    private Boolean enabled;

    @Schema(description = "Servis önceliği")
    private ServicePriority priority;

    @Schema(description = "Servis etiketleri (virgülle ayrılmış)", example = "payment,critical,core")
    private String tags;
}