package com.monitoring.dto;

import com.monitoring.enums.DatabaseType;
import com.monitoring.enums.EndpointType;
import com.monitoring.enums.HttpMethod;
import com.monitoring.enums.Protocol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Endpoint konfigürasyonu için veri transfer nesnesi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointConfigDTO {

    @Id
    @Schema(description = "Veritabanı tarafından otomatik oluşturulur", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;


    @NotNull(message = "Endpoint türü belirtilmelidir")
    private EndpointType type;

    private boolean enabled;

    @NotBlank(message = "URL belirtilmelidir")
    private String url;

    // HTTP endpoint için alanlar
    private HttpMethod method;
    private Map<String, String> headers;
    private String body;
    private Integer expectedStatus;
    private String expectedResponseContains;

    // WebSocket endpoint için alanlar
    private Protocol protocol;
    private String testMessage;
    private String expectedResponse;

    // Veritabanı endpoint için alanlar
    private DatabaseType dbType;
    private String dbUsername;
    private String dbPassword;
    private String query;

    // Ortak alanlar
    @Positive(message = "Interval değeri pozitif olmalıdır")
    private Long intervalMs;

    @Positive(message = "Timeout değeri pozitif olmalıdır")
    private Long timeoutMs;

    @Positive(message = "Threshold değeri pozitif olmalıdır")
    private Long thresholdMs;

    // İlişkiler
    private Long serviceId;
    private List<Long> monitoringGroupIds;
}