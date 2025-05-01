package com.monitoring.controller;

import com.monitoring.enums.Role;
import com.monitoring.security.JwtUtils;
import com.monitoring.model.User;
import com.monitoring.dto.LoginRequest;
import com.monitoring.dto.SignupRequest;
import com.monitoring.security.JwtResponse;
import com.monitoring.dto.MessageResponse;
import com.monitoring.dto.TokenVerificationResponse;
import com.monitoring.dto.ResponseWrapper;
import com.monitoring.dto.PasswordResetRequest;
import com.monitoring.dto.PasswordChangeRequest;
import com.monitoring.repository.UserRepository;
import com.monitoring.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Giriş ve kullanıcı kaydı işlemleri")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    @Value("${app.reset-token.expiry-hours:1}")
    private int resetTokenExpiryHours;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          PasswordEncoder encoder, JwtUtils jwtUtils, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService;
    }

    @Operation(summary = "Kullanıcı girişi", description = "Kullanıcı adı ve şifre ile giriş yapar ve JWT döner.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Başarılı giriş", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "401", description = "Geçersiz kimlik bilgileri", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        JwtResponse jwtResponse = new JwtResponse(
                jwt,
                userDetails.getUsername(),
                roles);

        return ResponseEntity.ok(new ResponseWrapper<>(true, "Giriş başarılı", jwtResponse));
    }

    @Operation(summary = "Yeni kullanıcı kaydı", description = "Yeni kullanıcı oluşturur. Rol sabit olarak USER atanır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kayıt başarılı", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz veri veya kullanıcı/eposta zaten kayıtlı", content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<ResponseWrapper<Void>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new ResponseWrapper<>(false, "Hata: Kullanıcı adı zaten kullanılıyor!", null));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new ResponseWrapper<>(false, "Hata: Email zaten kullanılıyor!", null));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRoles(Set.of(Role.USER)); // Sabit USER rolü
        user.setEnabled(true); // Varsayılan olarak aktif

        userRepository.save(user);

        return ResponseEntity.ok(new ResponseWrapper<>(true, "Kullanıcı başarıyla kaydedildi!", null));
    }

    @Operation(summary = "JWT token doğrulama", description = "Gönderilen JWT token'ın geçerli olup olmadığını kontrol eder.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token geçerli", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "401", description = "Geçersiz veya süresi dolmuş token", content = @Content)
    })
    @GetMapping("/verify")
    public ResponseEntity<ResponseWrapper<String>> verifyToken(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.substring(7); // "Bearer " prefixini kaldır

        if (jwtUtils.validateJwtToken(token)) {
            String username = jwtUtils.getUserNameFromJwtToken(token);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Token geçerli", username));
        } else {
            return ResponseEntity.status(401).body(new ResponseWrapper<>(false, "Geçersiz veya süresi dolmuş token", null));
        }
    }

    @Operation(summary = "Kullanıcı çıkışı", description = "Oturumu sonlandırır. Bu işlem client tarafında token'ın silinmesi ile tamamlanmalıdır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Başarıyla çıkış yapıldı", content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<ResponseWrapper<Void>> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new ResponseWrapper<>(true, "Başarıyla çıkış yapıldı!", null));
    }

    @Operation(summary = "Şifre sıfırlama talebi", description = "Kullanıcının email adresine şifre sıfırlama bağlantısı gönderir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sıfırlama talebi başarılı", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "404", description = "Email adresi bulunamadı", content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseWrapper<Void>> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            return ResponseEntity
                    .status(404)
                    .body(new ResponseWrapper<>(false, "Bu email adresine sahip kullanıcı bulunamadı.", null));
        }

        User user = userOptional.get();

        // Kullanıcı aktif değilse
        if (!user.isEnabled()) {
            return ResponseEntity
                    .badRequest()
                    .body(new ResponseWrapper<>(false, "Bu hesap aktif değil.", null));
        }

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(resetTokenExpiryHours));
        userRepository.save(user);

        // Email gönderme işlemi
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetToken);

        return ResponseEntity.ok(new ResponseWrapper<>(true, "Şifre sıfırlama bağlantısı email adresinize gönderildi.", null));
    }

    @Operation(summary = "Şifre sıfırlama", description = "Şifre sıfırlama token'ı ile yeni şifre belirleme.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Şifre başarıyla değiştirildi", content = @Content(schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz veya süresi dolmuş token", content = @Content(schema = @Schema(implementation = ResponseWrapper.class)))
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ResponseWrapper<Void>> resetPassword(@Valid @RequestBody PasswordChangeRequest request) {
        Optional<User> userOptional = userRepository.findByResetToken(request.getToken());

        if (userOptional.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(new ResponseWrapper<>(false, "Geçersiz şifre sıfırlama token'ı.", null));
        }

        User user = userOptional.get();

        // Token süresinin geçip geçmediğini kontrol et
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity
                    .badRequest()
                    .body(new ResponseWrapper<>(false, "Şifre sıfırlama token'ının süresi dolmuş. Lütfen yeni bir sıfırlama talebi oluşturun.", null));
        }

        // Yeni şifreyi kaydet
        user.setPassword(encoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok(new ResponseWrapper<>(true, "Şifreniz başarıyla değiştirildi. Yeni şifrenizle giriş yapabilirsiniz.", null));
    }
}