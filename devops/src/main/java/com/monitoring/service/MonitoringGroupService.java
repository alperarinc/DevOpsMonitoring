package com.monitoring.service;

import com.monitoring.dto.CreateMonitoringGroupDTO;
import com.monitoring.dto.MonitoringGroupDTO;
import com.monitoring.dto.UpdateMonitoringGroupDTO;
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
 * İzleme grupları için servis sınıfı.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringGroupService {

    private final MonitoringGroupRepository monitoringGroupRepository;
    private final EndpointConfigRepository endpointConfigRepository;
    private final EndpointConfigService endpointConfigService;
    private final ServiceRepository serviceRepository;

    /**
     * Tüm izleme gruplarını getirir.
     *
     * @return İzleme gruplarının listesi
     */
    @Transactional(readOnly = true)
    public List<MonitoringGroupDTO> getAllGroups() {
        return monitoringGroupRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Belirtilen ID'ye sahip izleme grubunu getirir.
     *
     * @param id İzleme grubu ID'si
     * @return İzleme grubu DTO'su veya boş Optional
     */
    @Transactional(readOnly = true)
    public Optional<MonitoringGroupDTO> getGroupById(Long id) {
        return monitoringGroupRepository.findById(id)
                .map(this::convertToDTO);
    }


    /**
     * Yeni bir izleme grubu oluşturur.
     *
     * @param createGroupDTO Oluşturulacak grubun bilgileri
     * @return Oluşturulan izleme grubu
     */
    @Transactional
    public MonitoringGroupDTO createGroup(CreateMonitoringGroupDTO createGroupDTO) {
        MonitoringGroup group = convertCreateDtoToEntity(createGroupDTO);
        group = monitoringGroupRepository.save(group);

        // ID'yi bir final değişkene ata
        final Long groupId = group.getId();

        // Endpoint ilişkilerini grup ID oluştuktan sonra kur
        if (createGroupDTO.getEndpointIds() != null && !createGroupDTO.getEndpointIds().isEmpty()) {
            List<EndpointConfigEntity> endpoints = endpointConfigRepository.findAllById(createGroupDTO.getEndpointIds());

            for (EndpointConfigEntity endpoint : endpoints) {
                if (endpoint.getMonitoringGroups() == null) {
                    endpoint.setMonitoringGroups(new ArrayList<>());
                }

                // Lambda içinde final groupId değişkenini kullan
                if (endpoint.getMonitoringGroups().stream().noneMatch(g -> g.getId().equals(groupId))) {
                    endpoint.getMonitoringGroups().add(group);
                }
            }

            // İlişkileri kaydet
            endpointConfigRepository.saveAll(endpoints);
        }

        log.info("Yeni izleme grubu oluşturuldu: {}", groupId);
        return convertToDTO(group);
    }

    /**
     * Mevcut bir izleme grubunu günceller.
     *
     * @param id İzleme grubu ID'si
     * @param updateGroupDTO Güncellenecek grup bilgileri
     * @return Güncellenen izleme grubu
     */
    @Transactional
    public MonitoringGroupDTO updateGroup(Long id, UpdateMonitoringGroupDTO updateGroupDTO) {
        MonitoringGroup existingGroup = monitoringGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan izleme grubu bulunamadı"));

        // Sadece null olmayan alanları güncelle
        if (updateGroupDTO.getName() != null) {
            existingGroup.setName(updateGroupDTO.getName());
        }
        if (updateGroupDTO.getDescription() != null) {
            existingGroup.setDescription(updateGroupDTO.getDescription());
        }
        if (updateGroupDTO.getEnabled() != null) {
            existingGroup.setEnabled(updateGroupDTO.getEnabled());
        }
        if (updateGroupDTO.getNotificationEmail() != null) {
            existingGroup.setNotificationEmail(updateGroupDTO.getNotificationEmail());
        }

        // Endpoint listesini güncelle
        if (updateGroupDTO.getEndpointIds() != null) {
            List<EndpointConfigEntity> newEndpoints = endpointConfigRepository.findAllById(updateGroupDTO.getEndpointIds());

            // Mevcut endpoint ilişkilerini temizle
            if (existingGroup.getEndpoints() != null) {
                // Değiştirilemeyen bir kopya üzerinde döngü yap
                List<EndpointConfigEntity> currentEndpoints = new ArrayList<>(existingGroup.getEndpoints());
                for (EndpointConfigEntity endpoint : currentEndpoints) {
                    if (endpoint.getMonitoringGroups() != null) {
                        // final id'yi kullan
                        final Long groupId = id;
                        endpoint.getMonitoringGroups().removeIf(g -> g.getId().equals(groupId));
                    }
                }
            }

            existingGroup.setEndpoints(newEndpoints);

            // Yeni endpoint ilişkilerini kur
            for (EndpointConfigEntity endpoint : newEndpoints) {
                if (endpoint.getMonitoringGroups() == null) {
                    endpoint.setMonitoringGroups(new ArrayList<>());
                }

                // final id'yi kullan
                final Long groupId = id;
                if (endpoint.getMonitoringGroups().stream().noneMatch(g -> g.getId().equals(groupId))) {
                    endpoint.getMonitoringGroups().add(existingGroup);
                }
            }
        }

        existingGroup = monitoringGroupRepository.save(existingGroup);
        log.info("İzleme grubu güncellendi: {}", existingGroup.getId());
        return convertToDTO(existingGroup);
    }



    /**
     * İzleme grubunun etkin durumunu değiştirir.
     *
     * @param id İzleme grubu ID'si
     * @param enabled Yeni etkin durumu
     * @return Güncellenen izleme grubu
     */
    @Transactional
    public MonitoringGroupDTO changeGroupStatus(Long id, boolean enabled) {
        MonitoringGroup group = monitoringGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan izleme grubu bulunamadı"));

        group.setEnabled(enabled);
        group = monitoringGroupRepository.save(group);
        log.info("İzleme grubu durumu güncellendi: {} -> {}", id, enabled);
        return convertToDTO(group);
    }

    /**
     * İzleme grubunu siler.
     *
     * @param id İzleme grubu ID'si
     */
    @Transactional
    public void deleteGroup(Long id) {
        MonitoringGroup group = monitoringGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan izleme grubu bulunamadı"));

        // Grup silinmeden önce, bu gruba sahip olan servislerin ilişkisini kontrol et
        List<ServiceEntity> ownedServices = serviceRepository.findByOwnerGroup(group);
        if (ownedServices != null && !ownedServices.isEmpty()) {
            for (ServiceEntity service : ownedServices) {
                service.setOwnerGroup(null);
            }
            serviceRepository.saveAll(ownedServices);
            log.info("İzleme grubu silinirken {} servisin grup ilişkisi kaldırıldı", ownedServices.size());
        }

        monitoringGroupRepository.delete(group);
        log.info("İzleme grubu silindi: {}", id);
    }

    /**
     * İzleme grubuna ait endpoint'leri getirir.
     *
     * @param id İzleme grubu ID'si
     * @return Endpoint listesi
     */
    @Transactional(readOnly = true)
    public List<Object> getGroupEndpoints(Long id) {
        MonitoringGroup group = monitoringGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan izleme grubu bulunamadı"));

        if (group.getEndpoints() == null || group.getEndpoints().isEmpty()) {
            return Collections.emptyList();
        }

        return group.getEndpoints().stream()
                .map(endpoint -> endpointConfigService.getEndpointById(endpoint.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * İzleme grubunun sorumlu olduğu servisleri getirir.
     *
     * @param id İzleme grubu ID'si
     * @return Servis listesi
     */
    @Transactional(readOnly = true)
    public List<Object> getGroupServices(Long id) {
        MonitoringGroup group = monitoringGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan izleme grubu bulunamadı"));

        List<ServiceEntity> ownedServices = serviceRepository.findByOwnerGroup(group);

        if (ownedServices == null || ownedServices.isEmpty()) {
            return Collections.emptyList();
        }

        // ServiceEntityService sınıfına erişimimiz olmadığı için basit bir Map dönüyoruz
        // Gerçek implementasyonda ServiceEntityService inject edilerek DTO dönüşümü yapılabilir
        return ownedServices.stream()
                .map(service -> {
                    Map<String, Object> serviceMap = new HashMap<>();
                    serviceMap.put("id", service.getId());
                    serviceMap.put("name", service.getName());
                    serviceMap.put("serviceType", service.getServiceType());
                    serviceMap.put("status", service.getStatus());
                    serviceMap.put("enabled", service.isEnabled());
                    return serviceMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * İzleme grubuna endpoint'ler ekler.
     *
     * @param id İzleme grubu ID'si
     * @param endpointIds Eklenecek endpoint ID'leri
     * @return Güncellenen izleme grubu
     */
    @Transactional
    public MonitoringGroupDTO addEndpointsToGroup(Long id, List<Long> endpointIds) {
        MonitoringGroup group = monitoringGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan izleme grubu bulunamadı"));

        List<EndpointConfigEntity> endpoints = endpointConfigRepository.findAllById(endpointIds);

        for (EndpointConfigEntity endpoint : endpoints) {
            if (group.getEndpoints() == null) {
                group.setEndpoints(new ArrayList<>());
            }

            if (group.getEndpoints().stream().noneMatch(e -> e.getId().equals(endpoint.getId()))) {
                group.getEndpoints().add(endpoint);

                if (endpoint.getMonitoringGroups() == null) {
                    endpoint.setMonitoringGroups(new ArrayList<>());
                }

                if (endpoint.getMonitoringGroups().stream().noneMatch(g -> g.getId().equals(id))) {
                    endpoint.getMonitoringGroups().add(group);
                }
            }
        }

        group = monitoringGroupRepository.save(group);
        log.info("İzleme grubuna endpoint'ler eklendi: {}", id);
        return convertToDTO(group);
    }

    /**
     * İzleme grubundan bir endpoint'i çıkarır.
     *
     * @param id İzleme grubu ID'si
     * @param endpointId Çıkarılacak endpoint ID'si
     * @return Güncellenen izleme grubu
     */
    @Transactional
    public MonitoringGroupDTO removeEndpointFromGroup(Long id, Long endpointId) {
        MonitoringGroup group = monitoringGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + id + " olan izleme grubu bulunamadı"));

        EndpointConfigEntity endpoint = endpointConfigRepository.findById(endpointId)
                .orElseThrow(() -> new EntityNotFoundException("ID: " + endpointId + " olan endpoint bulunamadı"));

        if (group.getEndpoints() != null) {
            group.getEndpoints().removeIf(e -> e.getId().equals(endpointId));
        }

        if (endpoint.getMonitoringGroups() != null) {
            endpoint.getMonitoringGroups().removeIf(g -> g.getId().equals(id));
        }

        group = monitoringGroupRepository.save(group);
        log.info("İzleme grubundan endpoint çıkarıldı: {} -> {}", id, endpointId);
        return convertToDTO(group);
    }

    /**
     * MonitoringGroup entity'sini DTO'ya dönüştürür.
     *
     * @param entity Dönüştürülecek entity
     * @return MonitoringGroupDTO nesnesi
     */
    private MonitoringGroupDTO convertToDTO(MonitoringGroup entity) {
        MonitoringGroupDTO dto = new MonitoringGroupDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setEnabled(entity.isEnabled());
        dto.setNotificationEmail(entity.getNotificationEmail());

        if (entity.getEndpoints() != null && !entity.getEndpoints().isEmpty()) {
            dto.setEndpointIds(entity.getEndpoints().stream()
                    .map(EndpointConfigEntity::getId)
                    .collect(Collectors.toList()));
        }

        // Eklenen: Grubun sahip olduğu servisleri ekle
        if (entity.getOwnedServices() != null && !entity.getOwnedServices().isEmpty()) {
            dto.setOwnedServiceIds(entity.getOwnedServices().stream()
                    .map(ServiceEntity::getId)
                    .collect(Collectors.toList()));

            dto.setOwnedServiceNames(entity.getOwnedServices().stream()
                    .map(ServiceEntity::getName)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * CreateMonitoringGroupDTO'yu MonitoringGroup entity'sine dönüştürür.
     *
     * @param dto Dönüştürülecek DTO
     * @return MonitoringGroup entity'si
     */
    private MonitoringGroup convertCreateDtoToEntity(CreateMonitoringGroupDTO dto) {
        MonitoringGroup entity = new MonitoringGroup();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setEnabled(dto.isEnabled());
        entity.setNotificationEmail(dto.getNotificationEmail());

        if (dto.getEndpointIds() != null && !dto.getEndpointIds().isEmpty()) {
            List<EndpointConfigEntity> endpoints = endpointConfigRepository.findAllById(dto.getEndpointIds());
            entity.setEndpoints(endpoints);
            // Endpoint ilişkileri save sonrası kurulacak
        }

        return entity;
    }
}