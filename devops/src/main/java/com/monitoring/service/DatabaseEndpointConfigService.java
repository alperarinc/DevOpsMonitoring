package com.monitoring.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitoring.config.EndpointConfig;
import com.monitoring.enums.EndpointType; // Bu import ihtiyacı olabilir
import com.monitoring.model.EndpointConfigEntity;
import com.monitoring.repository.EndpointConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseEndpointConfigService {

    private final EndpointConfigRepository endpointConfigRepository;
    private final Environment environment;
    private final ObjectMapper objectMapper;

    public List<EndpointConfig.HttpConfig.HttpEndpoint> getHttpEndpoints() {
        return endpointConfigRepository.findByTypeAndEnabledTrue(EndpointType.HTTP).stream()
                .map(this::convertToHttpEndpoint)
                .collect(Collectors.toList());
    }

    public List<EndpointConfig.WebSocketConfig.WebSocketEndpoint> getWebSocketEndpoints() {
        return endpointConfigRepository.findByTypeAndEnabledTrue(EndpointType.WEBSOCKET).stream()
                .map(this::convertToWebSocketEndpoint)
                .collect(Collectors.toList());
    }

    public List<EndpointConfig.DatabaseConfig.DatabaseEndpoint> getDatabaseEndpoints() {
        return endpointConfigRepository.findByTypeAndEnabledTrue(EndpointType.DATABASE).stream()
                .map(this::convertToDatabaseEndpoint)
                .collect(Collectors.toList());
    }

    private EndpointConfig.HttpConfig.HttpEndpoint convertToHttpEndpoint(EndpointConfigEntity entity) {
        EndpointConfig.HttpConfig.HttpEndpoint endpoint = new EndpointConfig.HttpConfig.HttpEndpoint();
        endpoint.setId(entity.getId().toString()); // Long türünü String'e dönüştür
        endpoint.setEnabled(entity.isEnabled());
        endpoint.setUrl(resolveProperties(entity.getUrl()));
        endpoint.setMethod(entity.getMethod());
        if (entity.getHeaders() != null) {
            try {
                endpoint.setHeaders(objectMapper.readValue(entity.getHeaders(), new TypeReference<Map<String, String>>() {}));
            } catch (IOException e) {
                log.error("Error parsing headers for HTTP endpoint: {}", entity.getId(), e);
                endpoint.setHeaders(Map.of());
            }
        } else {
            endpoint.setHeaders(Map.of());
        }
        endpoint.setBody(entity.getBody());
        endpoint.setInterval(entity.getIntervalMs());
        endpoint.setTimeout(entity.getTimeoutMs());
        endpoint.setThreshold(entity.getThresholdMs());
        endpoint.setExpectedStatus(entity.getExpectedStatus());
        endpoint.setExpectedResponseContains(entity.getExpectedResponseContains());
        return endpoint;
    }

    private EndpointConfig.WebSocketConfig.WebSocketEndpoint convertToWebSocketEndpoint(EndpointConfigEntity entity) {
        EndpointConfig.WebSocketConfig.WebSocketEndpoint endpoint = new EndpointConfig.WebSocketConfig.WebSocketEndpoint();
        endpoint.setId(entity.getId().toString()); // Long türünü String'e dönüştür
        endpoint.setEnabled(entity.isEnabled());
        endpoint.setUrl(resolveProperties(entity.getUrl()));
        endpoint.setProtocol(entity.getProtocol());
        endpoint.setInterval(entity.getIntervalMs());
        endpoint.setTimeout(entity.getTimeoutMs());
        endpoint.setTestMessage(entity.getTestMessage());
        endpoint.setExpectedResponse(entity.getExpectedResponse());
        endpoint.setExpectedResponseContains(entity.getExpectedResponseContains());
        return endpoint;
    }

    private EndpointConfig.DatabaseConfig.DatabaseEndpoint convertToDatabaseEndpoint(EndpointConfigEntity entity) {
        EndpointConfig.DatabaseConfig.DatabaseEndpoint endpoint = new EndpointConfig.DatabaseConfig.DatabaseEndpoint();
        endpoint.setId(entity.getId().toString()); // Long türünü String'e dönüştür
        endpoint.setEnabled(entity.isEnabled());
        endpoint.setType(String.valueOf(entity.getDbType()));
        endpoint.setUrl(resolveProperties(entity.getUrl()));
        endpoint.setUsername(resolveProperties(entity.getDbUsername()));
        endpoint.setPassword(resolveProperties(entity.getDbPassword()));
        endpoint.setInterval(entity.getIntervalMs());
        endpoint.setQuery(entity.getQuery());
        endpoint.setThreshold(entity.getThresholdMs());
        return endpoint;
    }

    private String resolveProperties(String value) {
        if (value == null || !value.contains("${")) {
            return value;
        }
        String result = value;
        int startIndex = result.indexOf("${");
        while (startIndex != -1) {
            int endIndex = result.indexOf("}", startIndex);
            if (endIndex == -1) {
                break;
            }
            String placeholder = result.substring(startIndex + 2, endIndex);
            String[] parts = placeholder.split(":", 2);
            String propertyName = parts[0];
            String defaultValue = parts.length > 1 ? parts[1] : "";
            String propertyValue = environment.getProperty(propertyName);
            if (propertyValue == null) {
                propertyValue = defaultValue;
            }
            result = result.substring(0, startIndex) + propertyValue + result.substring(endIndex + 1);
            startIndex = result.indexOf("${");
        }
        return result;
    }
}