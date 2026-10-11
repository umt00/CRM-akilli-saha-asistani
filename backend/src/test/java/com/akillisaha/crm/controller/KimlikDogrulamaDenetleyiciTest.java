package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.GirisIstegi;
import com.akillisaha.crm.dto.request.KayitIstegi;
import com.akillisaha.crm.dto.response.KimlikDogrulamaYaniti;
import com.akillisaha.crm.dto.response.KullaniciOzetiYaniti;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.security.*;
import com.akillisaha.crm.service.KimlikDogrulamaServisi;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * KimlikDogrulamaDenetleyici için Web katmanı (@WebMvcTest) testleri.
 * Giriş (Login), Kayıt (Register) ve Profil REST uç noktalarını,
 * JSON serileştirmesini ve DTO doğrulama kurallarını test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@WebMvcTest(controllers = KimlikDogrulamaDenetleyici.class)
@AutoConfigureMockMvc(addFilters = false)
class KimlikDogrulamaDenetleyiciTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KimlikDogrulamaServisi kimlikDogrulamaServisi;

    @MockBean
    private JwtBelirteciSaglayici jwtBelirteciSaglayici;

    @MockBean
    private OzelKullaniciDetaylariServisi ozelKullaniciDetaylariServisi;

    @MockBean
    private JwtGirisNoktasi yetkisizErisimYakayici;

    @MockBean
    private JwtKimlikDogrulamaFiltresi jwtFiltresi;

    @Test
    @DisplayName("POST /api/v1/kimlik-dogrulama/giris: Başarılı girişte 200 OK ve JWT token dönmelidir")
    void girisYap_GecerliBilgilerle_200OkVeTokenDoner() throws Exception {
        // Arrange
        GirisIstegi istek = GirisIstegi.builder()
                .eposta("plasiyer@saha.com")
                .sifre("Gizli123!")
                .build();

        KimlikDogrulamaYaniti yanit = KimlikDogrulamaYaniti.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .tokenTipi("Bearer")
                .kullaniciId(1L)
                .eposta("plasiyer@saha.com")
                .adSoyad("Ali Vural")
                .rol(Rol.PLASIYER)
                .build();

        when(kimlikDogrulamaServisi.girisYap(any(GirisIstegi.class))).thenReturn(yanit);

        // Act & Assert
        mockMvc.perform(post("/api/v1/kimlik-dogrulama/giris")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(istek)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Giriş başarılı"))
                .andExpect(jsonPath("$.veri.token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
                .andExpect(jsonPath("$.veri.eposta").value("plasiyer@saha.com"))
                .andExpect(jsonPath("$.veri.rol").value("PLASIYER"));
    }

    @Test
    @DisplayName("POST /api/v1/kimlik-dogrulama/giris: Geçersiz e-posta formatında 400 Bad Request dönmelidir")
    void girisYap_GecersizEposta_400BadRequestDoner() throws Exception {
        // Arrange
        GirisIstegi istek = GirisIstegi.builder()
                .eposta("gecersiz-eposta-formati")
                .sifre("Gizli123!")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/kimlik-dogrulama/giris")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(istek)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.basarili").value(false))
                .andExpect(jsonPath("$.mesaj").value("Girilen form alanları doğrulanamadı"))
                .andExpect(jsonPath("$.veri.eposta").exists());
    }

    @Test
    @DisplayName("POST /api/v1/kimlik-dogrulama/giris: Hatalı şifrede 401 Unauthorized dönmelidir")
    void girisYap_HataliSifre_401UnauthorizedDoner() throws Exception {
        // Arrange
        GirisIstegi istek = GirisIstegi.builder()
                .eposta("plasiyer@saha.com")
                .sifre("YanlisSifre123")
                .build();

        when(kimlikDogrulamaServisi.girisYap(any(GirisIstegi.class)))
                .thenThrow(new BadCredentialsException("Hatalı kimlik bilgisi"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/kimlik-dogrulama/giris")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(istek)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.basarili").value(false))
                .andExpect(jsonPath("$.mesaj").value("E-posta adresi veya şifre hatalı"));
    }

    @Test
    @DisplayName("POST /api/v1/kimlik-dogrulama/kayit: Başarılı kayıtta 201 Created dönmelidir")
    void kayitOl_GecerliBilgilerle_201CreatedDoner() throws Exception {
        // Arrange
        KayitIstegi istek = KayitIstegi.builder()
                .eposta("yeni.plasiyer@saha.com")
                .sifre("Gizli123!")
                .adSoyad("Mehmet Yeni")
                .rol(Rol.PLASIYER)
                .telefon("05559876543")
                .build();

        KimlikDogrulamaYaniti yanit = KimlikDogrulamaYaniti.builder()
                .token("yeni-jwt-token")
                .tokenTipi("Bearer")
                .kullaniciId(2L)
                .eposta("yeni.plasiyer@saha.com")
                .adSoyad("Mehmet Yeni")
                .rol(Rol.PLASIYER)
                .build();

        when(kimlikDogrulamaServisi.kayitOl(any(KayitIstegi.class))).thenReturn(yanit);

        // Act & Assert
        mockMvc.perform(post("/api/v1/kimlik-dogrulama/kayit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(istek)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Kullanıcı başarıyla kaydedildi"))
                .andExpect(jsonPath("$.veri.token").value("yeni-jwt-token"));
    }

    @Test
    @DisplayName("GET /api/v1/kimlik-dogrulama/profilim: Aktif kullanıcı profilini 200 OK ile dönmelidir")
    void profilGetir_AktifKullaniciyla_200OkDoner() throws Exception {
        // Arrange
        Kullanici kullanici = Kullanici.builder()
                .id(1L)
                .eposta("aktif@saha.com")
                .sifreOzeti("hash")
                .adSoyad("Aktif Kullanici")
                .rol(Rol.PLASIYER)
                .aktifMi(true)
                .olusturulmaTarihi(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        OzelKullaniciDetaylari detaylar = new OzelKullaniciDetaylari(kullanici);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(detaylar, null, detaylar.getAuthorities())
        );

        KullaniciOzetiYaniti profil = KullaniciOzetiYaniti.builder()
                .id(1L)
                .eposta("aktif@saha.com")
                .adSoyad("Aktif Kullanici")
                .rol(Rol.PLASIYER)
                .build();

        when(kimlikDogrulamaServisi.aktifKullaniciyiGetir(any())).thenReturn(profil);

        // Act & Assert
        mockMvc.perform(get("/api/v1/kimlik-dogrulama/profilim"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.veri.eposta").value("aktif@saha.com"))
                .andExpect(jsonPath("$.veri.adSoyad").value("Aktif Kullanici"));
    }
}
