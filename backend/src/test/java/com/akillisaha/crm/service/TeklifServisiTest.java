package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.TeklifIstegi;
import com.akillisaha.crm.dto.response.TeklifYaniti;
import com.akillisaha.crm.entity.Firma;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.entity.Teklif;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.enums.TeklifDurumu;
import com.akillisaha.crm.exception.ErisimEngellendiHatasi;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.repository.FirmaRepository;
import com.akillisaha.crm.repository.KullaniciRepository;
import com.akillisaha.crm.repository.TeklifRepository;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.impl.TeklifServisiImpl;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TeklifServisi (TeklifServisiImpl) birim test sınıfı.
 * Fiyat teklifleri, ticari fırsatlar, kazanıldı/kaybedildi durum güncellemeleri
 * ve plasiyer veri izolasyonunu test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@ExtendWith(MockitoExtension.class)
class TeklifServisiTest {

    @Mock
    private TeklifRepository teklifRepository;

    @Mock
    private FirmaRepository firmaRepository;

    @Mock
    private KullaniciRepository kullaniciRepository;

    @InjectMocks
    private TeklifServisiImpl teklifServisi;

    private Kullanici plasiyer1;
    private Kullanici plasiyer2;
    private OzelKullaniciDetaylari plasiyer1Detay;
    private OzelKullaniciDetaylari plasiyer2Detay;

    private Firma firma;
    private Teklif ornekTeklif;

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
                .unvan("Altın Ekmek Fabrikası")
                .atananPlasiyer(plasiyer1)
                .build();

        ornekTeklif = Teklif.builder()
                .id(200L)
                .firma(firma)
                .plasiyer(plasiyer1)
                .baslik("Aylık 5 Ton Maya Teklifi")
                .tutar(new BigDecimal("75000.00"))
                .paraBirimi("TRY")
                .durum(TeklifDurumu.ACIK)
                .olusturulmaTarihi(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Teklif Listeleme: Plasiyer yalnızca kendi tekliflerini görebilmeli")
    void teklifleriGetir_Plasiyer_YalnizcaKendiTeklifleriniDonmeli() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Teklif> sayfa = new PageImpl<>(List.of(ornekTeklif));

        when(teklifRepository.findByPlasiyerId(1L, pageable)).thenReturn(sayfa);

        Page<TeklifYaniti> sonuc = teklifServisi.teklifleriGetir(plasiyer1Detay, null, null, pageable);

        assertNotNull(sonuc);
        assertEquals(1, sonuc.getTotalElements());
        assertEquals("Aylık 5 Ton Maya Teklifi", sonuc.getContent().get(0).getBaslik());
        assertEquals(new BigDecimal("75000.00"), sonuc.getContent().get(0).getTutar());

        verify(teklifRepository, times(1)).findByPlasiyerId(1L, pageable);
    }

    @Test
    @DisplayName("Teklif Detayı: Plasiyer başka plasiyere ait teklife erişmeye çalıştığında ErisimEngellendiHatasi fırlatılmalı")
    void teklifDetayiGetir_BaskaPlasiyerinTeklifi_ErisimEngellendiHatasiFirlatmali() {
        when(teklifRepository.findById(200L)).thenReturn(Optional.of(ornekTeklif));

        assertThrows(ErisimEngellendiHatasi.class, () -> teklifServisi.teklifDetayiGetir(200L, plasiyer2Detay));
    }

    @Test
    @DisplayName("Teklif Oluştur: Geçerli istek verildiğinde yeni teklif ACIK statüsüyle kaydedilmeli")
    void teklifOlustur_GecerliIstek_BasariylaTeklifAcilmali() {
        TeklifIstegi istek = TeklifIstegi.builder()
                .firmaId(10L)
                .baslik("Yıllık Endüstriyel Margarin Teklifi")
                .tutar(new BigDecimal("250000.00"))
                .paraBirimi("TRY")
                .build();

        when(firmaRepository.findById(10L)).thenReturn(Optional.of(firma));
        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(plasiyer1));
        when(teklifRepository.save(any(Teklif.class))).thenAnswer(inv -> {
            Teklif t = inv.getArgument(0);
            t.setId(201L);
            return t;
        });

        TeklifYaniti yanit = teklifServisi.teklifOlustur(istek, plasiyer1Detay);

        assertNotNull(yanit);
        assertEquals(201L, yanit.getId());
        assertEquals("Yıllık Endüstriyel Margarin Teklifi", yanit.getBaslik());
        assertEquals(TeklifDurumu.ACIK, yanit.getDurum());
        assertEquals(new BigDecimal("250000.00"), yanit.getTutar());

        verify(teklifRepository, times(1)).save(any(Teklif.class));
    }

    @Test
    @DisplayName("Durum Güncelleme: Teklif KAZANILDI olarak işaretlenebilmeli")
    void teklifDurumuGuncelle_Kazanildi_DurumGuncellenmeli() {
        when(teklifRepository.findById(200L)).thenReturn(Optional.of(ornekTeklif));
        when(teklifRepository.save(any(Teklif.class))).thenAnswer(inv -> inv.getArgument(0));

        TeklifYaniti yanit = teklifServisi.teklifDurumuGuncelle(200L, TeklifDurumu.KAZANILDI, plasiyer1Detay);

        assertNotNull(yanit);
        assertEquals(TeklifDurumu.KAZANILDI, yanit.getDurum());
        verify(teklifRepository, times(1)).save(ornekTeklif);
    }

    @Test
    @DisplayName("Teklif Sil: Plasiyer kendi teklifini silebilmeli")
    void teklifSil_BasariylaSilinmeli() {
        when(teklifRepository.findById(200L)).thenReturn(Optional.of(ornekTeklif));

        teklifServisi.teklifSil(200L, plasiyer1Detay);

        verify(teklifRepository, times(1)).delete(ornekTeklif);
    }

    @Test
    @DisplayName("Teklif Bulunamadı: Olmayan ID çağrıldığında KaynakBulunamadiHatasi fırlatılmalı")
    void teklifDetayiGetir_Bulunamazsa_KaynakBulunamadiHatasiFirlatmali() {
        when(teklifRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(KaynakBulunamadiHatasi.class, () -> teklifServisi.teklifDetayiGetir(999L, plasiyer1Detay));
    }
}
