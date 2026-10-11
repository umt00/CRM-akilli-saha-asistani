package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.FirmaIstegi;
import com.akillisaha.crm.dto.request.YetkiliIstegi;
import com.akillisaha.crm.dto.response.FirmaYaniti;
import com.akillisaha.crm.dto.response.YetkiliYaniti;
import com.akillisaha.crm.entity.Firma;
import com.akillisaha.crm.entity.FirmaYetkili;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.exception.ErisimEngellendiHatasi;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.repository.FirmaRepository;
import com.akillisaha.crm.repository.FirmaYetkiliRepository;
import com.akillisaha.crm.repository.KullaniciRepository;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.impl.FirmaServisiImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FirmaServisi (FirmaServisiImpl) birim test sınıfı.
 * Portföy yönetimi, arama, yetkili yönetimi ve en önemlisi
 * Rol Tabanlı Veri İzolasyonunu (Sales Rep Data Isolation) test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@ExtendWith(MockitoExtension.class)
class FirmaServisiTest {

    @Mock
    private FirmaRepository firmaRepository;

    @Mock
    private FirmaYetkiliRepository yetkiliRepository;

    @Mock
    private KullaniciRepository kullaniciRepository;

    @InjectMocks
    private FirmaServisiImpl firmaServisi;

    private Kullanici plasiyer1;
    private Kullanici plasiyer2;
    private Kullanici yonetici;

    private OzelKullaniciDetaylari plasiyer1Detay;
    private OzelKullaniciDetaylari yoneticiDetay;

    private Firma plasiyer1Firmasi;
    private Firma plasiyer2Firmasi;

    @BeforeEach
    void setUp() {
        plasiyer1 = Kullanici.builder()
                .id(1L)
                .eposta("plasiyer1@akillisaha.com")
                .adSoyad("Ahmet Plasiyer")
                .rol(Rol.PLASIYER)
                .build();

        plasiyer2 = Kullanici.builder()
                .id(2L)
                .eposta("plasiyer2@akillisaha.com")
                .adSoyad("Mehmet Plasiyer")
                .rol(Rol.PLASIYER)
                .build();

        yonetici = Kullanici.builder()
                .id(99L)
                .eposta("admin@akillisaha.com")
                .adSoyad("Genel Müdür Fatma")
                .rol(Rol.YONETICI)
                .build();

        plasiyer1Detay = new OzelKullaniciDetaylari(plasiyer1);
        yoneticiDetay = new OzelKullaniciDetaylari(yonetici);

        plasiyer1Firmasi = Firma.builder()
                .id(10L)
                .unvan("Lezzet Unlu Mamulleri")
                .sehirBolge("İstanbul Anadolu")
                .telefon("02161112233")
                .atananPlasiyer(plasiyer1)
                .yetkililer(new ArrayList<>())
                .olusturulmaTarihi(OffsetDateTime.now())
                .guncellenmeTarihi(OffsetDateTime.now())
                .build();

        plasiyer2Firmasi = Firma.builder()
                .id(20L)
                .unvan("Anadolu Gıda Ltd.")
                .sehirBolge("Ankara")
                .telefon("03125556677")
                .atananPlasiyer(plasiyer2)
                .yetkililer(new ArrayList<>())
                .olusturulmaTarihi(OffsetDateTime.now())
                .guncellenmeTarihi(OffsetDateTime.now())
                .build();
    }

    // =========================================================================
    // 1. FİRMALARI GETİR (VERİ İZOLASYONU VE ARAMA TESTLERİ)
    // =========================================================================

    @Test
    @DisplayName("Veri İzolasyonu: Plasiyer firmaları istediğinde YALNIZCA kendisine atanan firmalar sorgulanmalı")
    void firmalariGetir_PlasiyerKullanicisi_YalnizcaKendisineAtananFirmalariDonmeli() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Firma> beklenenSayfa = new PageImpl<>(List.of(plasiyer1Firmasi));

        when(firmaRepository.findByAtananPlasiyerId(1L, pageable)).thenReturn(beklenenSayfa);

        Page<FirmaYaniti> sonuc = firmaServisi.firmalariGetir(plasiyer1Detay, null, pageable);

        assertNotNull(sonuc);
        assertEquals(1, sonuc.getTotalElements());
        assertEquals("Lezzet Unlu Mamulleri", sonuc.getContent().get(0).getUnvan());

