package com.monitoring.repository;

import com.monitoring.enums.AlertLevel;
import com.monitoring.model.AlertEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AlertEventRepository extends JpaRepository<AlertEvent, Long> {

    // Temel listeleme sorgusu - sayfalama destekli
    Page<AlertEvent> findAll(Pageable pageable);

    // Çözülme durumuna göre filtreleme
    Page<AlertEvent> findByResolved(boolean resolved, Pageable pageable);

    // Uyarı seviyesine göre filtreleme
    Page<AlertEvent> findByLevel(AlertLevel level, Pageable pageable);

    // Uyarı seviyesi ve çözülme durumuna göre filtreleme
    Page<AlertEvent> findByLevelAndResolved(AlertLevel level, boolean resolved, Pageable pageable);

    // Zaman aralığına göre filtreleme
    Page<AlertEvent> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    // Endpoint ID'sine göre filtreleme
    Page<AlertEvent> findByEndpointConfig_Id(Long endpointId, Pageable pageable);
}