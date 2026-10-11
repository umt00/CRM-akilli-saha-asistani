package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.NumuneIstegi;
import com.akillisaha.crm.dto.response.NumuneYaniti;
import com.akillisaha.crm.entity.Firma;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.entity.Numune;
import com.akillisaha.crm.enums.NumuneDurumu;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.exception.ErisimEngellendiHatasi;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.repository.*;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.impl.NumuneServisiImpl;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NumuneServisi (NumuneServisiImpl) birim test sınıfı.
 * Numune yaşam döngüsü, durum güncellemelerinde otomatik zaman damgası,
 * plasiyer izolasyonu ve marka istatistiklerini test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@ExtendWith(MockitoExtension.class)
class NumuneServisiTest {

    @Mock
    private NumuneRepository numuneRepository;

    @Mock
    private FirmaRepository firmaRepository;

    @Mock
    private ZiyaretRepository ziyaretRepository;

    @Mock
    private UrunRepository urunRepository;

    @Mock
    private KullaniciRepository kullaniciRepository;

    @InjectMocks
    private NumuneServisiImpl numuneServisi;

    private Kullanici plasiyer1;
    private Kullanici plasiyer2;
    private OzelKullaniciDetaylari plasiyer1Detay;
    private OzelKullaniciDetaylari plasiyer2Detay;

    private Firma firma;
    private Numune ornekNumune;

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

        plasiyer1Detay = new OzelKullaniciDetaylari(plasiyer1);
        plasiyer2Detay = new OzelKullaniciDetaylari(plasiyer2);

        firma = Firma.builder()
                .id(10L)
                .unvan("Tat Unlu Mamulleri")
                .atananPlasiyer(plasiyer1)
                .build();

        ornekNumune = Numune.builder()
                .id(100L)
                .firma(firma)
                .plasiyer(plasiyer1)
                .urunAdi("Kerry Vanilya Aroması")
                .marka("Kerry")
                .miktar("250 GR")
                .durum(NumuneDurumu.BEKLEMEDE)
                .gonderimTarihi(OffsetDateTime.now())
                .olusturulmaTarihi(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Numune Listeleme: Plasiyer yalnızca kendi numunelerini görebilmeli")
    void numuneleriGetir_Plasiyer_YalnizcaKendiNumuneleriniDonmeli() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Numune> sayfa = new PageImpl<>(List.of(ornekNumune));

        when(numuneRepository.findByPlasiyerId(1L, pageable)).thenReturn(sayfa);

        Page<NumuneYaniti> sonuc = numuneServisi.numuneleriGetir(plasiyer1Detay, null, null, pageable);

        assertNotNull(sonuc);
        assertEquals(1, sonuc.getTotalElements());
        assertEquals("Kerry Vanilya Aroması", sonuc.getContent().get(0).getUrunAdi());
        verify(numuneRepository, times(1)).findByPlasiyerId(1L, pageable);
    }

    @Test
    @DisplayName("Numune Listeleme: Durum filtresi verildiğinde ilgili durumdaki numuneler listelenmeli")
    void numuneleriGetir_DurumFiltresiVarsa_FiltrelenmisNumuneleriDonmeli() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Numune> sayfa = new PageImpl<>(List.of(ornekNumune));

        when(numuneRepository.findByPlasiyerIdAndDurum(1L, NumuneDurumu.BEKLEMEDE, pageable)).thenReturn(sayfa);

        Page<NumuneYaniti> sonuc = numuneServisi.numuneleriGetir(plasiyer1Detay, NumuneDurumu.BEKLEMEDE, null, pageable);

