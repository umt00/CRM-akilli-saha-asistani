package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.ZiyaretIstegi;
import com.akillisaha.crm.dto.response.ZiyaretYaniti;
import com.akillisaha.crm.entity.*;
import com.akillisaha.crm.enums.NumuneDurumu;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.enums.TeklifDurumu;
import com.akillisaha.crm.exception.ErisimEngellendiHatasi;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.repository.*;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.impl.ZiyaretServisiImpl;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ZiyaretServisi (ZiyaretServisiImpl) birim test sınıfı.
 * 30 saniyelik form iş kuralları, otomatik numune ve teklif açma,
 * pazar bilgisi senkronizasyonu ve randevu ajandasını test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@ExtendWith(MockitoExtension.class)
class ZiyaretServisiTest {

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
    private ZiyaretServisiImpl ziyaretServisi;

    private Kullanici plasiyer1;
    private Kullanici plasiyer2;
    private Kullanici yonetici;

    private OzelKullaniciDetaylari plasiyer1Detay;
    private OzelKullaniciDetaylari plasiyer2Detay;
    private OzelKullaniciDetaylari yoneticiDetay;

    private Firma plasiyer1Firmasi;
    private FirmaYetkili firmaYetkilisi;
    private Ziyaret ornekZiyaret;

    @BeforeEach
    void setUp() {
        plasiyer1 = Kullanici.builder()
                .id(1L)
                .eposta("ahmet@akillisaha.com")
                .adSoyad("Ahmet Plasiyer")
                .rol(Rol.PLASIYER)
                .build();

        plasiyer2 = Kullanici.builder()
                .id(2L)
                .eposta("mehmet@akillisaha.com")
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
        plasiyer2Detay = new OzelKullaniciDetaylari(plasiyer2);
        yoneticiDetay = new OzelKullaniciDetaylari(yonetici);

        plasiyer1Firmasi = Firma.builder()
                .id(10L)
                .unvan("Lezzet Pastanesi")
                .atananPlasiyer(plasiyer1)
                .yetkililer(new ArrayList<>())
                .build();

        firmaYetkilisi = FirmaYetkili.builder()
                .id(100L)
                .firma(plasiyer1Firmasi)
                .adSoyad("Canan Şef")
                .unvanGorev("Baş Aşçı")
                .build();

        ornekZiyaret = Ziyaret.builder()
                .id(50L)
                .firma(plasiyer1Firmasi)
                .yetkili(firmaYetkilisi)
                .plasiyer(plasiyer1)
                .ziyaretTarihi(OffsetDateTime.now())
                .ziyaretKonusu("Aroma tadımı ve teklif görüşmesi")
                .numuneVerildiMi(false)
                .teklifVerildiMi(false)
                .numuneler(new ArrayList<>())
                .teklifler(new ArrayList<>())
                .olusturulmaTarihi(OffsetDateTime.now())
                .build();
    }

    // =========================================================================
    // 1. ZİYARETLERİ LİSTELEME VE VERİ İZOLASYONU
    // =========================================================================

    @Test
    @DisplayName("Ziyaret Listeleme: Plasiyer yalnızca kendi ziyaretlerini görebilmeli")
    void ziyaretleriGetir_PlasiyerKullanicisi_YalnizcaKendiZiyaretleriniDonmeli() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Ziyaret> beklenenSayfa = new PageImpl<>(List.of(ornekZiyaret));

        when(ziyaretRepository.findByPlasiyerId(1L, pageable)).thenReturn(beklenenSayfa);

        Page<ZiyaretYaniti> sonuc = ziyaretServisi.ziyaretleriGetir(plasiyer1Detay, null, pageable);

        assertNotNull(sonuc);
        assertEquals(1, sonuc.getTotalElements());
        assertEquals("Aroma tadımı ve teklif görüşmesi", sonuc.getContent().get(0).getZiyaretKonusu());

