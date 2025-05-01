package com.monitoring.enums;

/**
 * İzleme sonucu durumlarını temsil eden enum.
 */
public enum Status {
    OK,        // Her şey normal
    WARNING,   // Eşik değeri aşıldı ama hata yok
    CRITICAL,  // Ciddi bir hata var
    UNKNOWN    // Durum belirlenemedi
}