        assertNotNull(sonuc);
        assertEquals(1, sonuc.getTotalElements());
        verify(numuneRepository, times(1)).findByPlasiyerIdAndDurum(1L, NumuneDurumu.BEKLEMEDE, pageable);
    }

    @Test
    @DisplayName("Numune Detayı: Plasiyer başka plasiyerin numunesini görmeye çalışırsa ErisimEngellendiHatasi fırlatılmalı")
    void numuneDetayiGetir_BaskaPlasiyerinNumunesi_ErisimEngellendiHatasiFirlatmali() {
        when(numuneRepository.findById(100L)).thenReturn(Optional.of(ornekNumune));

        assertThrows(ErisimEngellendiHatasi.class, () -> numuneServisi.numuneDetayiGetir(100L, plasiyer2Detay));
    }

    @Test
    @DisplayName("Numune Oluştur: Geçerli bilgilerle numune başarıyla kaydedilmeli")
    void numuneOlustur_GecerliIstek_BasariylaNumuneKaydedilmeli() {
        NumuneIstegi istek = NumuneIstegi.builder()
                .firmaId(10L)
                .urunAdi("Cargill Kakao Tozu")
                .marka("Cargill")
                .miktar("500 GR")
                .build();

        when(firmaRepository.findById(10L)).thenReturn(Optional.of(firma));
        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(plasiyer1));
        when(numuneRepository.save(any(Numune.class))).thenAnswer(inv -> {
            Numune n = inv.getArgument(0);
            n.setId(101L);
            return n;
        });

        NumuneYaniti yanit = numuneServisi.numuneOlustur(istek, plasiyer1Detay);

        assertNotNull(yanit);
        assertEquals(101L, yanit.getId());
        assertEquals("Cargill Kakao Tozu", yanit.getUrunAdi());
        assertEquals(NumuneDurumu.BEKLEMEDE, yanit.getDurum());
        verify(numuneRepository, times(1)).save(any(Numune.class));
    }

    @Test
    @DisplayName("Durum Güncelleme: Numune BEGENDI veya SIPARISE_DONUSTU yapıldığında değerlendirilme tarihi otomatik damgalanmalı")
    void numuneDurumuGuncelle_DegerlendirilmeTarihiDamgalanmali() {
        when(numuneRepository.findById(100L)).thenReturn(Optional.of(ornekNumune));
        when(numuneRepository.save(any(Numune.class))).thenAnswer(inv -> inv.getArgument(0));

        NumuneYaniti yanit = numuneServisi.numuneDurumuGuncelle(
                100L,
                NumuneDurumu.BEGENDI,
                "Müşteri tadımı onayladı, sipariş verecek",
                plasiyer1Detay
        );

        assertNotNull(yanit);
        assertEquals(NumuneDurumu.BEGENDI, yanit.getDurum());
        assertEquals("Müşteri tadımı onayladı, sipariş verecek", yanit.getSonucNotlari());
        assertNotNull(ornekNumune.getDegerlendirilmeTarihi(), "Değerlendirilme tarihi otomatik set edilmelidir");
    }

    @Test
    @DisplayName("İstatistikler: Durum ve marka dağılımı haritası doğru hesaplanmalı")
    void numuneIstatistikleriniGetir_DurumVeMarkaIstatistikleriHesaplanmali() {
        when(numuneRepository.durumaGoreNumuneSayisi(any(NumuneDurumu.class))).thenReturn(2L);
        List<Object[]> markaDagilimi = new ArrayList<>();
        markaDagilimi.add(new Object[]{"Kerry", 5L});
        markaDagilimi.add(new Object[]{"Cargill", 3L});

        when(numuneRepository.markayaGoreNumuneSayilari()).thenReturn(markaDagilimi);

        Map<String, Long> istatistikler = numuneServisi.numuneIstatistikleriniGetir(plasiyer1Detay);

        assertNotNull(istatistikler);
        assertEquals(5L, istatistikler.get("MARKA_Kerry"));
        assertEquals(3L, istatistikler.get("MARKA_Cargill"));
        assertEquals(2L, istatistikler.get("BEKLEMEDE"));
    }

    @Test
    @DisplayName("Numune Sil: Plasiyer kendi numunesini başarıyla silebilmeli")
    void numuneSil_BasariylaSilinmeli() {
        when(numuneRepository.findById(100L)).thenReturn(Optional.of(ornekNumune));

        numuneServisi.numuneSil(100L, plasiyer1Detay);

        verify(numuneRepository, times(1)).delete(ornekNumune);
    }
}
