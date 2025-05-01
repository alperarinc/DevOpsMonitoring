package com.monitoring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Host sistem metriklerini temsil eden model sınıfı.
 */
@Entity
@Table(name = "host_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostMetrics {

    /**
     * Metriğin benzersiz tanımlayıcısı
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Metrik zamanı
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * CPU kullanım oranı (yüzde)
     */
    @Column(name = "cpu_usage", nullable = false)
    private double cpuUsage;

    /**
     * Toplam RAM miktarı (byte)
     */
    @Column(name = "total_memory", nullable = false)
    private long totalMemory;

    /**
     * Kullanılan RAM miktarı (byte)
     */
    @Column(name = "used_memory", nullable = false)
    private long usedMemory;

    /**
     * RAM kullanım oranı (yüzde)
     */
    @Column(name = "memory_usage", nullable = false)
    private double memoryUsage;

    /**
     * Toplam disk alanı (byte)
     */
    @Column(name = "total_disk_space", nullable = false)
    private long totalDiskSpace;

    /**
     * Kullanılan disk alanı (byte)
     */
    @Column(name = "used_disk_space", nullable = false)
    private long usedDiskSpace;

    /**
     * Disk kullanım oranı (yüzde)
     */
    @Column(name = "disk_usage", nullable = false)
    private double diskUsage;

    /**
     * Sistemin çalışma süresi (milisaniye)
     */
    @Column(name = "uptime", nullable = false)
    private long uptime;

    /**
     * İşletim sistemi adı ve versiyonu
     */
    @Column(name = "os_info")
    private String osInfo;

    /**
     * İlgili sunucu/host bilgisi (opsiyonel)
     */
    @Column(name = "host_name")
    private String hostName;

    /**
     * Sunucu IP adresi (opsiyonel)
     */
    @Column(name = "host_ip")
    @Pattern(
            regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "Geçerli bir IPv4 adresi giriniz (örn. 192.168.1.1)"
    )
    private String hostIp;
}