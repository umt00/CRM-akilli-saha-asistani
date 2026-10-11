package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.FirmaIstegi;
import com.akillisaha.crm.dto.request.YetkiliIstegi;
import com.akillisaha.crm.dto.response.FirmaYaniti;
import com.akillisaha.crm.dto.response.YetkiliYaniti;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.exception.ErisimEngellendiHatasi;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.security.*;
import com.akillisaha.crm.service.FirmaServisi;
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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FirmaDenetleyici için Web katmanı (@WebMvcTest) testleri.
 * Müşteri firma kartları, yetkililer, arama ve sayfalama uç noktalarını test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@WebMvcTest(controllers = FirmaDenetleyici.class)
@AutoConfigureMockMvc(addFilters = false)
class FirmaDenetleyiciTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FirmaServisi firmaServisi;

    @MockBean
    private JwtBelirteciSaglayici jwtBelirteciSaglayici;

    @MockBean
    private OzelKullaniciDetaylariServisi ozelKullaniciDetaylariServisi;

    @MockBean
    private JwtGirisNoktasi yetkisizErisimYakayici;

    @MockBean
    private JwtKimlikDogrulamaFiltresi jwtFiltresi;

    private FirmaYaniti ornekFirmaYaniti;

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

        ornekFirmaYaniti = FirmaYaniti.builder()
                .id(10L)
                .unvan("ABC Metal Sanayi Ltd. Şti.")
                .sehirBolge("Kocaeli / Gebze")
                .adres("Organize Sanayi Bölgesi No:4")
                .telefon("02621234567")
                .eposta("info@abcmetal.com")
                .mevcutTedarikciRakip("Rakip Metal")
                .tedarikEttigiUrunler("Sac ve Profil")
                .aylikKullanimMiktari("50 Ton")
                .bizdenAldigiUrunler("Profil")
                .atananPlasiyerAdi("Ali Vural")
                .atananPlasiyerId(1L)
                .yetkililer(new ArrayList<>())
                .olusturulmaTarihi(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/firmalar: Sayfalı firma listesi 200 OK ile dönmelidir")
    void firmalariListele_BasariliIstek_200OkDoner() throws Exception {
        // Arrange
        when(firmaServisi.firmalariGetir(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ornekFirmaYaniti)));

        // Act & Assert
        mockMvc.perform(get("/api/v1/firmalar")
                        .param("arama", "Metal")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.veri.content[0].id").value(10L))
                .andExpect(jsonPath("$.veri.content[0].unvan").value("ABC Metal Sanayi Ltd. Şti."));
    }

    @Test
    @DisplayName("GET /api/v1/firmalar/{id}: Var olan firma detayını 200 OK ile dönmelidir")
    void firmaDetayiGetir_MevcutFirma_200OkDoner() throws Exception {
        // Arrange
        when(firmaServisi.firmaDetayiGetir(eq(10L), any())).thenReturn(ornekFirmaYaniti);

        // Act & Assert
        mockMvc.perform(get("/api/v1/firmalar/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.veri.id").value(10L))
                .andExpect(jsonPath("$.veri.unvan").value("ABC Metal Sanayi Ltd. Şti."));
    }

    @Test
    @DisplayName("GET /api/v1/firmalar/{id}: Firma bulunamadığında 404 Not Found dönmelidir")
    void firmaDetayiGetir_BulunamayanFirma_404NotFoundDoner() throws Exception {
        // Arrange
        when(firmaServisi.firmaDetayiGetir(eq(999L), any()))
                .thenThrow(new KaynakBulunamadiHatasi("Firma bulunamadı: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/firmalar/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.basarili").value(false))
                .andExpect(jsonPath("$.mesaj").value("Firma bulunamadı: 999"));
    }

    @Test
    @DisplayName("GET /api/v1/firmalar/{id}: Başka plasiyerin firmasına erişildiğinde 403 Forbidden dönmelidir")
    void firmaDetayiGetir_YetkisizErisim_403ForbiddenDoner() throws Exception {
        // Arrange
        when(firmaServisi.firmaDetayiGetir(eq(20L), any()))
                .thenThrow(new ErisimEngellendiHatasi("Bu firmayı görüntüleme yetkiniz yok"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/firmalar/20"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.basarili").value(false))
                .andExpect(jsonPath("$.mesaj").value("Bu işlem için yetkiniz bulunmamaktadır"));
    }

    @Test
    @DisplayName("POST /api/v1/firmalar: Geçerli istekte 201 Created dönmelidir")
    void firmaOlustur_GecerliIstek_201CreatedDoner() throws Exception {
        // Arrange
        FirmaIstegi istek = FirmaIstegi.builder()
                .unvan("ABC Metal Sanayi Ltd. Şti.")
                .sehirBolge("Kocaeli")
                .tedarikEttigiUrunler("Sac")
                .build();

        when(firmaServisi.firmaOlustur(any(FirmaIstegi.class), any())).thenReturn(ornekFirmaYaniti);

        // Act & Assert
        mockMvc.perform(post("/api/v1/firmalar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(istek)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Firma başarıyla oluşturuldu"))
                .andExpect(jsonPath("$.veri.id").value(10L));
    }

    @Test
    @DisplayName("POST /api/v1/firmalar: Unvan boş bırakıldığında 400 Bad Request dönmelidir")
    void firmaOlustur_BosUnvan_400BadRequestDoner() throws Exception {
        // Arrange
        FirmaIstegi istek = FirmaIstegi.builder()
                .unvan("") // Boş unvan
                .sehirBolge("Kocaeli")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/firmalar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(istek)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.basarili").value(false))
                .andExpect(jsonPath("$.mesaj").value("Girilen form alanları doğrulanamadı"))
                .andExpect(jsonPath("$.veri.unvan").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/firmalar/{id}: Firma güncellendiğinde 200 OK dönmelidir")
    void firmaGuncelle_GecerliIstek_200OkDoner() throws Exception {
        // Arrange
        FirmaIstegi istek = FirmaIstegi.builder()
                .unvan("ABC Metal Sanayi Ltd. Şti. (Güncel)")
                .sehirBolge("Kocaeli / Gebze")
                .build();

        when(firmaServisi.firmaGuncelle(eq(10L), any(FirmaIstegi.class), any()))
                .thenReturn(ornekFirmaYaniti);

        // Act & Assert
        mockMvc.perform(put("/api/v1/firmalar/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(istek)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Firma başarıyla güncellendi"));
    }

    @Test
    @DisplayName("DELETE /api/v1/firmalar/{id}: Firma silindiğinde 200 OK dönmelidir")
    void firmaSil_GecerliIstek_200OkDoner() throws Exception {
        // Arrange
        doNothing().when(firmaServisi).firmaSil(eq(10L), any());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/firmalar/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Firma başarıyla silindi"));

        verify(firmaServisi).firmaSil(eq(10L), any());
    }

    @Test
    @DisplayName("POST /api/v1/firmalar/{firmaId}/yetkililer: Yetkili eklendiğinde 201 Created dönmelidir")
    void yetkiliEkle_GecerliIstek_201CreatedDoner() throws Exception {
        // Arrange
        YetkiliIstegi istek = YetkiliIstegi.builder()
                .firmaId(10L)
                .adSoyad("Ali Vural")
                .unvanGorev("Satın Alma Müdürü")
                .telefon("05551234567")
                .eposta("ali@abcmetal.com")
                .build();

        YetkiliYaniti yanit = YetkiliYaniti.builder()
                .id(101L)
                .adSoyad("Ali Vural")
                .unvanGorev("Satın Alma Müdürü")
                .telefon("05551234567")
                .eposta("ali@abcmetal.com")
                .olusturulmaTarihi(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        when(firmaServisi.yetkiliEkle(eq(10L), any(YetkiliIstegi.class), any())).thenReturn(yanit);

        // Act & Assert
        mockMvc.perform(post("/api/v1/firmalar/10/yetkililer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(istek)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Yetkili başarıyla eklendi"))
                .andExpect(jsonPath("$.veri.id").value(101L))
                .andExpect(jsonPath("$.veri.adSoyad").value("Ali Vural"));
    }

    @Test
    @DisplayName("DELETE /api/v1/firmalar/{firmaId}/yetkililer/{yetkiliId}: Yetkili silindiğinde 200 OK dönmelidir")
    void yetkiliSil_GecerliIstek_200OkDoner() throws Exception {
        // Arrange
        doNothing().when(firmaServisi).yetkiliSil(eq(10L), eq(101L), any());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/firmalar/10/yetkililer/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Yetkili başarıyla silindi"));

        verify(firmaServisi).yetkiliSil(eq(10L), eq(101L), any());
    }
}
