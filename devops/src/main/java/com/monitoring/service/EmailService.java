package com.monitoring.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String username, String resetToken);
}