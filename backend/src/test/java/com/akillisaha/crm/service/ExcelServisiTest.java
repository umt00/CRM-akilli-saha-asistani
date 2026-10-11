package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.response.ExcelIceAktarimSonucuYaniti;
import com.akillisaha.crm.entity.*;
import com.akillisaha.crm.enums.NumuneDurumu;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.enums.TeklifDurumu;
import com.akillisaha.crm.exception.GecersizIstekHatasi;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.repository.*;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.impl.ExcelServisiImpl;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ExcelServisi (ExcelServisiImpl) birim test sınıfı.
 * 17 kolonluk Apache POI SXSSFWorkbook dışa aktarım (Export) ve
 * toplu içe aktarım (Import), veri izolasyonu ve hata toleransı kurallarını test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@ExtendWith(MockitoExtension.class)
class ExcelServisiTest {

    @Mock
    private ZiyaretRepository ziyaretRepository;

    @Mock
    private FirmaRepository firmaRepository;

    @Mock
    private FirmaYetkiliRepository yetkiliRepository;

    @Mock
    private KullaniciRepository kullaniciRepository;

    @Mock
    private NumuneRepository numuneRepository;

    @Mock
    private TeklifRepository teklifRepository;

    @InjectMocks
    private ExcelServisiImpl excelServisi;

    private Kullanici ornekPlasiyer;
    private Kullanici ornekYonetici;
    private OzelKullaniciDetaylari plasiyerDetaylari;
    private OzelKullaniciDetaylari yoneticiDetaylari;
    private Firma ornekFirma;
    private FirmaYetkili ornekYetkili;
    private Ziyaret ornekZiyaret;

    @BeforeEach
    void hazirla() {
        ornekPlasiyer = Kullanici.builder()
                .id(1L)
                .eposta("plasiyer@saha.com")
                .sifreOzeti("Gizli123!")
                .adSoyad("Ahmet Saha")
                .rol(Rol.PLASIYER)
                .aktifMi(true)
                .olusturulmaTarihi(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        ornekYonetici = Kullanici.builder()
                .id(99L)
                .eposta("yonetici@saha.com")
                .sifreOzeti("Gizli123!")
                .adSoyad("Mehmet Yonetici")
                .rol(Rol.YONETICI)
                .aktifMi(true)
                .olusturulmaTarihi(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        plasiyerDetaylari = new OzelKullaniciDetaylari(ornekPlasiyer);
        yoneticiDetaylari = new OzelKullaniciDetaylari(ornekYonetici);

        ornekFirma = Firma.builder()
                .id(10L)
                .unvan("ABC Metal Sanayi Ltd. Şti.")
                .sehirBolge("Kocaeli / Gebze")
                .mevcutTedarikciRakip("Rakip Metal")
                .tedarikEttigiUrunler("Sac ve Profil")
                .aylikKullanimMiktari("50 Ton")
                .bizdenAldigiUrunler("Profil")
                .atananPlasiyer(ornekPlasiyer)
                .yetkililer(new ArrayList<>())
                .build();

        ornekYetkili = FirmaYetkili.builder()
                .id(100L)
                .firma(ornekFirma)
                .adSoyad("Ali Vural")
                .unvanGorev("Satın Alma Müdürü")
                .telefon("05551234567")
                .eposta("ali@abcmetal.com")
                .build();
        ornekFirma.getYetkililer().add(ornekYetkili);

        ornekZiyaret = Ziyaret.builder()
                .id(50L)
                .firma(ornekFirma)
                .yetkili(ornekYetkili)
                .plasiyer(ornekPlasiyer)
                .ziyaretTarihi(OffsetDateTime.of(2026, 5, 10, 14, 0, 0, 0, ZoneOffset.UTC))
                .ziyaretKonusu("Yeni Fiyat Teklifi ve Numune Sunumu")
                .tedarikEttigiUrunler("Sac ve Profil")
                .aylikKullanimMiktari("50 Ton")
                .bizdenAldigiUrunler("Profil")
                .mevcutTedarikciRakip("Rakip Metal")
                .numuneVerildiMi(true)
                .teklifVerildiMi(true)
                .sonrakiAksiyon("Teklif revizyonu takip edilecek")
                .sonrakiZiyaretTarihi(OffsetDateTime.of(2026, 5, 20, 10, 0, 0, 0, ZoneOffset.UTC))
                .notlar("Müşteri fiyat konusunda esneklik istedi.")
                .numuneler(new ArrayList<>())
                .teklifler(new ArrayList<>())
                .build();

        Numune numune = Numune.builder()
                .id(200L)
                .ziyaret(ornekZiyaret)
                .firma(ornekFirma)
                .plasiyer(ornekPlasiyer)
                .urunAdi("Özel Kaplamalı Profil")
                .miktar("2 ADET")
                .durum(NumuneDurumu.TEST_ASAMASINDA)
                .sonucNotlari("Laboratuvar testi devam ediyor")
                .build();
        ornekZiyaret.getNumuneler().add(numune);

        Teklif teklif = Teklif.builder()
                .id(300L)
                .ziyaret(ornekZiyaret)
                .firma(ornekFirma)
                .plasiyer(ornekPlasiyer)
                .baslik("Profil Alımı Teklifi")
                .tutar(new BigDecimal("125000.50"))
                .paraBirimi("TRY")
                .durum(TeklifDurumu.ACIK)
                .build();
        ornekZiyaret.getTeklifler().add(teklif);
    }

    // ==========================================
    // EXPORT (Dışa Aktarma) TESTLERİ
    // ==========================================

    @Test
    @DisplayName("Dışa Aktar: Plasiyer rolünde sadece kendi ziyaretleri çekilmelidir")
    void ziyaretleriExceleAktar_PlasiyerRolunde_SadeceKendiZiyaretleriniCeker() throws IOException {
        // Arrange
        when(ziyaretRepository.findByPlasiyerId(1L)).thenReturn(List.of(ornekZiyaret));

        // Act
        byte[] excelBytes = excelServisi.ziyaretleriExceleAktar(plasiyerDetaylari, null, null, null);

        // Assert
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
        verify(ziyaretRepository).findByPlasiyerId(1L);
        verify(ziyaretRepository, never()).findAll();

        // Excel dosyasının okunabilirliğini doğrula
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(excelBytes))) {
            assertEquals("Müşteri Ziyaretleri", workbook.getSheetAt(0).getSheetName());
            assertEquals(2, workbook.getSheetAt(0).getPhysicalNumberOfRows()); // 1 Başlık + 1 Veri
        }
    }