        verify(ziyaretRepository, times(1)).findByPlasiyerId(1L, pageable);
        verify(ziyaretRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Ziyaret Listeleme: Yönetici tüm şirket ziyaretlerini görebilmeli")
    void ziyaretleriGetir_YoneticiKullanicisi_TumZiyaretleriDonmeli() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Ziyaret> beklenenSayfa = new PageImpl<>(List.of(ornekZiyaret));

        when(ziyaretRepository.findAll(pageable)).thenReturn(beklenenSayfa);

        Page<ZiyaretYaniti> sonuc = ziyaretServisi.ziyaretleriGetir(yoneticiDetay, null, pageable);

        assertNotNull(sonuc);
        assertEquals(1, sonuc.getTotalElements());
        verify(ziyaretRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Ziyaret Detayı: Plasiyer başka plasiyerin ziyaretine erişmeye çalışırsa ErisimEngellendiHatasi fırlatılmalı")
    void ziyaretDetayiGetir_BaskaPlasiyerinZiyareti_ErisimEngellendiHatasiFirlatmali() {
        // Ziyaret Plasiyer 1'e ait, istek Plasiyer 2'den geliyor:
        when(ziyaretRepository.findById(50L)).thenReturn(Optional.of(ornekZiyaret));

        assertThrows(ErisimEngellendiHatasi.class, () -> ziyaretServisi.ziyaretDetayiGetir(50L, plasiyer2Detay));
    }

    // =========================================================================
    // 2. OTOMATİK İŞ KURALLARI İLE ZİYARET OLUŞTURMA
    // =========================================================================

    @Test
    @DisplayName("İş Kuralı 1: Ziyarette numune verildiğinde otomatik Numune nesnesi üretilmeli ve BEKLEMEDE açılmalı")
    void ziyaretOlustur_NumuneVerildiyse_OtomatikNumuneNesnesiUretilmeli() {
        ZiyaretIstegi istek = ZiyaretIstegi.builder()
                .firmaId(10L)
                .yetkiliId(100L)
                .ziyaretTarihi(OffsetDateTime.now())
                .ziyaretKonusu("Yeni Çilek Aroması Tanıtımı")
                .numuneVerildiMi(true)
                .numuneUrunAdi("Kerry Çilek Aroması")
                .numuneMarkasi("Kerry")
                .numuneMiktari("500 GR")
                .numuneSonucNotu("Tadım için bırakıldı")
                .build();

        when(firmaRepository.findById(10L)).thenReturn(Optional.of(plasiyer1Firmasi));
        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(plasiyer1));
        when(yetkiliRepository.findById(100L)).thenReturn(Optional.of(firmaYetkilisi));
        when(ziyaretRepository.save(any(Ziyaret.class))).thenAnswer(inv -> {
            Ziyaret z = inv.getArgument(0);
            z.setId(60L);
            z.setNumuneler(new ArrayList<>());
            z.setTeklifler(new ArrayList<>());
            return z;
        });

        ZiyaretYaniti yanit = ziyaretServisi.ziyaretOlustur(istek, plasiyer1Detay);

        assertNotNull(yanit);
        assertEquals(60L, yanit.getId());
        assertTrue(yanit.getNumuneVerildiMi());

        // Otomatik Numune kaydedilmiş olmalı:
        verify(numuneRepository, times(1)).save(argThat(numune ->
                numune.getUrunAdi().equals("Kerry Çilek Aroması") &&
                numune.getMarka().equals("Kerry") &&
                numune.getDurum() == NumuneDurumu.BEKLEMEDE
        ));
    }

    @Test
    @DisplayName("İş Kuralı 2: Ziyarette teklif açıldığında otomatik Teklif nesnesi üretilmeli ve ACIK durumda kaydedilmeli")
    void ziyaretOlustur_TeklifVerildiyse_OtomatikTeklifNesnesiUretilmeli() {
        ZiyaretIstegi istek = ZiyaretIstegi.builder()
                .firmaId(10L)
                .ziyaretTarihi(OffsetDateTime.now())
                .ziyaretKonusu("Yıllık Tedarik Teklifi")
                .teklifVerildiMi(true)
                .teklifBasligi("10 Ton Kakao Yağı Teklifi")
                .teklifTutari(new BigDecimal("150000.00"))
                .teklifParaBirimi("TRY")
                .build();

        when(firmaRepository.findById(10L)).thenReturn(Optional.of(plasiyer1Firmasi));
        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(plasiyer1));
        when(ziyaretRepository.save(any(Ziyaret.class))).thenAnswer(inv -> {
            Ziyaret z = inv.getArgument(0);
            z.setId(70L);
            z.setNumuneler(new ArrayList<>());
            z.setTeklifler(new ArrayList<>());
            return z;
        });

        ZiyaretYaniti yanit = ziyaretServisi.ziyaretOlustur(istek, plasiyer1Detay);

        assertNotNull(yanit);
        assertTrue(yanit.getTeklifVerildiMi());

        // Otomatik Teklif kaydedilmiş olmalı:
        verify(teklifRepository, times(1)).save(argThat(teklif ->
                teklif.getBaslik().equals("10 Ton Kakao Yağı Teklifi") &&
                teklif.getTutar().compareTo(new BigDecimal("150000.00")) == 0 &&
                teklif.getDurum() == TeklifDurumu.ACIK
        ));
    }