        // Plasiyer sorgusunda findAll asla çağrılmamalıdır!
        verify(firmaRepository, times(1)).findByAtananPlasiyerId(1L, pageable);
        verify(firmaRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Yönetici Yetkisi: Yönetici arama yapmadığında sistemdeki TÜM firmalar listelenmeli")
    void firmalariGetir_YoneticiAramaYoksa_TumFirmalariDonmeli() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Firma> beklenenSayfa = new PageImpl<>(List.of(plasiyer1Firmasi, plasiyer2Firmasi));

        when(firmaRepository.findAll(pageable)).thenReturn(beklenenSayfa);

        Page<FirmaYaniti> sonuc = firmaServisi.firmalariGetir(yoneticiDetay, "", pageable);

        assertNotNull(sonuc);
        assertEquals(2, sonuc.getTotalElements());

        verify(firmaRepository, times(1)).findAll(pageable);
        verify(firmaRepository, never()).findByAtananPlasiyerId(any(), any());
    }

    @Test
    @DisplayName("Arama: Yönetici arama metni girdiğinde firmalardaAra sorgusu çalışmalı")
    void firmalariGetir_YoneticiAramaMetniVarsa_AramaSonuclariniDonmeli() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Firma> beklenenSayfa = new PageImpl<>(List.of(plasiyer1Firmasi));

        when(firmaRepository.firmalardaAra("Lezzet", pageable)).thenReturn(beklenenSayfa);

        Page<FirmaYaniti> sonuc = firmaServisi.firmalariGetir(yoneticiDetay, "Lezzet", pageable);

        assertNotNull(sonuc);
        assertEquals(1, sonuc.getTotalElements());
        assertEquals("Lezzet Unlu Mamulleri", sonuc.getContent().get(0).getUnvan());

