package com.monitoring.model;

import com.monitoring.enums.AlertLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Uyarı olaylarını temsil eden model sınıfı.
 */
@Data
@Builder
@Entity
@Table(name = "alert_event")
@NoArgsConstructor
@AllArgsConstructor
public class AlertEvent {

    /**
     * Olayın benzersiz tanımlayıcısı
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Olay zamanı
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * İzleme sonucu referansı
     */
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "monitoring_result_id")
    private MonitoringResult monitoringResult;

    /**
     * İlgili endpoint konfigürasyonu
     */
    @ManyToOne
    @JoinColumn(name = "endpoint_config_id")
    private EndpointConfigEntity endpointConfig;

    /**
     * Uyarı seviyesi
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private AlertLevel level;

    /**
     * Uyarı mesajı
     */
    @Column(name = "message", nullable = false)
    private String message;

    /**
     * Ek bilgiler (JSON string olarak saklanabilir)
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * Uyarının çözülüp çözülmediği
     */
    @Column(name = "resolved", nullable = false)
    private boolean resolved;

    /**
     * Çözülme zamanı
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}