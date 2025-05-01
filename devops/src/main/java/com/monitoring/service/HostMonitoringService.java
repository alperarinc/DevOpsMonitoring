package com.monitoring.service;

import com.monitoring.config.MonitoringConfig;
import com.monitoring.enums.AlertLevel;
import com.monitoring.model.AlertEvent;
import com.monitoring.model.MonitoringResult;
import com.monitoring.enums.MonitoringType;
import com.monitoring.enums.Status;
import com.monitoring.model.HostMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Host sistem metriklerini izleyen servis.
 */
@Service
@ConditionalOnProperty(value = "monitoring.host.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class HostMonitoringService {

    private final MonitoringConfig monitoringConfig;
    private final NotificationCoordinator notificationCoordinator;
    private final Random random = new Random(); // ID üretimi için

    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hardware = systemInfo.getHardware();
    private final OperatingSystem os = systemInfo.getOperatingSystem();

    /**
     * Long türünde ID üretir
     * @return Benzersiz Long ID
     */
    private Long generateLongId() {
        // Basit bir numara üreteci - gerçek projede sequence veya başka bir strateji kullanılabilir
        return Math.abs(random.nextLong());
    }

    /**
     * Host metriklerini belirli aralıklarla izler.
     */
    @Scheduled(fixedDelayString = "${monitoring.host.interval:60000}")
    public void monitorHostMetrics() {
        try {
            log.debug("Host metrics monitoring started...");

            // Host metriklerini topla
            HostMetrics metrics = collectHostMetrics();

            // CPU kontrolü
            checkCpuUsage(metrics);

            // RAM kontrolü
            checkMemoryUsage(metrics);

            // Disk kontrolü
            checkDiskUsage(metrics);

            log.debug("Host metrics monitoring completed: {}", metrics);
        } catch (Exception e) {
            log.error("Error while monitoring host metrics", e);
        }
    }

    /**
     * Host metriklerini toplar.
     *
     * @return Host metrikleri
     */
    private HostMetrics collectHostMetrics() {
        // CPU kullanımı
        CentralProcessor processor = hardware.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();

        // Hafif bir gecikme ile daha doğru CPU kullanım ölçümü
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long[] currTicks = processor.getSystemCpuLoadTicks();
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

        // Bellek kullanımı
        GlobalMemory memory = hardware.getMemory();
        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        long usedMemory = totalMemory - availableMemory;
        double memoryUsage = ((double) usedMemory / totalMemory) * 100;

        // Disk kullanımı
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();

        long totalDiskSpace = 0;
        long usedDiskSpace = 0;

        for (OSFileStore store : fileStores) {
            totalDiskSpace += store.getTotalSpace();
            usedDiskSpace += (store.getTotalSpace() - store.getFreeSpace());
        }

        double diskUsage = ((double) usedDiskSpace / totalDiskSpace) * 100;

        // İşletim sistemi bilgileri
        String osInfo = os.getFamily() + " " + os.getVersionInfo().getVersion();

        // Sistem çalışma süresi
        long uptime = os.getSystemUptime();

        return HostMetrics.builder()
                .id(generateLongId()) // ID alanı eklendi
                .timestamp(LocalDateTime.now())
                .cpuUsage(cpuUsage)
                .totalMemory(totalMemory)
                .usedMemory(usedMemory)
                .memoryUsage(memoryUsage)
                .totalDiskSpace(totalDiskSpace)
                .usedDiskSpace(usedDiskSpace)
                .diskUsage(diskUsage)
                .uptime(uptime)
                .osInfo(osInfo)
                .hostName(System.getProperty("hostname", "localhost")) // Hostname bilgisi eklendi
                .build();
    }

    /**
     * CPU kullanımını kontrol eder ve threshold aşılırsa alarm oluşturur.
     *
     * @param metrics Host metrikleri
     */
    private void checkCpuUsage(HostMetrics metrics) {
        int cpuThreshold = monitoringConfig.getHost().getThresholds().getOrDefault("cpu", 80);

        if (metrics.getCpuUsage() > cpuThreshold) {
            log.warn("CPU usage threshold exceeded: {}% (threshold: {}%)",
                    String.format("%.2f", metrics.getCpuUsage()), cpuThreshold);

            MonitoringResult result = MonitoringResult.builder()
                    .id(generateLongId()) // String UUID yerine Long ID
                    .timestamp(LocalDateTime.now())
                    .type(MonitoringType.HOST)
                    .resourceId("cpu")
                    .success(true)
                    .responseTime(0)
                    .status(Status.WARNING)
                    .data("CPU usage: " + String.format("%.2f", metrics.getCpuUsage()) + "%")
                    .build();

            AlertEvent alertEvent = AlertEvent.builder()
                    .id(generateLongId()) // String UUID yerine Long ID
                    .timestamp(LocalDateTime.now())
                    .monitoringResult(result)
                    .level(AlertLevel.WARNING)
                    .message("CPU usage threshold exceeded: " + String.format("%.2f", metrics.getCpuUsage()) + "% (threshold: " + cpuThreshold + "%)")
                    .resolved(false)
                    .build();

            notificationCoordinator.sendAlert(alertEvent);
        }
    }

    /**
     * Bellek kullanımını kontrol eder ve threshold aşılırsa alarm oluşturur.
     *
     * @param metrics Host metrikleri
     */
    private void checkMemoryUsage(HostMetrics metrics) {
        int memoryThreshold = monitoringConfig.getHost().getThresholds().getOrDefault("ram", 90);

        if (metrics.getMemoryUsage() > memoryThreshold) {
            log.warn("Memory usage threshold exceeded: {}% (threshold: {}%)",
                    String.format("%.2f", metrics.getMemoryUsage()), memoryThreshold);

            MonitoringResult result = MonitoringResult.builder()
                    .id(generateLongId()) // String UUID yerine Long ID
                    .timestamp(LocalDateTime.now())
                    .type(MonitoringType.HOST)
                    .resourceId("memory")
                    .success(true)
                    .responseTime(0)
                    .status(Status.WARNING)
                    .data("Memory usage: " + String.format("%.2f", metrics.getMemoryUsage()) + "%")
                    .build();

            AlertEvent alertEvent = AlertEvent.builder()
                    .id(generateLongId()) // String UUID yerine Long ID
                    .timestamp(LocalDateTime.now())
                    .monitoringResult(result)
                    .level(AlertLevel.WARNING)
                    .message("Memory usage threshold exceeded: " + String.format("%.2f", metrics.getMemoryUsage()) + "% (threshold: " + memoryThreshold + "%)")
                    .resolved(false)
                    .build();

            notificationCoordinator.sendAlert(alertEvent);
        }
    }

    /**
     * Disk kullanımını kontrol eder ve threshold aşılırsa alarm oluşturur.
     *
     * @param metrics Host metrikleri
     */
    private void checkDiskUsage(HostMetrics metrics) {
        int diskThreshold = monitoringConfig.getHost().getThresholds().getOrDefault("disk", 85);

        if (metrics.getDiskUsage() > diskThreshold) {
            log.warn("Disk usage threshold exceeded: {}% (threshold: {}%)",
                    String.format("%.2f", metrics.getDiskUsage()), diskThreshold);

            MonitoringResult result = MonitoringResult.builder()
                    .id(generateLongId()) // String UUID yerine Long ID
                    .timestamp(LocalDateTime.now())
                    .type(MonitoringType.HOST)
                    .resourceId("disk")
                    .success(true)
                    .responseTime(0)
                    .status(Status.WARNING)
                    .data("Disk usage: " + String.format("%.2f", metrics.getDiskUsage()) + "%")
                    .build();

            AlertEvent alertEvent = AlertEvent.builder()
                    .id(generateLongId()) // String UUID yerine Long ID
                    .timestamp(LocalDateTime.now())
                    .monitoringResult(result)
                    .level(AlertLevel.WARNING)
                    .message("Disk usage threshold exceeded: " + String.format("%.2f", metrics.getDiskUsage()) + "% (threshold: " + diskThreshold + "%)")
                    .resolved(false)
                    .build();

            notificationCoordinator.sendAlert(alertEvent);
        }
    }

    /**
     * Host metriklerini getirir.
     *
     * @return Güncel host metrikleri
     */
    public HostMetrics getHostMetrics() {
        return collectHostMetrics();
    }
}