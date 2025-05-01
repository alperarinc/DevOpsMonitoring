package com.monitoring.service;

import com.monitoring.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Monitoring System}")
    private String appName;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Autowired
    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendPasswordResetEmail(String to, String username, String resetToken) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName);
            helper.setTo(to);
            helper.setSubject("Şifre Sıfırlama Talebi");

            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

            String emailContent =
                    "<html>" +
                            "<body style='font-family: Arial, sans-serif; color: #333333;'>" +
                            "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #dddddd; border-radius: 5px;'>" +
                            "<h2 style='color: #0066cc;'>Şifre Sıfırlama</h2>" +
                            "<p>Merhaba " + username + ",</p>" +
                            "<p>Hesabınız için bir şifre sıfırlama talebinde bulundunuz.</p>" +
                            "<p>Şifrenizi sıfırlamak için aşağıdaki butona tıklayınız:</p>" +
                            "<p style='text-align: center;'>" +
                            "<a href='" + resetLink + "' style='display: inline-block; background-color: #0066cc; color: white; padding: 10px 20px; " +
                            "text-decoration: none; border-radius: 4px; font-weight: bold;'>Şifremi Sıfırla</a>" +
                            "</p>" +
                            "<p>Ya da aşağıdaki bağlantıyı tarayıcınıza kopyalayabilirsiniz:</p>" +
                            "<p><a href='" + resetLink + "'>" + resetLink + "</a></p>" +
                            "<p><strong>Not:</strong> Bu bağlantı 1 saat boyunca geçerlidir.</p>" +
                            "<p>Eğer şifre sıfırlama talebinde bulunmadıysanız, lütfen bu e-postayı görmezden geliniz.</p>" +
                            "<hr style='border: none; border-top: 1px solid #dddddd; margin: 20px 0;'>" +
                            "<p style='font-size: 12px; color: #666666; text-align: center;'>" + appName + " - Bu otomatik bir e-postadır, lütfen yanıtlamayınız.</p>" +
                            "</div>" +
                            "</body>" +
                            "</html>";

            helper.setText(emailContent, true); // true parametresi HTML içeriği aktifleştirir

            javaMailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("E-posta gönderilirken bir hata oluştu: " + e.getMessage(), e);
        }
    }
}