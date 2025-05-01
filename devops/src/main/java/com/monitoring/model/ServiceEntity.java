package com.monitoring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.monitoring.enums.*;

@Entity
@Table(name = "service")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    private ServiceStatus status;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_group_id")
    private MonitoringGroup ownerGroup;

    @Email
    private String contactEmail;

    private boolean enabled;

    @Enumerated(EnumType.STRING)
    private ServicePriority priority;

    private String tags;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EndpointConfigEntity> endpoints;
}