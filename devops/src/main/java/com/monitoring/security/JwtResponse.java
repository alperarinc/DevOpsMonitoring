package com.monitoring.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "JWT yanıt nesnesi")
public class JwtResponse {

    @Schema(description = "JWT erişim anahtarı", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Token tipi", example = "Bearer", defaultValue = "Bearer")
    private String type = "Bearer";

    @Schema(description = "Kullanıcı adı", example = "alperarinc")
    private String username;

    @Schema(description = "Kullanıcının yetkileri/rolleri", example = "[\"ROLE_USER\"]")
    private List<String> roles;

    public JwtResponse(String accessToken, String username, List<String> roles) {
        this.token = accessToken;
        this.username = username;
        this.roles = roles;
    }
}
