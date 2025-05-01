package com.monitoring.repository;

import com.monitoring.model.MonitoringGroup;
import com.monitoring.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    /**
     * Aktif olan servisleri getirir.
     *
     * @return Etkin servislerin listesi
     */
    List<ServiceEntity> findByEnabledTrue();

    /**
     * Belirli bir izleme grubuna ait servisleri getirir.
     *
     * @param ownerGroup Servis sahibi olan izleme grubu
     * @return Belirtilen gruba ait servislerin listesi
     */
    List<ServiceEntity> findByOwnerGroup(MonitoringGroup ownerGroup);

    /**
     * Belirli bir izleme grubuna ait aktif servisleri getirir.
     *
     * @param ownerGroup Servis sahibi olan izleme grubu
     * @return Belirtilen gruba ait etkin servislerin listesi
     */
    List<ServiceEntity> findByOwnerGroupAndEnabledTrue(MonitoringGroup ownerGroup);

    /**
     * Servis sahibi olmayan servisleri getirir (gruba atanmamış servisler).
     *
     * @return Sahibi olmayan servislerin listesi
     */
    List<ServiceEntity> findByOwnerGroupIsNull();
}