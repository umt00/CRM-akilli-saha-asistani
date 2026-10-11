package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.ZiyaretIstegi;
import com.akillisaha.crm.dto.response.ZiyaretYaniti;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.security.*;
import com.akillisaha.crm.service.ZiyaretServisi;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ZiyaretDenetleyici için Web katmanı (@WebMvcTest) testleri.
 * Saha ziyaretleri, 30 saniyelik form girişi, ajanda ve filtreleme uç noktalarını test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@WebMvcTest(controllers = ZiyaretDenetleyici.class)
@AutoConfigureMockMvc(addFilters = false)
class ZiyaretDenetleyiciTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ZiyaretServisi ziyaretServisi;

    @MockBean
    private JwtBelirteciSaglayici jwtBelirteciSaglayici;

    @MockBean
    private OzelKullaniciDetaylariServisi ozelKullaniciDetaylariServisi;

    @MockBean
    private JwtGirisNoktasi yetkisizErisimYakayici;

    @MockBean
    private JwtKimlikDogrulamaFiltresi jwtFiltresi;

    private ZiyaretYaniti ornekZiyaretYaniti;

    @BeforeEach
    void hazirla() {
        Kullanici kullanici = Kullanici.builder()
                .id(1L)
                .eposta("plasiyer@saha.com")
                .sifreOzeti("hash")
                .adSoyad("Ali Vural")
                .rol(Rol.PLASIYER)
                .aktifMi(true)
                .olusturulmaTarihi(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        OzelKullaniciDetaylari detaylar = new OzelKullaniciDetaylari(kullanici);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(detaylar, null, detaylar.getAuthorities())
        );

        ornekZiyaretYaniti = ZiyaretYaniti.builder()
                .id(50L)
                .firmaId(10L)
                .firmaAdi("ABC Metal Sanayi Ltd. Şti.")
                .sehirBolge("Kocaeli / Gebze")
                .yetkiliId(100L)
                .yetkiliAdi("Ali Vural")
                .yetkiliGorevi("Satın Alma Müdürü")
                .plasiyerId(1L)
                .plasiyerAdi("Ali Vural")
                .ziyaretTarihi(OffsetDateTime.of(2026, 5, 10, 14, 0, 0, 0, ZoneOffset.UTC))
                .ziyaretKonusu("Yeni Fiyat Teklifi ve Numune Sunumu")
                .numuneVerildiMi(true)
                .teklifVerildiMi(true)
                .numuneler(Collections.emptyList())
                .teklifler(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/ziyaretler: Sayfalanmış ziyaret listesi 200 OK ile dönmelidir")
    void ziyaretleriListele_BasariliIstek_200OkDoner() throws Exception {
        // Arrange
        when(ziyaretServisi.ziyaretleriGetir(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ornekZiyaretYaniti)));

        // Act & Assert
        mockMvc.perform(get("/api/v1/ziyaretler")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.veri.content[0].id").value(50L))
                .andExpect(jsonPath("$.veri.content[0].ziyaretKonusu").value("Yeni Fiyat Teklifi ve Numune Sunumu"));
    }

    @Test
    @DisplayName("GET /api/v1/ziyaretler/{id}: Var olan ziyaret detayı 200 OK ile dönmelidir")
    void ziyaretDetayiGetir_MevcutZiyaret_200OkDoner() throws Exception {
        // Arrange
        when(ziyaretServisi.ziyaretDetayiGetir(eq(50L), any())).thenReturn(ornekZiyaretYaniti);

        // Act & Assert
        mockMvc.perform(get("/api/v1/ziyaretler/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.veri.id").value(50L))
                .andExpect(jsonPath("$.veri.firmaAdi").value("ABC Metal Sanayi Ltd. Şti."));
    }

    @Test
    @DisplayName("GET /api/v1/ziyaretler/{id}: Bulunamayan ziyarette 404 Not Found dönmelidir")
    void ziyaretDetayiGetir_BulunamayanZiyaret_404NotFoundDoner() throws Exception {
        // Arrange
        when(ziyaretServisi.ziyaretDetayiGetir(eq(999L), any()))
                .thenThrow(new KaynakBulunamadiHatasi("Ziyaret bulunamadı: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/ziyaretler/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.basarili").value(false))
                .andExpect(jsonPath("$.mesaj").value("Ziyaret bulunamadı: 999"));
    }

    @Test
    @DisplayName("POST /api/v1/ziyaretler: Geçerli 30 saniyelik form gönderildiğinde 201 Created dönmelidir")
    void ziyaretOlustur_GecerliIstek_201CreatedDoner() throws Exception {
        // Arrange
        ZiyaretIstegi istek = ZiyaretIstegi.builder()
                .firmaId(10L)
                .yetkiliId(100L)
                .ziyaretTarihi(OffsetDateTime.of(2026, 5, 10, 14, 0, 0, 0, ZoneOffset.UTC))
                .ziyaretKonusu("Rutin Saha Ziyareti")
                .numuneVerildiMi(false)
                .teklifVerildiMi(false)
                .build();

        when(ziyaretServisi.ziyaretOlustur(any(ZiyaretIstegi.class), any())).thenReturn(ornekZiyaretYaniti);

        // Act & Assert
        mockMvc.perform(post("/api/v1/ziyaretler")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(istek)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Ziyaret başarıyla kaydedildi"))
                .andExpect(jsonPath("$.veri.id").value(50L));
    }

    @Test
    @DisplayName("POST /api/v1/ziyaretler: Zorunlu firmaId eksikse 400 Bad Request dönmelidir")
    void ziyaretOlustur_EksikFirmaId_400BadRequestDoner() throws Exception {
        // Arrange
        ZiyaretIstegi istek = ZiyaretIstegi.builder()
                .firmaId(null) // Zorunlu alan eksik
                .ziyaretTarihi(OffsetDateTime.now(ZoneOffset.UTC))
                .ziyaretKonusu("Konu")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/ziyaretler")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(istek)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.basarili").value(false))
                .andExpect(jsonPath("$.mesaj").value("Girilen form alanları doğrulanamadı"))
                .andExpect(jsonPath("$.veri.firmaId").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/ziyaretler/{id}: Ziyaret güncellendiğinde 200 OK dönmelidir")
    void ziyaretGuncelle_GecerliIstek_200OkDoner() throws Exception {
        // Arrange
        ZiyaretIstegi istek = ZiyaretIstegi.builder()
                .firmaId(10L)
                .ziyaretTarihi(OffsetDateTime.of(2026, 5, 10, 14, 0, 0, 0, ZoneOffset.UTC))
                .ziyaretKonusu("Güncellenen Konu")
                .build();

        when(ziyaretServisi.ziyaretGuncelle(eq(50L), any(ZiyaretIstegi.class), any()))
                .thenReturn(ornekZiyaretYaniti);

        // Act & Assert
        mockMvc.perform(put("/api/v1/ziyaretler/50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(istek)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Ziyaret başarıyla güncellendi"));
    }

    @Test
    @DisplayName("DELETE /api/v1/ziyaretler/{id}: Ziyaret silindiğinde 200 OK dönmelidir")
    void ziyaretSil_GecerliIstek_200OkDoner() throws Exception {
        // Arrange
        doNothing().when(ziyaretServisi).ziyaretSil(eq(50L), any());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/ziyaretler/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Ziyaret başarıyla silindi"));

        verify(ziyaretServisi).ziyaretSil(eq(50L), any());
    }

    @Test
    @DisplayName("GET /api/v1/ziyaretler/ajanda: Randevusu yaklaşanlar 200 OK ile listelenmelidir")
    void vadesiGelenleriGetir_GecerliIstek_200OkDoner() throws Exception {
        // Arrange
        when(ziyaretServisi.vadesiGelenAjandaZiyaretleriniGetir(any(), any()))
                .thenReturn(List.of(ornekZiyaretYaniti));

        // Act & Assert
        mockMvc.perform(get("/api/v1/ziyaretler/ajanda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Ajanda listesi başarıyla getirildi"))
                .andExpect(jsonPath("$.veri[0].id").value(50L));
    }
}
