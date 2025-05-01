package com.monitoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordChangeRequest {
    @NotBlank(message = "Token boş olamaz")
    private String token;

    @NotBlank(message = "Yeni şifre boş olamaz")
    @Size(min = 6, max = 40, message = "Şifre 6-40 karakter arasında olmalıdır")
    private String newPassword;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}