package com.monitoring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Genel başarı/başarısızlık mesajı yanıtı")
public class MessageResponse {

    @Schema(description = "İşlem sonucu mesajı", example = "Kullanıcı başarıyla kaydedildi!")
    private String message;
}
