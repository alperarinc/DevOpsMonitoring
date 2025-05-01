package com.monitoring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standart API yanıt formatı
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standart API yanıt formatı")
public class MonitoringApiResponse<T> {

    @Schema(description = "İşlem başarı durumu")
    private boolean success;

    @Schema(description = "İşlemle ilgili mesaj")
    private String message;

    @Schema(description = "Yanıt verisi")
    private T data;

    @Schema(description = "Yanıt oluşturma zamanı")
    private LocalDateTime timestamp;

    @Schema(description = "Sayfalama meta verileri")
    private PageMetadata pageMetadata;

    /**
     * Başarılı yanıt oluşturur (veri olmadan)
     */
    public static <T> MonitoringApiResponse<T> success(String message) {
        return MonitoringApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Başarılı yanıt oluşturur (veri ile)
     */
    public static <T> MonitoringApiResponse<T> success(String message, T data) {
        return MonitoringApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Sayfalanmış başarılı yanıt oluşturur
     */
    public static <T> MonitoringApiResponse<List<T>> success(String message, Page<T> page) {
        PageMetadata metadata = PageMetadata.builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return MonitoringApiResponse.<List<T>>builder()
                .success(true)
                .message(message)
                .data(page.getContent())
                .pageMetadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Hata yanıtı oluşturur
     */
    public static <T> MonitoringApiResponse<T> error(String message) {
        return MonitoringApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Sayfalama meta verileri iç sınıfı
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Sayfalama meta verileri")
    public static class PageMetadata {
        @Schema(description = "Mevcut sayfa numarası")
        private int pageNumber;

        @Schema(description = "Sayfa başına eleman sayısı")
        private int pageSize;

        @Schema(description = "Toplam eleman sayısı")
        private long totalElements;

        @Schema(description = "Toplam sayfa sayısı")
        private int totalPages;
    }
}