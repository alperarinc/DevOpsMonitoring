package com.monitoring.repository;

import com.monitoring.model.MonitoringResult;
import com.monitoring.model.EndpointConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.PageRequest;
import java.util.List;

public interface MonitoringResultRepository extends JpaRepository<MonitoringResult, Long> {
    List<MonitoringResult> findByEndpointConfigOrderByTimestampDesc(EndpointConfigEntity endpoint, PageRequest page);
}