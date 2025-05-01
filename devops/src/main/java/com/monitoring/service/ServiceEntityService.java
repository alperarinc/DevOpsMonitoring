package com.monitoring.service;

import com.monitoring.dto.CreateServiceDTO;
import com.monitoring.dto.ServiceDTO;
import com.monitoring.dto.UpdateServiceDTO;
import com.monitoring.enums.ServiceStatus;
import com.monitoring.model.EndpointConfigEntity;
import com.monitoring.model.MonitoringGroup;
import com.monitoring.model.ServiceEntity;
import com.monitoring.repository.EndpointConfigRepository;
import com.monitoring.repository.MonitoringGroupRepository;
import com.monitoring.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servis entity'leri için servis sınıfı.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceEntityService {

    private final ServiceRepository serviceRepository;
    private final EndpointConfigRepository endpointConfigRepository;
    private final EndpointConfigService endpointConfigService;
    private final MonitoringGroupRepository monitoringGroupRepository;

    @Transactional(readOnly = true)
    public List<ServiceDTO> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ServiceDTO> getServiceById(Long id) {
        return serviceRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public ServiceDTO createService(CreateServiceDTO createServiceDTO) {
        ServiceEntity service = convertCreateDtoToEntity(createServiceDTO);
        service.setStatus(ServiceStatus.INACTIVE); // Yeni servisler varsayılan olarak INACTIVE olarak ayarlanır
        service = serviceRepository.save(service);
        log.info("Yeni servis oluşturuldu: {}", service.getId());
        return convertToDTO(service);
    }

    @Transactional
    public ServiceDTO updateService(Long id, UpdateServiceDTO updateServiceDTO) {
        ServiceEntity existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan servis bulunamadı"));

        // Sadece null olmayan alanları güncelle
        if (updateServiceDTO.getName() != null) {
            existingService.setName(updateServiceDTO.getName());
        }
        if (updateServiceDTO.getDescription() != null) {
            existingService.setDescription(updateServiceDTO.getDescription());
        }
        if (updateServiceDTO.getServiceType() != null) {
            existingService.setServiceType(updateServiceDTO.getServiceType());
        }
        // Owner yerine ownerGroup güncelleniyor
        if (updateServiceDTO.getOwnerGroupId() != null) {
            MonitoringGroup ownerGroup = monitoringGroupRepository.findById(updateServiceDTO.getOwnerGroupId())
                    .orElseThrow(() -> new EntityNotFoundException("ID: " + updateServiceDTO.getOwnerGroupId() + " olan izleme grubu bulunamadı"));
            existingService.setOwnerGroup(ownerGroup);
        }
        if (updateServiceDTO.getContactEmail() != null) {
            existingService.setContactEmail(updateServiceDTO.getContactEmail());
        }
        if (updateServiceDTO.getEnabled() != null) {
            existingService.setEnabled(updateServiceDTO.getEnabled());
        }
        if (updateServiceDTO.getPriority() != null) {
            existingService.setPriority(updateServiceDTO.getPriority());
        }
        if (updateServiceDTO.getTags() != null) {
            existingService.setTags(updateServiceDTO.getTags());
        }

        existingService = serviceRepository.save(existingService);
        log.info("Servis güncellendi: {}", existingService.getId());
        return convertToDTO(existingService);
    }

    @Transactional
    public ServiceDTO changeServiceStatus(Long id, boolean enabled) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan servis bulunamadı"));

        service.setEnabled(enabled);
        service = serviceRepository.save(service);
        log.info("Servis durumu güncellendi: {} -> {}", id, enabled);
        return convertToDTO(service);
    }

    @Transactional
    public ServiceDTO changeServiceOperationStatus(Long id, String status) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan servis bulunamadı"));

        try {
            ServiceStatus newStatus = ServiceStatus.valueOf(status.toUpperCase());
            service.setStatus(newStatus);
            service = serviceRepository.save(service);
            log.info("Servis çalışma durumu güncellendi: {} -> {}", id, newStatus);
            return convertToDTO(service);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Geçersiz servis durumu: " + status);
        }
    }

    @Transactional
    public void deleteService(Long id) {
        if (!serviceRepository.existsById(id)) {
            throw new EntityNotFoundException("ID: " + id + " olan servis bulunamadı");
        }

        serviceRepository.deleteById(id);
        log.info("Servis silindi: {}", id);
    }

    @Transactional(readOnly = true)
    public List<Object> getServiceEndpoints(Long id) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan servis bulunamadı"));

        if (service.getEndpoints() == null || service.getEndpoints().isEmpty()) {
            return Collections.emptyList();
        }

        return service.getEndpoints().stream()
                .map(endpoint -> endpointConfigService.getEndpointById(endpoint.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Belirtilen izleme grubuna ait tüm servisleri getirir.
     *
     * @param groupId İzleme grubu ID'si
     * @return Servislerin DTO listesi
     */
    @Transactional(readOnly = true)
    public List<ServiceDTO> getServicesByGroup(Long groupId) {
        MonitoringGroup group = monitoringGroupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + groupId + " olan izleme grubu bulunamadı"));

        List<ServiceEntity> services = serviceRepository.findByOwnerGroup(group);
        return services.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Bir servisin bağlı olduğu izleme grubunu değiştirir.
     *
     * @param id Servis ID'si
     * @param groupId İzleme grubu ID'si (null ise grup ilişkisi kaldırılır)
     * @return Güncellenen servis DTO'su
     */
    @Transactional
    public ServiceDTO changeServiceOwnerGroup(Long id, Long groupId) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan servis bulunamadı"));

        if (groupId == null) {
            // Grup ilişkisini kaldır
            service.setOwnerGroup(null);
            log.info("Servisin grup ilişkisi kaldırıldı: {}", id);
        } else {
            // Yeni grup ilişkisi kur
            MonitoringGroup group = monitoringGroupRepository.findById(groupId)
                    .orElseThrow(() -> new EntityNotFoundException("ID: " + groupId + " olan izleme grubu bulunamadı"));

            service.setOwnerGroup(group);
            log.info("Servisin grubu güncellendi: {} -> {}", id, groupId);
        }

        service = serviceRepository.save(service);
        return convertToDTO(service);
    }

    /**
     * Herhangi bir izleme grubuna atanmamış tüm servisleri getirir.
     *
     * @return Servislerin DTO listesi
     */
    @Transactional(readOnly = true)
    public List<ServiceDTO> getUnassignedServices() {
        List<ServiceEntity> services = serviceRepository.findByOwnerGroupIsNull();
        return services.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ServiceDTO convertToDTO(ServiceEntity entity) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setServiceType(entity.getServiceType());
        dto.setStatus(entity.getStatus());

        // Owner yerine ownerGroup bilgileri kullanılıyor
        if (entity.getOwnerGroup() != null) {
            dto.setOwnerGroupId(entity.getOwnerGroup().getId());
            dto.setOwnerGroupName(entity.getOwnerGroup().getName());
        }

        dto.setContactEmail(entity.getContactEmail());
        dto.setEnabled(entity.isEnabled());
        dto.setPriority(entity.getPriority());
        dto.setTags(entity.getTags());

        if (entity.getEndpoints() != null && !entity.getEndpoints().isEmpty()) {
            dto.setEndpointIds(
                    entity.getEndpoints().stream()
                            .map(EndpointConfigEntity::getId)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    /**
     * CreateServiceDTO nesnesini ServiceEntity nesnesine dönüştürür.
     *
     * @param dto Dönüştürülecek CreateServiceDTO nesnesi
     * @return Dönüştürülmüş ServiceEntity nesnesi
     */
    private ServiceEntity convertCreateDtoToEntity(CreateServiceDTO dto) {
        ServiceEntity entity = new ServiceEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setServiceType(dto.getServiceType());

        // Owner yerine ownerGroup kullanılıyor
        if (dto.getOwnerGroupId() != null) {
            MonitoringGroup ownerGroup = monitoringGroupRepository.findById(dto.getOwnerGroupId())
                    .orElseThrow(() -> new EntityNotFoundException("ID: " + dto.getOwnerGroupId() + " olan izleme grubu bulunamadı"));
            entity.setOwnerGroup(ownerGroup);
        }

        entity.setContactEmail(dto.getContactEmail());
        entity.setEnabled(dto.isEnabled());
        entity.setPriority(dto.getPriority());
        entity.setTags(dto.getTags());

        return entity;
    }
}