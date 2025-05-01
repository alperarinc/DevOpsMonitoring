package com.monitoring.dto;

import com.monitoring.enums.DatabaseType;
import com.monitoring.enums.EndpointType;
import com.monitoring.enums.HttpMethod;
import com.monitoring.enums.Protocol;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEndpointConfigDTO {

    @NotNull(message = "Endpoint türü belirtilmelidir")
    private EndpointType type;

    private boolean enabled;

    private String url;
    private HttpMethod method;
    private Map<String, String> headers;
    private String body;
    private Integer expectedStatus;
    private String expectedResponseContains;

    private Protocol protocol;
    private String testMessage;
    private String expectedResponse;

    private DatabaseType dbType;
    private String dbUsername;
    private String dbPassword;
    private String query;

    @Positive(message = "Interval değeri pozitif olmalıdır")
    private Long intervalMs;

    @Positive(message = "Timeout değeri pozitif olmalıdır")
    private Long timeoutMs;

    @Positive(message = "Threshold değeri pozitif olmalıdır")
    private Long thresholdMs;

    private Long serviceId;
    private List<Long> monitoringGroupIds;

    // 🧠 Dinamik Validasyonlar (type'a göre kontrol)

    @AssertTrue(message = "HTTP türü için url, method ve expectedStatus alanları zorunludur.")
    public boolean isValidHttp() {
        if (type != EndpointType.HTTP) return true;
        return url != null && method != null && expectedStatus != null;
    }

    @AssertTrue(message = "DATABASE türü için dbType, dbUsername, dbPassword ve query alanları zorunludur.")
    public boolean isValidDatabase() {
        if (type != EndpointType.DATABASE) return true;
        return dbType != null && dbUsername != null && dbPassword != null && query != null;
    }

    @AssertTrue(message = "WEBSOCKET türü için protocol, testMessage ve expectedResponse alanları zorunludur.")
    public boolean isValidWebSocket() {
        if (type != EndpointType.WEBSOCKET) return true;
        return protocol != null && testMessage != null && expectedResponse != null;
    }
}
