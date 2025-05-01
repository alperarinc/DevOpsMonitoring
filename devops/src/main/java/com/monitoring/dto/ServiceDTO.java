package com.monitoring.dto;

import com.monitoring.enums.ServiceType;
import com.monitoring.enums.ServiceStatus;
import com.monitoring.enums.ServicePriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "İzlenen servislerin bilgilerini taşır")
public class ServiceDTO {

    @Schema(description = "Servis ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Servis adı belirtilmelidir")
    @Schema(description = "Servis adı", example = "Payment Service")
    private String name;

    @Schema(description = "Servis açıklaması", example = "Ödeme işlemlerini yöneten servis")
    private String description;

    @NotNull(message = "Servis türü belirtilmelidir")
    @Schema(description = "Servis türü")
    private ServiceType serviceType;

    @Schema(description = "Servis durumu", accessMode = Schema.AccessMode.READ_ONLY)
    private ServiceStatus status;


    @Schema(description = "Servisin sahip olduğu izleme grubu ID'si")
    private Long ownerGroupId;

    @Schema(description = "Servisin sahip olduğu izleme grubu adı", accessMode = Schema.AccessMode.READ_ONLY)
    private String ownerGroupName;

    @Email(message = "Geçerli bir e-posta adresi girilmelidir")
    @Schema(description = "İletişim e-posta adresi", example = "platform@example.com")
    private String contactEmail;

    @Schema(description = "Servisin etkin olup olmadığı", defaultValue = "true")
    private boolean enabled;

    @Schema(description = "Servis önceliği")
    private ServicePriority priority;

    @Schema(description = "Servis etiketleri (virgülle ayrılmış)", example = "payment,critical,core")
    private String tags;

    @Schema(description = "Servis ile ilişkili endpoint'lerin ID listesi", accessMode = Schema.AccessMode.READ_ONLY)
    private List<Long> endpointIds;
}