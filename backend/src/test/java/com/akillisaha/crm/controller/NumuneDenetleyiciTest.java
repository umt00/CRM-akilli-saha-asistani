package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.NumuneIstegi;
import com.akillisaha.crm.dto.response.NumuneYaniti;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.enums.NumuneDurumu;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.security.*;
import com.akillisaha.crm.service.NumuneServisi;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * NumuneDenetleyici için Web katmanı (@WebMvcTest) testleri.
 * Numune kartları, durum geçişleri, istatistikler ve DTO validasyonunu test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@WebMvcTest(controllers = NumuneDenetleyici.class)
@AutoConfigureMockMvc(addFilters = false)
class NumuneDenetleyiciTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NumuneServisi numuneServisi;

    @MockBean
    private JwtBelirteciSaglayici jwtBelirteciSaglayici;

    @MockBean
    private OzelKullaniciDetaylariServisi ozelKullaniciDetaylariServisi;

    @MockBean
    private JwtGirisNoktasi yetkisizErisimYakayici;

    @MockBean
    private JwtKimlikDogrulamaFiltresi jwtFiltresi;

    private NumuneYaniti ornekNumuneYaniti;

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

        ornekNumuneYaniti = NumuneYaniti.builder()
                .id(100L)
                .firmaId(10L)
                .firmaAdi("Lezzet Pastanesi")
                .plasiyerId(1L)
                .plasiyerAdi("Ali Vural")
                .urunAdi("Kerry Çilek Aroması")
                .marka("Kerry")
                .miktar("500 GR")
                .durum(NumuneDurumu.BEKLEMEDE)
                .gonderimTarihi(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/numuneler: Sayfalanmış numune listesi 200 OK ile dönmelidir")
    void numuneleriListele_BasariliIstek_200OkDoner() throws Exception {
        // Arrange
        when(numuneServisi.numuneleriGetir(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ornekNumuneYaniti)));

        // Act & Assert
        mockMvc.perform(get("/api/v1/numuneler")
                        .param("durum", "BEKLEMEDE")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.veri.content[0].id").value(100L))
                .andExpect(jsonPath("$.veri.content[0].urunAdi").value("Kerry Çilek Aroması"));
    }

    @Test
    @DisplayName("GET /api/v1/numuneler/{id}: Var olan numune detayı 200 OK ile dönmelidir")
    void numuneDetayiGetir_MevcutNumune_200OkDoner() throws Exception {
        // Arrange
        when(numuneServisi.numuneDetayiGetir(eq(100L), any())).thenReturn(ornekNumuneYaniti);

        // Act & Assert
        mockMvc.perform(get("/api/v1/numuneler/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.veri.id").value(100L))
                .andExpect(jsonPath("$.veri.marka").value("Kerry"));
    }

    @Test
    @DisplayName("GET /api/v1/numuneler/{id}: Bulunamayan numunede 404 Not Found dönmelidir")
    void numuneDetayiGetir_BulunamayanNumune_404NotFoundDoner() throws Exception {
        // Arrange
        when(numuneServisi.numuneDetayiGetir(eq(999L), any()))
                .thenThrow(new KaynakBulunamadiHatasi("Numune bulunamadı: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/numuneler/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.basarili").value(false))
                .andExpect(jsonPath("$.mesaj").value("Numune bulunamadı: 999"));
    }

    @Test
    @DisplayName("POST /api/v1/numuneler: Geçerli istekte 201 Created dönmelidir")
    void numuneOlustur_GecerliIstek_201CreatedDoner() throws Exception {
        // Arrange
        NumuneIstegi istek = NumuneIstegi.builder()
                .firmaId(10L)
                .urunAdi("Kerry Çilek Aroması")
                .marka("Kerry")
                .miktar("500 GR")
                .build();

        when(numuneServisi.numuneOlustur(any(NumuneIstegi.class), any())).thenReturn(ornekNumuneYaniti);

        // Act & Assert
        mockMvc.perform(post("/api/v1/numuneler")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(istek)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Numune başarıyla kaydedildi"))
                .andExpect(jsonPath("$.veri.id").value(100L));
    }

    @Test
    @DisplayName("PATCH /api/v1/numuneler/{id}/durum: Durum güncellendiğinde 200 OK dönmelidir")
    void numuneDurumuGuncelle_GecerliIstek_200OkDoner() throws Exception {
        // Arrange
        NumuneYaniti guncelYanit = NumuneYaniti.builder()
                .id(100L)
                .durum(NumuneDurumu.BEGENDI)
                .sonucNotlari("Tadı çok beğenildi, sipariş verecekler")
                .build();

        when(numuneServisi.numuneDurumuGuncelle(eq(100L), eq(NumuneDurumu.BEGENDI), any(), any()))
                .thenReturn(guncelYanit);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/numuneler/100/durum")
                        .param("durum", "BEGENDI")
                        .param("sonucNotlari", "Tadı çok beğenildi, sipariş verecekler"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Numune durumu başarıyla güncellendi"))
                .andExpect(jsonPath("$.veri.durum").value("BEGENDI"));
    }

    @Test
    @DisplayName("DELETE /api/v1/numuneler/{id}: Numune silindiğinde 200 OK dönmelidir")
    void numuneSil_GecerliIstek_200OkDoner() throws Exception {
        // Arrange
        doNothing().when(numuneServisi).numuneSil(eq(100L), any());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/numuneler/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Numune başarıyla silindi"));

        verify(numuneServisi).numuneSil(eq(100L), any());
    }

    @Test
    @DisplayName("GET /api/v1/numuneler/istatistikler: İstatistik haritası 200 OK ile dönmelidir")
    void istatistikleriGetir_GecerliIstek_200OkDoner() throws Exception {
        // Arrange
        Map<String, Long> istatistikler = Map.of(
                "BEKLEMEDE", 5L,
                "BEGENDI", 12L,
                "REDDETTI", 2L
        );
        when(numuneServisi.numuneIstatistikleriniGetir(any())).thenReturn(istatistikler);

        // Act & Assert
        mockMvc.perform(get("/api/v1/numuneler/istatistikler"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("İstatistikler başarıyla getirildi"))
                .andExpect(jsonPath("$.veri.BEGENDI").value(12));
    }
}