        verify(firmaRepository, times(1)).firmalardaAra("Lezzet", pageable);
    }

    // =========================================================================
    // 2. FİRMA DETAYI GETİR (IDOR GÜVENLİK TESTLERİ)
    // =========================================================================

    @Test
    @DisplayName("Firma Detayı: Plasiyer kendisine atanan firmanın detayını başarıyla görebilmeli")
    void firmaDetayiGetir_PlasiyerKendiFirmasi_BasariylaDonmeli() {
        when(firmaRepository.findById(10L)).thenReturn(Optional.of(plasiyer1Firmasi));

        FirmaYaniti yanit = firmaServisi.firmaDetayiGetir(10L, plasiyer1Detay);

        assertNotNull(yanit);
        assertEquals(10L, yanit.getId());
        assertEquals("Lezzet Unlu Mamulleri", yanit.getUnvan());
        assertEquals(1L, yanit.getAtananPlasiyerId());
        assertEquals("Ahmet Plasiyer", yanit.getAtananPlasiyerAdi());

        verify(firmaRepository, times(1)).findById(10L);
    }

    @Test
    @DisplayName("IDOR Engeli: Plasiyer başka plasiyere ait firmayı çağırdığında ErisimEngellendiHatasi fırlatılmalı")
    void firmaDetayiGetir_PlasiyerBaskaPlasiyerinFirmasinaErisirse_ErisimEngellendiHatasiFirlatmali() {
        // Plasiyer 1 (Ahmet), Plasiyer 2'ye (Mehmet) ait 20 ID'li firmayı çağırmaya çalışıyor:
        when(firmaRepository.findById(20L)).thenReturn(Optional.of(plasiyer2Firmasi));

        ErisimEngellendiHatasi hata = assertThrows(
                ErisimEngellendiHatasi.class,
                () -> firmaServisi.firmaDetayiGetir(20L, plasiyer1Detay)
        );

        assertTrue(hata.getMessage().contains("Bu firmaya erişim yetkiniz bulunmamaktadır"));
        verify(firmaRepository, times(1)).findById(20L);
    }

    @Test
    @DisplayName("Yönetici İstisnası: Yönetici başkasına atanmış olsa dahi herhangi bir firmanın detayını görebilmeli")
    void firmaDetayiGetir_YoneticiBaskasinaAtanmisFirmayi_Gorebilmeli() {
        when(firmaRepository.findById(20L)).thenReturn(Optional.of(plasiyer2Firmasi));

        FirmaYaniti yanit = firmaServisi.firmaDetayiGetir(20L, yoneticiDetay);

        assertNotNull(yanit);
        assertEquals("Anadolu Gıda Ltd.", yanit.getUnvan());
    }

    @Test
    @DisplayName("Hata Durumu: Olmayan firma ID'si çağrıldığında KaynakBulunamadiHatasi fırlatılmalı")
    void firmaDetayiGetir_FirmaBulunamazsa_KaynakBulunamadiHatasiFirlatmali() {
        when(firmaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(KaynakBulunamadiHatasi.class, () -> firmaServisi.firmaDetayiGetir(999L, plasiyer1Detay));
    }

    // =========================================================================
    // 3. FİRMA OLUŞTURMA TESTLERİ
    // =========================================================================

    @Test
    @DisplayName("Firma Oluştur: Plasiyer firma oluşturduğunda firma otomatik olarak kendisine atanmalı")
    void firmaOlustur_PlasiyerTarafindan_OtomatikKendisineAtanmali() {
        FirmaIstegi istek = FirmaIstegi.builder()
                .unvan("Yeni Tatlar Pastanesi")
                .sehirBolge("İzmir")
                .telefon("02321112233")
                .mevcutTedarikciRakip("Rakip Gıda A.Ş.")
                .aylikKullanimMiktari("500 kg")
                .build();

        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(plasiyer1));
        when(firmaRepository.save(any(Firma.class))).thenAnswer(inv -> {
            Firma f = inv.getArgument(0);
            f.setId(30L);
            f.setOlusturulmaTarihi(OffsetDateTime.now());
            f.setGuncellenmeTarihi(OffsetDateTime.now());
            return f;
        });

        FirmaYaniti yanit = firmaServisi.firmaOlustur(istek, plasiyer1Detay);

        assertNotNull(yanit);
        assertEquals(30L, yanit.getId());
        assertEquals("Yeni Tatlar Pastanesi", yanit.getUnvan());
        assertEquals(1L, yanit.getAtananPlasiyerId());
        assertEquals("Ahmet Plasiyer", yanit.getAtananPlasiyerAdi());

        verify(kullaniciRepository, times(1)).findById(1L);
        verify(firmaRepository, times(1)).save(any(Firma.class));
    }

    @Test
    @DisplayName("Firma Oluştur: Yönetici firma oluştururken başka bir plasiyere atayabilmeli")
    void firmaOlustur_YoneticiTarafindan_BelirtilenPlasiyereAtanmali() {
        FirmaIstegi istek = FirmaIstegi.builder()
                .unvan("Büyük Otel Mutfak")
                .atananPlasiyerId(2L) // Plasiyer 2'ye atanıyor
                .build();

        when(kullaniciRepository.findById(2L)).thenReturn(Optional.of(plasiyer2));
        when(firmaRepository.save(any(Firma.class))).thenAnswer(inv -> {
            Firma f = inv.getArgument(0);
            f.setId(40L);
            f.setOlusturulmaTarihi(OffsetDateTime.now());
            f.setGuncellenmeTarihi(OffsetDateTime.now());
            return f;
        });

        FirmaYaniti yanit = firmaServisi.firmaOlustur(istek, yoneticiDetay);

        assertNotNull(yanit);
        assertEquals(2L, yanit.getAtananPlasiyerId());
        assertEquals("Mehmet Plasiyer", yanit.getAtananPlasiyerAdi());

        verify(kullaniciRepository, times(1)).findById(2L);
    }

    // =========================================================================
    // 4. FİRMA GÜNCELLEME VE SİLME TESTLERİ
    // =========================================================================

    @Test
    @DisplayName("Firma Güncelle: Plasiyer kendi firmasının bilgilerini ve rakip tedarikçisini güncelleyebilmeli")
    void firmaGuncelle_PlasiyerKendiFirmasiniGunceller_BasariylaGuncellenmeli() {
        FirmaIstegi istek = FirmaIstegi.builder()
                .unvan("Lezzet Unlu Mamulleri - Güncel")
                .sehirBolge("İstanbul Anadolu")
                .mevcutTedarikciRakip("Yeni Rakip Kimya")
                .tedarikEttigiUrunler("Kakaolu Fındık Kreması")
                .build();

        when(firmaRepository.findById(10L)).thenReturn(Optional.of(plasiyer1Firmasi));
        when(firmaRepository.save(any(Firma.class))).thenAnswer(inv -> inv.getArgument(0));

        FirmaYaniti yanit = firmaServisi.firmaGuncelle(10L, istek, plasiyer1Detay);

        assertNotNull(yanit);
        assertEquals("Lezzet Unlu Mamulleri - Güncel", yanit.getUnvan());
        assertEquals("Yeni Rakip Kimya", yanit.getMevcutTedarikciRakip());
        verify(firmaRepository, times(1)).save(plasiyer1Firmasi);
    }

    @Test
    @DisplayName("Firma Sil: Plasiyer kendi firmasını silebilir")
    void firmaSil_PlasiyerKendiFirmasiniSiler_BasariylaSilinmeli() {
        when(firmaRepository.findById(10L)).thenReturn(Optional.of(plasiyer1Firmasi));

        firmaServisi.firmaSil(10L, plasiyer1Detay);

        verify(firmaRepository, times(1)).delete(plasiyer1Firmasi);
    }

    @Test
    @DisplayName("Yetkisiz Silme Engeli: Plasiyer başka plasiyere ait firmayı silememeli")
    void firmaSil_BaskaPlasiyerinFirmasiSilinmeyeCalisilirsa_ErisimEngellendiHatasiFirlatmali() {
        when(firmaRepository.findById(20L)).thenReturn(Optional.of(plasiyer2Firmasi));

        assertThrows(ErisimEngellendiHatasi.class, () -> firmaServisi.firmaSil(20L, plasiyer1Detay));
        verify(firmaRepository, never()).delete(any());
    }

    // =========================================================================
    // 5. YETKİLİ KİŞİ YÖNETİMİ TESTLERİ
    // =========================================================================

    @Test
    @DisplayName("Yetkili Ekle: Firmaya yeni yetkili (şef, satın almacı) başarıyla eklenmeli")
    void yetkiliEkle_FirmaMevcutsa_YeniYetkiliEklenmeli() {
        YetkiliIstegi istek = YetkiliIstegi.builder()
                .firmaId(10L)
                .adSoyad("Canan Şef")
                .unvanGorev("Baş Aşçı")
                .telefon("05321112233")
                .eposta("canan@lezzet.com")
                .notlar("Aroma testlerine sıcak bakıyor")
                .build();

        when(firmaRepository.findById(10L)).thenReturn(Optional.of(plasiyer1Firmasi));
        when(yetkiliRepository.save(any(FirmaYetkili.class))).thenAnswer(inv -> {
            FirmaYetkili y = inv.getArgument(0);
            y.setId(101L);
            y.setOlusturulmaTarihi(OffsetDateTime.now());
            return y;
        });

        YetkiliYaniti yanit = firmaServisi.yetkiliEkle(10L, istek, plasiyer1Detay);

        assertNotNull(yanit);
        assertEquals(101L, yanit.getId());
        assertEquals(10L, yanit.getFirmaId());
        assertEquals("Canan Şef", yanit.getAdSoyad());
        assertEquals("Baş Aşçı", yanit.getUnvanGorev());

        verify(yetkiliRepository, times(1)).save(any(FirmaYetkili.class));
    }

    @Test
    @DisplayName("Yetkili Sil: Mevcut yetkili kişi başarıyla silinmeli")
    void yetkiliSil_YetkiliMevcutsa_BasariylaSilinmeli() {
        FirmaYetkili yetkili = FirmaYetkili.builder()
                .id(101L)
                .firma(plasiyer1Firmasi)
                .adSoyad("Canan Şef")
                .build();

        when(firmaRepository.findById(10L)).thenReturn(Optional.of(plasiyer1Firmasi));
        when(yetkiliRepository.findById(101L)).thenReturn(Optional.of(yetkili));

        firmaServisi.yetkiliSil(10L, 101L, plasiyer1Detay);

        verify(yetkiliRepository, times(1)).delete(yetkili);
    }

    @Test
    @DisplayName("Yetkili Sil Hata: Olmayan yetkili silinmeye çalışıldığında KaynakBulunamadiHatasi fırlatılmalı")
    void yetkiliSil_YetkiliBulunamazsa_KaynakBulunamadiHatasiFirlatmali() {
        when(firmaRepository.findById(10L)).thenReturn(Optional.of(plasiyer1Firmasi));
        when(yetkiliRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(KaynakBulunamadiHatasi.class, () -> firmaServisi.yetkiliSil(10L, 999L, plasiyer1Detay));
        verify(yetkiliRepository, never()).delete(any());
    }
}