    @Test
    @DisplayName("İş Kuralı 3: Ziyarette konuşulan rakip tedarikçi ve tüketim miktarı Firma ana kartına senkronize edilmeli")
    void ziyaretOlustur_PazarBilgileriVarsa_FirmaBilgileriGuncellenmeli() {
        ZiyaretIstegi istek = ZiyaretIstegi.builder()
                .firmaId(10L)
                .ziyaretTarihi(OffsetDateTime.now())
                .ziyaretKonusu("Pazar araştırması")
                .mevcutTedarikciRakip("Rakip Aroma Ltd.")
                .aylikKullanimMiktari("2 Ton")
                .build();

        when(firmaRepository.findById(10L)).thenReturn(Optional.of(plasiyer1Firmasi));
        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(plasiyer1));
        when(ziyaretRepository.save(any(Ziyaret.class))).thenAnswer(inv -> {
            Ziyaret z = inv.getArgument(0);
            z.setId(80L);
            z.setNumuneler(new ArrayList<>());
            z.setTeklifler(new ArrayList<>());
            return z;
        });

        ziyaretServisi.ziyaretOlustur(istek, plasiyer1Detay);

        // Firmanın güncellenip kaydedildiği doğrulanır:
        assertEquals("Rakip Aroma Ltd.", plasiyer1Firmasi.getMevcutTedarikciRakip());
        assertEquals("2 Ton", plasiyer1Firmasi.getAylikKullanimMiktari());
        verify(firmaRepository, times(1)).save(plasiyer1Firmasi);
    }

    @Test
    @DisplayName("Güvenlik: Başka plasiyere ait bir firmaya ziyaret girilmeye çalışılırsa ErisimEngellendiHatasi fırlatılmalı")
    void ziyaretOlustur_BaskaPlasiyerinFirmasinaZiyaretGirilirse_ErisimEngellendiHatasiFirlatmali() {
        ZiyaretIstegi istek = ZiyaretIstegi.builder()
                .firmaId(10L)
                .ziyaretTarihi(OffsetDateTime.now())
                .ziyaretKonusu("İzinsiz ziyaret")
                .build();

        // Firma Plasiyer 1'e ait, ancak istek Plasiyer 2'den geliyor:
        when(firmaRepository.findById(10L)).thenReturn(Optional.of(plasiyer1Firmasi));

        assertThrows(ErisimEngellendiHatasi.class, () -> ziyaretServisi.ziyaretOlustur(istek, plasiyer2Detay));
        verify(ziyaretRepository, never()).save(any());
    }

    // =========================================================================
    // 3. AJANDA VE SİLME TESTLERİ
    // =========================================================================

    @Test
    @DisplayName("Ajanda: Vadesi gelen ziyaretler plasiyere göre filtrelenmeli")
    void vadesiGelenAjandaZiyaretleriniGetir_PlasiyereGoreFiltrelenmeli() {
        Ziyaret plasiyer2Ziyareti = Ziyaret.builder()
                .id(90L)
                .plasiyer(plasiyer2)
                .firma(plasiyer1Firmasi)
                .sonrakiZiyaretTarihi(OffsetDateTime.now().plusDays(2))
                .numuneler(new ArrayList<>())
                .teklifler(new ArrayList<>())
                .build();

        when(ziyaretRepository.vadesiGelenZiyaretleriGetir(any(OffsetDateTime.class)))
                .thenReturn(List.of(ornekZiyaret, plasiyer2Ziyareti));

        List<ZiyaretYaniti> sonuc = ziyaretServisi.vadesiGelenAjandaZiyaretleriniGetir(plasiyer1Detay, null);

        assertNotNull(sonuc);
        // Sadece Plasiyer 1'in ziyareti dönmeli:
        assertEquals(1, sonuc.size());
        assertEquals(50L, sonuc.get(0).getId());
    }

    @Test
    @DisplayName("Ziyaret Sil: Plasiyer kendi ziyaretini başarıyla silebilmeli")
    void ziyaretSil_PlasiyerKendiZiyaretiniSiler_BasariylaSilinmeli() {
        when(ziyaretRepository.findById(50L)).thenReturn(Optional.of(ornekZiyaret));

        ziyaretServisi.ziyaretSil(50L, plasiyer1Detay);

        verify(ziyaretRepository, times(1)).delete(ornekZiyaret);
    }
}
