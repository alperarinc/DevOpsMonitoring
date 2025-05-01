package com.monitoring.dto;

import com.monitoring.enums.AlertLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Uyarı olaylarının API üzerinden iletilmesi için DTO sınıfı
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Uyarı olayı bilgilerini içeren veri transfer nesnesi")
public class AlertEventDTO {

    @Schema(description = "Uyarı olayının benzersiz tanımlayıcısı")
    private Long id;

    @Schema(description = "Uyarı olayının gerçekleştiği zaman")
    private LocalDateTime timestamp;

    @Schema(description = "İlişkili izleme sonucunun ID'si")
    private Long monitoringResultId;

    @Schema(description = "İzleme sonucu özeti")
    private String monitoringResultSummary;

    @Schema(description = "İlişkili endpoint'in ID'si")
    private Long endpointId;

    @Schema(description = "İlişkili endpoint'in adı")
    private String endpointName;

    @Schema(description = "İlişkili endpoint'in URL'i")
    private String endpointUrl;

    @Schema(description = "Uyarının önem seviyesi")
    private AlertLevel level;

    @Schema(description = "Uyarı mesajı")
    private String message;

    @Schema(description = "Uyarı ile ilgili ek detaylar")
    private String details;

    @Schema(description = "Uyarının çözülme durumu")
    private boolean resolved;

    @Schema(description = "Uyarının çözüldüğü zaman")
    private LocalDateTime resolvedAt;
}