    @Test
    @DisplayName("Dışa Aktar: Yönetici plasiyer belirtmediğinde tüm ziyaretler çekilmelidir")
    void ziyaretleriExceleAktar_YoneticiRolundeVePlasiyerSecilmemisse_TumZiyaretleriCeker() {
        // Arrange
        when(ziyaretRepository.findAll()).thenReturn(List.of(ornekZiyaret));

        // Act
        byte[] excelBytes = excelServisi.ziyaretleriExceleAktar(yoneticiDetaylari, null, null, null);

        // Assert
        assertNotNull(excelBytes);
        verify(ziyaretRepository).findAll();
        verify(ziyaretRepository, never()).findByPlasiyerId(anyLong());
    }

    @Test
    @DisplayName("Dışa Aktar: Yönetici belirli bir plasiyeri filtrelediğinde sadece o plasiyerin verileri çekilmelidir")
    void ziyaretleriExceleAktar_YoneticiRolundeVePlasiyerSecilmisse_IlgiliPlasiyerinZiyaretleriniCeker() {
        // Arrange
        Long hedeflenenPlasiyerId = 5L;
        when(ziyaretRepository.findByPlasiyerId(hedeflenenPlasiyerId)).thenReturn(List.of(ornekZiyaret));

        // Act
        byte[] excelBytes = excelServisi.ziyaretleriExceleAktar(yoneticiDetaylari, hedeflenenPlasiyerId, null, null);

        // Assert
        assertNotNull(excelBytes);
        verify(ziyaretRepository).findByPlasiyerId(hedeflenenPlasiyerId);
        verify(ziyaretRepository, never()).findAll();
    }

