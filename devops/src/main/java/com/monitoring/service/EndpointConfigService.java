package com.monitoring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitoring.dto.CreateEndpointConfigDTO;
import com.monitoring.dto.EndpointConfigDTO;
import com.monitoring.dto.UpdateEndpointConfigDTO;
import com.monitoring.enums.EndpointType;
import com.monitoring.model.EndpointConfigEntity;
import com.monitoring.model.MonitoringGroup;
import com.monitoring.model.MonitoringResult;
import com.monitoring.model.ServiceEntity;
import com.monitoring.repository.EndpointConfigRepository;
import com.monitoring.repository.MonitoringGroupRepository;
import com.monitoring.repository.MonitoringResultRepository;
import com.monitoring.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EndpointConfigService {

    private final EndpointConfigRepository endpointConfigRepository;
    private final ServiceRepository serviceRepository;
    private final MonitoringGroupRepository monitoringGroupRepository;
    private final MonitoringResultRepository monitoringResultRepository;
    private final ObjectMapper objectMapper;

    private final HttpEndpointMonitoringService httpMonitoringService;
    private final WebSocketEndpointMonitoringService webSocketMonitoringService;
    private final DatabaseEndpointMonitoringService databaseMonitoringService;

    @Transactional(readOnly = true)
    public List<EndpointConfigDTO> getAllEndpoints() {
        return endpointConfigRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EndpointConfigDTO> getEndpointsByType(EndpointType type) {
        return endpointConfigRepository.findByType(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<EndpointConfigDTO> getEndpointById(Long id) {
        return endpointConfigRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public EndpointConfigDTO createEndpoint(CreateEndpointConfigDTO dto) {
        EndpointConfigEntity entity = fillEntityFields(dto, new EndpointConfigEntity());
        entity = endpointConfigRepository.save(entity);
        log.info("Yeni endpoint oluşturuldu: {}", entity.getId());
        return convertToDTO(entity);
    }



    @Transactional
    public EndpointConfigDTO updateEndpoint(Long id, UpdateEndpointConfigDTO dto) {
        EndpointConfigEntity existingEntity = endpointConfigRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan endpoint bulunamadı"));

        // Mevcut entity'yi güncelle
        updateEntityFields(dto, existingEntity);

        EndpointConfigEntity updatedEntity = endpointConfigRepository.save(existingEntity);
        log.info("Endpoint güncellendi: {}", id);
        return convertToDTO(updatedEntity);
    }

    @Transactional
    public EndpointConfigDTO changeEndpointStatus(Long id, boolean enabled) {
        EndpointConfigEntity entity = endpointConfigRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan endpoint bulunamadı"));
        entity.setEnabled(enabled);
        entity = endpointConfigRepository.save(entity);
        log.info("Endpoint durumu güncellendi: {} -> {}", id, enabled);
        return convertToDTO(entity);
    }

    @Transactional
    public void deleteEndpoint(Long id) {
        if (!endpointConfigRepository.existsById(id)) {
            throw new EntityNotFoundException("ID: " + id + " olan endpoint bulunamadı");
        }
        endpointConfigRepository.deleteById(id);
        log.info("Endpoint silindi: {}", id);
    }

    @Transactional(readOnly = true)
    public List<Object> getEndpointResults(Long id, int limit) {
        EndpointConfigEntity entity = endpointConfigRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan endpoint bulunamadı"));

        return monitoringResultRepository.findByEndpointConfigOrderByTimestampDesc(entity, PageRequest.of(0, limit))
                .stream()
                .map(this::convertResultToDTO)
                .collect(Collectors.toList());
    }

    public Object testEndpoint(Long id) {
        EndpointConfigEntity entity = endpointConfigRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan endpoint bulunamadı"));

        MonitoringResult result;
        switch (entity.getType()) {
            case HTTP -> result = httpMonitoringService.monitorEndpoint(entity);
            case WEBSOCKET -> result = webSocketMonitoringService.monitorEndpoint(entity);
            case DATABASE -> result = databaseMonitoringService.monitorEndpoint(entity);
            default -> throw new UnsupportedOperationException("Desteklenmeyen endpoint türü: " + entity.getType());
        }

        return convertResultToDTO(result);
    }

    private EndpointConfigDTO convertToDTO(EndpointConfigEntity entity) {
        EndpointConfigDTO dto = new EndpointConfigDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setEnabled(entity.isEnabled());
        dto.setUrl(entity.getUrl());
        dto.setIntervalMs(entity.getIntervalMs());
        dto.setTimeoutMs(entity.getTimeoutMs());
        dto.setThresholdMs(entity.getThresholdMs());
        dto.setMethod(entity.getMethod());

        if (entity.getHeaders() != null && !entity.getHeaders().isEmpty()) {
            try {
                dto.setHeaders(objectMapper.readValue(entity.getHeaders(),
                        objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class)));
            } catch (JsonProcessingException e) {
                log.error("Header'ları dönüştürürken hata oluştu", e);
            }
        }

        dto.setBody(entity.getBody());
        dto.setExpectedStatus(entity.getExpectedStatus());
        dto.setExpectedResponseContains(entity.getExpectedResponseContains());
        dto.setProtocol(entity.getProtocol());
        dto.setTestMessage(entity.getTestMessage());
        dto.setExpectedResponse(entity.getExpectedResponse());
        dto.setDbType(entity.getDbType());
        dto.setDbUsername(entity.getDbUsername());
        dto.setDbPassword(entity.getDbPassword());
        dto.setQuery(entity.getQuery());

        if (entity.getService() != null) {
            dto.setServiceId(entity.getService().getId());
        }

        if (entity.getMonitoringGroups() != null && !entity.getMonitoringGroups().isEmpty()) {
            dto.setMonitoringGroupIds(entity.getMonitoringGroups().stream()
                    .map(MonitoringGroup::getId)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private EndpointConfigEntity fillEntityFields(CreateEndpointConfigDTO dto, EndpointConfigEntity entity) {
        entity.setType(dto.getType());
        entity.setEnabled(dto.isEnabled());
        entity.setUrl(dto.getUrl());
        entity.setMethod(dto.getMethod());
        entity.setBody(dto.getBody());
        entity.setExpectedStatus(dto.getExpectedStatus());
        entity.setExpectedResponseContains(dto.getExpectedResponseContains());
        entity.setProtocol(dto.getProtocol());
        entity.setTestMessage(dto.getTestMessage());
        entity.setExpectedResponse(dto.getExpectedResponse());
        entity.setDbType(dto.getDbType());
        entity.setDbUsername(dto.getDbUsername());
        entity.setDbPassword(dto.getDbPassword());
        entity.setQuery(dto.getQuery());
        entity.setIntervalMs(dto.getIntervalMs());
        entity.setTimeoutMs(dto.getTimeoutMs());
        entity.setThresholdMs(dto.getThresholdMs());

        if (dto.getHeaders() != null && !dto.getHeaders().isEmpty()) {
            try {
                entity.setHeaders(objectMapper.writeValueAsString(dto.getHeaders()));
            } catch (JsonProcessingException e) {
                log.error("Header'ları JSON'a çevirirken hata oluştu", e);
            }
        }

        if (dto.getServiceId() != null) {
            ServiceEntity service = serviceRepository.findById(dto.getServiceId())
                    .orElseThrow(() -> new EntityNotFoundException("ID: " + dto.getServiceId() + " olan servis bulunamadı"));
            entity.setService(service);
        }

        if (dto.getMonitoringGroupIds() != null && !dto.getMonitoringGroupIds().isEmpty()) {
            List<MonitoringGroup> groups = monitoringGroupRepository.findAllById(dto.getMonitoringGroupIds());
            entity.setMonitoringGroups(groups);
        }

        return entity;
    }


    private void updateEntityFields(UpdateEndpointConfigDTO dto, EndpointConfigEntity entity) {
        // Sadece güncellenebilir alanları set ediyoruz, ID'yi değiştirmiyoruz
        entity.setType(dto.getType());
        entity.setEnabled(dto.isEnabled());
        entity.setUrl(dto.getUrl());
        entity.setMethod(dto.getMethod());
        entity.setBody(dto.getBody());
        entity.setExpectedStatus(dto.getExpectedStatus());
        entity.setExpectedResponseContains(dto.getExpectedResponseContains());
        entity.setProtocol(dto.getProtocol());
        entity.setTestMessage(dto.getTestMessage());
        entity.setExpectedResponse(dto.getExpectedResponse());
        entity.setDbType(dto.getDbType());
        entity.setDbUsername(dto.getDbUsername());
        entity.setDbPassword(dto.getDbPassword());
        entity.setQuery(dto.getQuery());
        entity.setIntervalMs(dto.getIntervalMs());
        entity.setTimeoutMs(dto.getTimeoutMs());
        entity.setThresholdMs(dto.getThresholdMs());

        if (dto.getHeaders() != null) {
            try {
                entity.setHeaders(objectMapper.writeValueAsString(dto.getHeaders()));
            } catch (JsonProcessingException e) {
                log.error("Header'ları JSON'a çevirirken hata oluştu", e);
            }
        }

        if (dto.getServiceId() != null) {
            ServiceEntity service = serviceRepository.findById(dto.getServiceId())
                    .orElseThrow(() -> new EntityNotFoundException("ID: " + dto.getServiceId() + " olan servis bulunamadı"));
            entity.setService(service);
        }

        if (dto.getMonitoringGroupIds() != null) {
            List<MonitoringGroup> groups = monitoringGroupRepository.findAllById(dto.getMonitoringGroupIds());
            entity.setMonitoringGroups(groups);
        }
    }


    private Object convertResultToDTO(MonitoringResult result) {
        Map<String, Object> resultDTO = new HashMap<>();
        resultDTO.put("id", result.getId());
        resultDTO.put("timestamp", result.getTimestamp());
        resultDTO.put("type", result.getType());
        resultDTO.put("resourceId", result.getResourceId());
        resultDTO.put("success", result.isSuccess());
        resultDTO.put("responseTime", result.getResponseTime());
        resultDTO.put("status", result.getStatus());

        if (result.getData() != null) {
            resultDTO.put("data", result.getData());
        }

        if (result.getErrorMessage() != null) {
            resultDTO.put("errorMessage", result.getErrorMessage());
        }

        return resultDTO;
    }
}
