package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.LoginRequest;
import com.akillisaha.crm.dto.request.RegisterRequest;
import com.akillisaha.crm.dto.response.ApiResponse;
import com.akillisaha.crm.dto.response.AuthResponse;
import com.akillisaha.crm.dto.response.UserSummaryResponse;
import com.akillisaha.crm.security.CustomUserDetails;
import com.akillisaha.crm.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Kimlik Doğrulama (Auth)", description = "Giriş, kayıt olma ve kullanıcı profili uç noktaları")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Kullanıcı Girişi", description = "E-posta ve şifre ile JWT Bearer token alır")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Giriş başarılı", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Yeni Kullanıcı Kaydı", description = "Yeni temsilci veya yönetici hesabı oluşturur")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kullanıcı başarıyla kaydedildi", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Aktif Kullanıcı Bilgisi", description = "Oturum açmış olan kullanıcının profil detaylarını döner")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        UserSummaryResponse response = authService.getCurrentUser(currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