    @Test
    @DisplayName("Dışa Aktar: Tarih filtresi verildiğinde aralık dışındaki ziyaretler filtrelenmelidir")
    void ziyaretleriExceleAktar_TarihFiltresiVarsa_AralikDisindakiZiyaretleriFiltreler() throws IOException {
        // Arrange
        Ziyaret eskiZiyaret = Ziyaret.builder()
                .firma(ornekFirma)
                .ziyaretTarihi(OffsetDateTime.of(2026, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .numuneler(Collections.emptyList())
                .teklifler(Collections.emptyList())
                .build();

        Ziyaret gecerliZiyaret = ornekZiyaret; // 2026-05-10

        Ziyaret gelecekZiyaret = Ziyaret.builder()
                .firma(ornekFirma)
                .ziyaretTarihi(OffsetDateTime.of(2026, 8, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .numuneler(Collections.emptyList())
                .teklifler(Collections.emptyList())
                .build();

        when(ziyaretRepository.findByPlasiyerId(1L)).thenReturn(List.of(eskiZiyaret, gecerliZiyaret, gelecekZiyaret));

        OffsetDateTime baslangic = OffsetDateTime.of(2026, 5, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime bitis = OffsetDateTime.of(2026, 5, 31, 23, 59, 59, 0, ZoneOffset.UTC);

        // Act
        byte[] excelBytes = excelServisi.ziyaretleriExceleAktar(plasiyerDetaylari, null, baslangic, bitis);

        // Assert
        assertNotNull(excelBytes);
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(excelBytes))) {
            Sheet sayfa = workbook.getSheetAt(0);
            // Sadece 1 başlık + 1 aralıktaki geçerli satır olmalı
            assertEquals(2, sayfa.getPhysicalNumberOfRows());
        }
    }

    @Test
    @DisplayName("Dışa Aktar: 17 kolon başlığı ve hücre değerleri kurumsal formata uygun yazılmalıdır")
    void ziyaretleriExceleAktar_Tum17KolonVeVeriAlanlariDogruYazilir() throws IOException {
        // Arrange
        when(ziyaretRepository.findByPlasiyerId(1L)).thenReturn(List.of(ornekZiyaret));

        // Act
        byte[] excelBytes = excelServisi.ziyaretleriExceleAktar(plasiyerDetaylari, null, null, null);

        // Assert
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(excelBytes))) {
            Sheet sayfa = workbook.getSheetAt(0);
            assertEquals("Müşteri Ziyaretleri", sayfa.getSheetName());

            // 1. Başlık Satırını (0) Test Et
            Row baslikSatiri = sayfa.getRow(0);
            assertEquals("Müşteri Adı", baslikSatiri.getCell(0).getStringCellValue());
            assertEquals("Ziyaret Edilen Kişi", baslikSatiri.getCell(1).getStringCellValue());
            assertEquals("Görevi / Departmanı", baslikSatiri.getCell(2).getStringCellValue());
            assertEquals("Bölge / Şehir", baslikSatiri.getCell(3).getStringCellValue());
            assertEquals("Ziyaret Tarihi", baslikSatiri.getCell(4).getStringCellValue());
            assertEquals("Tedarik Ettiği Ürünler", baslikSatiri.getCell(5).getStringCellValue());
            assertEquals("Aylık Kullanım Miktarı", baslikSatiri.getCell(6).getStringCellValue());
            assertEquals("Bizden Aldığı Ürünler", baslikSatiri.getCell(7).getStringCellValue());
            assertEquals("Mevcut Tedarikçi / Rakip", baslikSatiri.getCell(8).getStringCellValue());
            assertEquals("Ziyaret Konusu", baslikSatiri.getCell(9).getStringCellValue());
            assertEquals("Numune Verildi mi?", baslikSatiri.getCell(10).getStringCellValue());
            assertEquals("Numune Sonucu", baslikSatiri.getCell(11).getStringCellValue());
            assertEquals("Teklif Verildi mi?", baslikSatiri.getCell(12).getStringCellValue());
            assertEquals("Teklif Tutarı", baslikSatiri.getCell(13).getStringCellValue());
            assertEquals("Sonraki Aksiyon", baslikSatiri.getCell(14).getStringCellValue());
            assertEquals("Sonraki Ziyaret Tarihi", baslikSatiri.getCell(15).getStringCellValue());
            assertEquals("Notlar", baslikSatiri.getCell(16).getStringCellValue());

            // 2. Veri Satırını (1) Test Et
            Row veriSatiri = sayfa.getRow(1);
            assertEquals("ABC Metal Sanayi Ltd. Şti.", veriSatiri.getCell(0).getStringCellValue());
            assertEquals("Ali Vural", veriSatiri.getCell(1).getStringCellValue());
            assertEquals("Satın Alma Müdürü", veriSatiri.getCell(2).getStringCellValue());
            assertEquals("Kocaeli / Gebze", veriSatiri.getCell(3).getStringCellValue());
            assertEquals("2026-05-10", veriSatiri.getCell(4).getStringCellValue());
            assertEquals("Sac ve Profil", veriSatiri.getCell(5).getStringCellValue());
            assertEquals("50 Ton", veriSatiri.getCell(6).getStringCellValue());
            assertEquals("Profil", veriSatiri.getCell(7).getStringCellValue());
            assertEquals("Rakip Metal", veriSatiri.getCell(8).getStringCellValue());
            assertEquals("Yeni Fiyat Teklifi ve Numune Sunumu", veriSatiri.getCell(9).getStringCellValue());
            assertEquals("Evet", veriSatiri.getCell(10).getStringCellValue());
            assertTrue(veriSatiri.getCell(11).getStringCellValue().contains("TEST_ASAMASINDA"));
            assertEquals("Evet", veriSatiri.getCell(12).getStringCellValue());
            assertEquals(125000.50, veriSatiri.getCell(13).getNumericCellValue(), 0.001);
            assertEquals("Teklif revizyonu takip edilecek", veriSatiri.getCell(14).getStringCellValue());
            assertEquals("2026-05-20", veriSatiri.getCell(15).getStringCellValue());
            assertEquals("Müşteri fiyat konusunda esneklik istedi.", veriSatiri.getCell(16).getStringCellValue());
        }
    }

    // ==========================================
    // IMPORT (İçe Aktarma) TESTLERİ
    // ==========================================

    @Test
    @DisplayName("İçe Aktar: Yüklenen dosya boş ise GecersizIstekHatasi fırlatılmalıdır")
    void exceldenZiyaretleriIceAktar_DosyaBosOldugunda_GecersizIstekHatasiFirlatir() {
        // Arrange
        MockMultipartFile bosDosya = new MockMultipartFile("dosya", "bos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        // Act & Assert
        GecersizIstekHatasi hata = assertThrows(GecersizIstekHatasi.class,
                () -> excelServisi.exceldenZiyaretleriIceAktar(bosDosya, plasiyerDetaylari));

        assertTrue(hata.getMessage().contains("boş olamaz"));
    }

    @Test
    @DisplayName("İçe Aktar: Aktif kullanıcı sistemde bulunamazsa KaynakBulunamadiHatasi fırlatılmalıdır")
    void exceldenZiyaretleriIceAktar_KullaniciBulunamadiginda_KaynakBulunamadiHatasiFirlatir() {
        // Arrange
        MockMultipartFile dosya = new MockMultipartFile("dosya", "ziyaretler.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "test".getBytes());

        when(kullaniciRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(KaynakBulunamadiHatasi.class,
                () -> excelServisi.exceldenZiyaretleriIceAktar(dosya, plasiyerDetaylari));
    }

    @Test
    @DisplayName("İçe Aktar: Yeni firma, yetkili, numune ve teklif içeren satır başarıyla içe aktarılmalıdır")
    void exceldenZiyaretleriIceAktar_YeniFirmaVeNumuneVeTeklifle_BasariylaKaydeder() throws IOException {
        // Arrange
        byte[] excelBytes = ornekExcelDosyasiOlustur(
                List.<String[]>of(
                        new String[]{
                                "Hedef Plastik A.Ş.",   // 0: Müşteri Adı
                                "Mustafa Demir",        // 1: Ziyaret Edilen Kişi
                                "Fabrika Müdürü",       // 2: Görevi / Departmanı
                                "Bursa / Nilüfer",      // 3: Bölge / Şehir
                                "2026-06-15",           // 4: Ziyaret Tarihi
                                "Granül Hammadde",      // 5: Tedarik Ettiği Ürünler
                                "100 Ton",              // 6: Aylık Kullanım
                                "Yok",                  // 7: Bizden Aldığı
                                "X Plastik",            // 8: Rakip
                                "Yeni Numune Sunumu",   // 9: Ziyaret Konusu
                                "Evet",                 // 10: Numune Verildi mi?
                                "BEKLEMEDE",            // 11: Numune Sonucu
                                "Evet",                 // 12: Teklif Verildi mi?
                                "45.000,00 TL",         // 13: Teklif Tutarı
                                "Telefonla aranacak",   // 14: Sonraki Aksiyon
                                "2026-06-25",           // 15: Sonraki Tarih
                                "Olumlu karşılandı"     // 16: Notlar
                        }
                )
        );

        MockMultipartFile dosya = new MockMultipartFile("dosya", "ziyaretler.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(ornekPlasiyer));
        when(firmaRepository.findByUnvanIgnoreCase("Hedef Plastik A.Ş.")).thenReturn(Optional.empty());

        Firma kaydedilenFirma = Firma.builder()
                .id(20L)
                .unvan("Hedef Plastik A.Ş.")
                .yetkililer(new ArrayList<>())
                .build();
        when(firmaRepository.save(any(Firma.class))).thenReturn(kaydedilenFirma);

        FirmaYetkili kaydedilenYetkili = FirmaYetkili.builder()
                .id(201L)
                .firma(kaydedilenFirma)
                .adSoyad("Mustafa Demir")
                .build();
        when(yetkiliRepository.save(any(FirmaYetkili.class))).thenReturn(kaydedilenYetkili);

        Ziyaret kaydedilenZiyaret = Ziyaret.builder()
                .id(2001L)
                .firma(kaydedilenFirma)
                .yetkili(kaydedilenYetkili)
                .plasiyer(ornekPlasiyer)
                .build();
        when(ziyaretRepository.save(any(Ziyaret.class))).thenReturn(kaydedilenZiyaret);

        // Act
        ExcelIceAktarimSonucuYaniti sonuc = excelServisi.exceldenZiyaretleriIceAktar(dosya, plasiyerDetaylari);

        // Assert
        assertNotNull(sonuc);
        assertEquals(1, sonuc.getToplamSatirSayisi());
        assertEquals(1, sonuc.getYeniFirmaSayisi());
        assertEquals(1, sonuc.getOlusturulanZiyaretSayisi());
        assertEquals(1, sonuc.getOlusturulanNumuneSayisi());
        assertEquals(1, sonuc.getOlusturulanTeklifSayisi());
        assertTrue(sonuc.getHataMesajlari().isEmpty());

        verify(firmaRepository).save(any(Firma.class));
        verify(yetkiliRepository).save(any(FirmaYetkili.class));
        verify(ziyaretRepository).save(any(Ziyaret.class));
        verify(numuneRepository).save(any(Numune.class));
        verify(teklifRepository).save(any(Teklif.class));
    }

    @Test
    @DisplayName("İçe Aktar: Mevcut firma ve yetkili varsa tekrar oluşturulmadan ziyarete bağlanmalıdır")
    void exceldenZiyaretleriIceAktar_MevcutFirmaVeYetkiliVarsa_TekrarYaratmadanBaglar() throws IOException {
        // Arrange
        byte[] excelBytes = ornekExcelDosyasiOlustur(
                List.<String[]>of(
                        new String[]{
                                "ABC Metal Sanayi Ltd. Şti.", // Mevcut Firma
                                "Ali Vural",                  // Mevcut Yetkili
                                "Satın Alma Müdürü",
                                "Kocaeli",
                                "2026-07-01",
                                "", "", "", "", "Rutin Ziyaret",
                                "Hayır", "", "Hayır", "",
                                "2 ay sonra ziyaret", "2026-09-01", "Görüşüldü"
                        }
                )
        );

        MockMultipartFile dosya = new MockMultipartFile("dosya", "ziyaretler.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(ornekPlasiyer));
        when(firmaRepository.findByUnvanIgnoreCase("ABC Metal Sanayi Ltd. Şti.")).thenReturn(Optional.of(ornekFirma));

        Ziyaret kaydedilenZiyaret = Ziyaret.builder().id(5001L).build();
        when(ziyaretRepository.save(any(Ziyaret.class))).thenReturn(kaydedilenZiyaret);

        // Act
        ExcelIceAktarimSonucuYaniti sonuc = excelServisi.exceldenZiyaretleriIceAktar(dosya, plasiyerDetaylari);

        // Assert
        assertNotNull(sonuc);
        assertEquals(1, sonuc.getToplamSatirSayisi());
        assertEquals(0, sonuc.getYeniFirmaSayisi()); // Firma tekrar açılmamalı
        assertEquals(1, sonuc.getOlusturulanZiyaretSayisi());
        assertEquals(0, sonuc.getOlusturulanNumuneSayisi());
        assertEquals(0, sonuc.getOlusturulanTeklifSayisi());

        verify(firmaRepository, never()).save(any(Firma.class));
        verify(yetkiliRepository, never()).save(any(FirmaYetkili.class));
        verify(ziyaretRepository).save(any(Ziyaret.class));
        verify(numuneRepository, never()).save(any(Numune.class));
        verify(teklifRepository, never()).save(any(Teklif.class));
    }

    @Test
    @DisplayName("İçe Aktar: Firma unvanı boş olan satırlar atlanmalıdır")
    void exceldenZiyaretleriIceAktar_BosSatirlariAtlar() throws IOException {
        // Arrange
        byte[] excelBytes = ornekExcelDosyasiOlustur(
                List.<String[]>of(
                        new String[]{"", "Boş Satır", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""},
                        new String[]{"   ", "Yine Boş", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""}
                )
        );

        MockMultipartFile dosya = new MockMultipartFile("dosya", "ziyaretler.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(ornekPlasiyer));

        // Act
        ExcelIceAktarimSonucuYaniti sonuc = excelServisi.exceldenZiyaretleriIceAktar(dosya, plasiyerDetaylari);

        // Assert
        assertEquals(0, sonuc.getToplamSatirSayisi());
        assertEquals(0, sonuc.getOlusturulanZiyaretSayisi());
        verify(ziyaretRepository, never()).save(any());
    }

    @Test
    @DisplayName("İçe Aktar: Bir satırda hata çıktığında diğer satırların işlenmesine devam edilmelidir")
    void exceldenZiyaretleriIceAktar_SatirHatasiOldugunda_DigerSatirlariIslemeyeDevamEder() throws IOException {
        // Arrange
        byte[] excelBytes = ornekExcelDosyasiOlustur(
                List.<String[]>of(
                        // Satır 1: Başarılı
                        new String[]{"Firma 1", "Yetkili 1", "", "", "2026-05-01", "", "", "", "", "Konu 1", "Hayır", "", "Hayır", "", "", "", ""},
                        // Satır 2: DB save esnasında hata alacak
                        new String[]{"Firma 2", "Yetkili 2", "", "", "2026-05-02", "", "", "", "", "Konu 2", "Hayır", "", "Hayır", "", "", "", ""}
                )
        );

        MockMultipartFile dosya = new MockMultipartFile("dosya", "ziyaretler.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(ornekPlasiyer));

        Firma f1 = Firma.builder().id(11L).unvan("Firma 1").yetkililer(new ArrayList<>()).build();
        Firma f2 = Firma.builder().id(12L).unvan("Firma 2").yetkililer(new ArrayList<>()).build();

        when(firmaRepository.findByUnvanIgnoreCase("Firma 1")).thenReturn(Optional.of(f1));
        when(firmaRepository.findByUnvanIgnoreCase("Firma 2")).thenReturn(Optional.of(f2));

        // İlk ziyaret kaydedilir, ikinci ziyarette hata fırlatılır
        when(ziyaretRepository.save(any(Ziyaret.class)))
                .thenReturn(Ziyaret.builder().id(1L).build())
                .thenThrow(new RuntimeException("Veritabanı bağlantı hatası"));

        // Act
        ExcelIceAktarimSonucuYaniti sonuc = excelServisi.exceldenZiyaretleriIceAktar(dosya, plasiyerDetaylari);

        // Assert
        assertEquals(2, sonuc.getToplamSatirSayisi());
        assertEquals(1, sonuc.getOlusturulanZiyaretSayisi());
        assertEquals(1, sonuc.getHataMesajlari().size());
        assertTrue(sonuc.getHataMesajlari().get(0).contains("Satır 3 (Firma 2)"));
    }

    @Test
    @DisplayName("İçe Aktar: Bozuk veya Excel formatında olmayan dosya yüklendiğinde GecersizIstekHatasi fırlatılmalıdır")
    void exceldenZiyaretleriIceAktar_GecersizBozukExcelDosyasi_GecersizIstekHatasiFirlatir() {
        // Arrange
        byte[] bozukIcerik = "Bu bir excel dosyasi degil, duz metin".getBytes();
        MockMultipartFile dosya = new MockMultipartFile("dosya", "bozuk.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bozukIcerik);

        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(ornekPlasiyer));

        // Act & Assert
        GecersizIstekHatasi hata = assertThrows(GecersizIstekHatasi.class,
                () -> excelServisi.exceldenZiyaretleriIceAktar(dosya, plasiyerDetaylari));

        assertTrue(hata.getMessage().contains("Excel dosyası işlenirken hata oluştu"));
    }

    // ==========================================
    // TEST YARDIMCI METOTLARI
    // ==========================================

    private byte[] ornekExcelDosyasiOlustur(List<String[]> satirlar) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Sayfa1");

            // Başlık Satırı
            Row baslik = sheet.createRow(0);
            for (int i = 0; i < 17; i++) {
                baslik.createCell(i).setCellValue("Kolon " + i);
            }

            // Veri Satırları
            int satirIndeks = 1;
            for (String[] satirVerisi : satirlar) {
                Row row = sheet.createRow(satirIndeks++);
                for (int i = 0; i < satirVerisi.length && i < 17; i++) {
                    row.createCell(i).setCellValue(satirVerisi[i]);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
