package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.response.ExcelIceAktarimSonucuYaniti;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.exception.GecersizIstekHatasi;
import com.akillisaha.crm.security.*;
import com.akillisaha.crm.service.ExcelServisi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ExcelDenetleyici için Web katmanı (@WebMvcTest) testleri.
 * 17 kolonluk Excel dışa aktarım (Export indirme) ve içe aktarım (Import yükleme)
 * HTTP uç noktalarını test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@WebMvcTest(controllers = ExcelDenetleyici.class)
@AutoConfigureMockMvc(addFilters = false)
class ExcelDenetleyiciTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExcelServisi excelServisi;

    @MockBean
    private JwtBelirteciSaglayici jwtBelirteciSaglayici;

    @MockBean
    private OzelKullaniciDetaylariServisi ozelKullaniciDetaylariServisi;

    @MockBean
    private JwtGirisNoktasi yetkisizErisimYakayici;

    @MockBean
    private JwtKimlikDogrulamaFiltresi jwtFiltresi;

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
    }

    @Test
    @DisplayName("GET /api/v1/excel/disa-aktar: Excel dosyası attachment başlığıyla 200 OK dönmelidir")
    void disaAktar_BasariliIstek_200OkVeExcelDosyasiDoner() throws Exception {
        // Arrange
        byte[] sahteExcelIcerigi = "PK sahte excel baytlari".getBytes();
        when(excelServisi.ziyaretleriExceleAktar(any(), any(), any(), any())).thenReturn(sahteExcelIcerigi);

        // Act & Assert
        mockMvc.perform(get("/api/v1/excel/disa-aktar"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("attachment; filename=\"Saha_Ziyaretleri_")))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(sahteExcelIcerigi));
    }

    @Test
    @DisplayName("POST /api/v1/excel/ice-aktar: Geçerli Excel dosyası yüklendiğinde 200 OK ve istatistik dönmelidir")
    void iceAktar_GecerliDosya_200OkVeIstatistikDoner() throws Exception {
        // Arrange
        MockMultipartFile yuklenenDosya = new MockMultipartFile(
                "dosya",
                "ziyaretler.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "ornek icerik".getBytes()
        );

        ExcelIceAktarimSonucuYaniti yanit = ExcelIceAktarimSonucuYaniti.builder()
                .toplamSatirSayisi(5)
                .yeniFirmaSayisi(2)
                .olusturulanZiyaretSayisi(5)
                .olusturulanNumuneSayisi(3)
                .olusturulanTeklifSayisi(1)
                .hataMesajlari(Collections.emptyList())
                .build();

        when(excelServisi.exceldenZiyaretleriIceAktar(any(), any())).thenReturn(yanit);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/excel/ice-aktar").file(yuklenenDosya))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basarili").value(true))
                .andExpect(jsonPath("$.mesaj").value("Excel dosyası başarıyla işlendi"))
                .andExpect(jsonPath("$.veri.toplamSatirSayisi").value(5))
                .andExpect(jsonPath("$.veri.yeniFirmaSayisi").value(2))
                .andExpect(jsonPath("$.veri.olusturulanZiyaretSayisi").value(5));
    }

    @Test
    @DisplayName("POST /api/v1/excel/ice-aktar: Boş veya bozuk dosyada 400 Bad Request dönmelidir")
    void iceAktar_BozukDosya_400BadRequestDoner() throws Exception {
        // Arrange
        MockMultipartFile bosDosya = new MockMultipartFile(
                "dosya",
                "bos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        when(excelServisi.exceldenZiyaretleriIceAktar(any(), any()))
                .thenThrow(new GecersizIstekHatasi("Yüklenen Excel dosyası boş olamaz"));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/excel/ice-aktar").file(bosDosya))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.basarili").value(false))
                .andExpect(jsonPath("$.mesaj").value("Yüklenen Excel dosyası boş olamaz"));
    }
}
