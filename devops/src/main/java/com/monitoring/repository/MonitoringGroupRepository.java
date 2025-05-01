package com.monitoring.repository;

import com.monitoring.model.MonitoringGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonitoringGroupRepository extends JpaRepository<MonitoringGroup, Long> {
    List<MonitoringGroup> findByEnabledTrue();
}
