package com.monitoring.dto;

import com.monitoring.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Yeni kullanıcı kayıt isteği")
public class SignupRequest {

    @NotBlank
    @Size(min = 3, max = 20)
    @Schema(description = "Kullanıcı adı", example = "alperarinc")
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    @Schema(description = "E-posta adresi", example = "alper@arinc.com")
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    @Schema(description = "Şifre", example = "sifre123")
    private String password;

    private boolean enablad;
}
