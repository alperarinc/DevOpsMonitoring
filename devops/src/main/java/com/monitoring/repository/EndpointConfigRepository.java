package com.monitoring.repository;

import com.monitoring.enums.EndpointType;
import com.monitoring.model.EndpointConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EndpointConfigRepository extends JpaRepository<EndpointConfigEntity, Long> {
    List<EndpointConfigEntity> findByType(EndpointType type);
    List<EndpointConfigEntity> findByTypeAndEnabledTrue(EndpointType type);
    List<EndpointConfigEntity> findByServiceId(Long serviceId);
}

