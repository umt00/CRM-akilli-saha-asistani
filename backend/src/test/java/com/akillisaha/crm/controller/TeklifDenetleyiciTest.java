package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.TeklifIstegi;
import com.akillisaha.crm.dto.response.TeklifYaniti;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.enums.TeklifDurumu;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.security.*;
import com.akillisaha.crm.service.TeklifServisi;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TeklifDenetleyici için Web katmanı (@WebMvcTest) testleri.
 * Ticari teklifler, fırsat yönetimi, durum geçişleri ve tutar güncellemelerini test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@WebMvcTest(controllers = TeklifDenetleyici.class)
@AutoConfigureMockMvc(addFilters = false)
class TeklifDenetleyiciTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TeklifServisi teklifServisi;

    @MockBean
    private JwtBelirteciSaglayici jwtBelirteciSaglayici;

    @MockBean
    private OzelKullaniciDetaylariServisi ozelKullaniciDetaylariServisi;

    @MockBean
    private JwtGirisNoktasi yetkisizErisimYakayici;

    @MockBean
    private JwtKimlikDogrulamaFiltresi jwtFiltresi;

    private TeklifYaniti ornekTeklifYaniti;

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

        ornekTeklifYaniti = TeklifYaniti.builder()
                .id(200L)
                .firmaId(10L)
                .firmaAdi("Lezzet Pastanesi")
                .plasiyerId(1L)
                .plasiyerAdi("Ali Vural")
                .baslik("Yıllık Margarin Teklifi")
                .tutar(new BigDecimal("250000.00"))
                .paraBirimi("TRY")
                .durum(TeklifDurumu.ACIK)
                .olusturulmaTarihi(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/teklifler: Sayfalı teklif listesi 200 OK ile dönmelidir")
    void teklifleriListele_BasariliIstek_200OkDoner() throws Exception {
        // Arrange
        when(teklifServisi.teklifleriGetir(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ornekTeklifYaniti)));

        // Act & Assert
        mockMvc.perform(get("/api/v1/teklifler")
                        .param("durum", "ACIK")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.veri.content[0].id").value(200L))
                .andExpect(jsonPath("$.veri.content[0].baslik").value("Yıllık Margarin Teklifi"))
                .andExpect(jsonPath("$.veri.content[0].tutar").value(250000.00));
    }

    @Test
    @DisplayName("GET /api/v1/teklifler/{id}: Var olan teklif detayı 200 OK ile dönmelidir")
    void teklifDetayiGetir_MevcutTeklif_200OkDoner() throws Exception {
        // Arrange
        when(teklifServisi.teklifDetayiGetir(eq(200L), any())).thenReturn(ornekTeklifYaniti);

        // Act & Assert
        mockMvc.perform(get("/api/v1/teklifler/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.veri.id").value(200L))
                .andExpect(jsonPath("$.veri.firmaAdi").value("Lezzet Pastanesi"));
    }

    @Test
    @DisplayName("GET /api/v1/teklifler/{id}: Bulunamayan teklifte 404 Not Found dönmelidir")
    void teklifDetayiGetir_BulunamayanTeklif_404NotFoundDoner() throws Exception {
        // Arrange
        when(teklifServisi.teklifDetayiGetir(eq(999L), any()))
                .thenThrow(new KaynakBulunamadiHatasi("Teklif bulunamadı: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/teklifler/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.basarili").value(false))
                .andExpect(jsonPath("$.mesaj").value("Teklif bulunamadı: 999"));
    }

    @Test
    @DisplayName("POST /api/v1/teklifler: Geçerli istekte 201 Created dönmelidir")
    void teklifOlustur_GecerliIstek_201CreatedDoner() throws Exception {
        // Arrange
        TeklifIstegi istek = TeklifIstegi.builder()
                .firmaId(10L)
                .baslik("Yıllık Margarin Teklifi")
                .tutar(new BigDecimal("250000.00"))
                .paraBirimi("TRY")
                .durum(TeklifDurumu.ACIK)
                .build();

        when(teklifServisi.teklifOlustur(any(TeklifIstegi.class), any())).thenReturn(ornekTeklifYaniti);

        // Act & Assert
        mockMvc.perform(post("/api/v1/teklifler")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(istek)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Teklif başarıyla oluşturuldu"))
                .andExpect(jsonPath("$.veri.id").value(200L));
    }

    @Test
    @DisplayName("PATCH /api/v1/teklifler/{id}/durum: Durum güncellendiğinde 200 OK dönmelidir")
    void teklifDurumuGuncelle_GecerliIstek_200OkDoner() throws Exception {
        // Arrange
        TeklifYaniti guncelYanit = TeklifYaniti.builder()
                .id(200L)
                .durum(TeklifDurumu.KAZANILDI)
                .tutar(new BigDecimal("250000.00"))
                .build();

        when(teklifServisi.teklifDurumuGuncelle(eq(200L), eq(TeklifDurumu.KAZANILDI), any()))
                .thenReturn(guncelYanit);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/teklifler/200/durum")
                        .param("durum", "KAZANILDI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Teklif durumu başarıyla güncellendi"))
                .andExpect(jsonPath("$.veri.durum").value("KAZANILDI"));
    }

    @Test
    @DisplayName("DELETE /api/v1/teklifler/{id}: Teklif silindiğinde 200 OK dönmelidir")
    void teklifSil_GecerliIstek_200OkDoner() throws Exception {
        // Arrange
        doNothing().when(teklifServisi).teklifSil(eq(200L), any());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/teklifler/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Teklif başarıyla silindi"));

        verify(teklifServisi).teklifSil(eq(200L), any());
    }
}
