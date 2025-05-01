package com.monitoring.enums;

/**
 * Servis önceliklerini temsil eden enum.
 */
public enum ServicePriority {
    CRITICAL,    // İş açısından kritik, 7/24 izleme
    HIGH,        // Yüksek öncelikli, hızlı müdahale gerekli
    MEDIUM,      // Orta öncelikli
    LOW          // Düşük öncelikli, gecikmeli müdahale kabul edilebilir
}