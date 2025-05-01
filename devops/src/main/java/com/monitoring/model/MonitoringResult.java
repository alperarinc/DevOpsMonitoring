package com.monitoring.model;

import com.monitoring.enums.MonitoringType;
import com.monitoring.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * İzleme sonuçlarını temsil eden model sınıfı.
 */
@Entity
@Table(name = "monitoring_result")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringResult {

    /**
     * Sonucun benzersiz tanımlayıcısı
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * İzleme zamanı
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * İzleme türü (HOST, HTTP, WEBSOCKET, DATABASE)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MonitoringType type;

    /**
     * İzlenen kaynağın tanımlayıcısı
     */
    @Column(name = "resource_id")
    private String resourceId;

    /**
     * İzleme sonucu başarılı mı?
     */
    @Column(name = "success", nullable = false)
    private boolean success;

    /**
     * Yanıt süresi (milisaniye)
     */
    @Column(name = "response_time")
    private long responseTime;

    /**
     * Hata durumunda hata mesajı
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * İzleme sonucunun durumu
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    /**
     * İzleme verileri (JSON string olarak saklanabilir)
     */
    @Column(name = "data", columnDefinition = "TEXT")
    private String data;

    /**
     * İlişkili endpoint konfigürasyonu
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_config_id")
    private EndpointConfigEntity endpointConfig;

    /**
     * Bu sonuca bağlı uyarı olayları
     */
    @OneToMany(mappedBy = "monitoringResult", cascade = CascadeType.ALL)
    private java.util.List<AlertEvent> alertEvents;
}