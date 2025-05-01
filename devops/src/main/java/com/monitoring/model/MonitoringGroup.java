package com.monitoring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "monitoring_group")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringGroup {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @NotNull
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "enabled")
    private boolean enabled;

    /**
     * Bildirim e-posta adresi
     */
    @Column(name = "notification_email")
    @Email
    private String notificationEmail;

    /**
     * Bu gruba bağlı endpoint'ler
     */
    @ManyToMany(mappedBy = "monitoringGroups")
    private List<EndpointConfigEntity> endpoints;

    /**
     * Bu grubun sahibi olduğu servisler
     */
    @OneToMany(mappedBy = "ownerGroup")
    private List<ServiceEntity> ownedServices;
}