package com.monitoring.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Kullanıcı giriş isteği")
public class LoginRequest {

    @NotBlank
    @Schema(description = "Kullanıcı adı veya e-posta", example = "alperarinc")
    private String username;

    @NotBlank
    @Schema(description = "Kullanıcının şifresi", example = "gizliSifre123")
    private String password;
}
