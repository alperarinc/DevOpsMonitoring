package com.monitoring.enums;

/**
 * Bir servisin çalışma durumunu temsil eden enum.
 * Bu durumlar servisin erişilebilirliğini ve performans seviyesini belirtir.
 */
public enum ServiceStatus {
    /**
     * Servis tam olarak çalışıyor ve performansı normal seviyede.
     * Herhangi bir sorun veya yavaşlama yoktur.
     */
    OPERATIONAL,

    /**
     * Servis çalışıyor ancak performans veya işlevsellik açısından sorunlar yaşanıyor.
     * Kullanıcılar servise erişebilir ancak yavaşlama, bazı özelliklerin geçici olarak
     * kullanılamaması veya belirli hataların oluşması söz konusu olabilir.
     */
    DEGRADED,

    /**
     * Servis çalışmıyor ve erişilemez durumda.
     * Sistem hatası, kesinti veya diğer nedenlerle servis tamamen devre dışı kalmıştır.
     */
    OUTAGE,

    /**
     * Servis, planlı bakım çalışması nedeniyle geçici olarak erişilemez durumda.
     * Bu durum, yükseltme, yapılandırma değişiklikleri veya diğer planlı bakım
     * işlemleri sırasında kullanılır.
     */
    MAINTENANCE,

    /**
     * Servis devre dışı bırakılmış veya henüz etkinleştirilmemiş durumda.
     * Bu durum genellikle servislerin yeni eklendiğinde veya yönetici tarafından
     * kasıtlı olarak devre dışı bırakıldığında kullanılır.
     */
    INACTIVE,

    /**
     * Servis aktif ve kullanılabilir durumda.
     * Farklı seviyede çalışma durumları arasında ayrım yapmak yerine sadece aktif
     * olup olmadığını belirtmek için kullanılabilir.
     */
    ACTIVE,

    /**
     * Servisin durumu belirlenemedi veya bilinmiyor.
     * Durum kontrol edilemediğinde veya geçerli bir durum döndürülemediğinde kullanılır.
     */
    UNKNOWN
}