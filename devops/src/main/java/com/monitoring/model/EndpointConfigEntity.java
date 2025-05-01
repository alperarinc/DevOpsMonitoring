package com.monitoring.model;

import com.monitoring.enums.DatabaseType;
import com.monitoring.enums.EndpointType;
import com.monitoring.enums.Protocol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.monitoring.enums.HttpMethod;

import java.util.List;

@Entity
@Table(name = "endpoint_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointConfigEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EndpointType type;

    private boolean enabled;

    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "method")
    private HttpMethod method;

    @Column(columnDefinition = "TEXT")
    private String headers;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "protocol")
    private Protocol protocol;

    @Column(columnDefinition = "TEXT")
    private String testMessage;

    @Column(columnDefinition = "TEXT")
    private String expectedResponse;

    @Enumerated(EnumType.STRING)
    @Column(name = "db_type")
    private DatabaseType dbType;

    private String dbUsername;

    private String dbPassword;

    @Column(columnDefinition = "TEXT")
    private String query;

    private Long intervalMs;

    private Long timeoutMs;

    private Long thresholdMs;

    private Integer expectedStatus;

    @Column(columnDefinition = "TEXT")
    private String expectedResponseContains;

    // İlişkiler

    /**
     * Bu endpoint'in ait olduğu servis
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;


    /**
     * Bu endpoint için yapılan izleme sonuçları
     */
    @OneToMany(mappedBy = "endpointConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonitoringResult> monitoringResults;

    /**
     * Bu endpoint için oluşturulan uyarılar
     */
    @OneToMany(mappedBy = "endpointConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlertEvent> alertEvents;

    /**
     * Bu endpoint'in bağlı olduğu izleme grupları
     */
    @ManyToMany
    @JoinTable(
            name = "endpoint_monitoring_group",
            joinColumns = @JoinColumn(name = "endpoint_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private List<MonitoringGroup> monitoringGroups;
}