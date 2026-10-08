package com.akillisaha.crm.service.impl;

import com.akillisaha.crm.dto.request.ZiyaretIstegi;
import com.akillisaha.crm.dto.response.NumuneYaniti;
import com.akillisaha.crm.dto.response.TeklifYaniti;
import com.akillisaha.crm.dto.response.ZiyaretYaniti;
import com.akillisaha.crm.entity.*;
import com.akillisaha.crm.enums.NumuneDurumu;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.enums.TeklifDurumu;
import com.akillisaha.crm.exception.ErisimEngellendiHatasi;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.repository.*;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.ZiyaretServisi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Saha ziyareti iş kurallarını koordine eden servis sınıfı.
 * Ziyaret kaydedilirken numune ve tekliflerin otomatik oluşturulmasını (@Transactional) ACID güvencesiyle sağlar.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZiyaretServisiImpl implements ZiyaretServisi {

    private final ZiyaretRepository ziyaretRepository;
    private final FirmaRepository firmaRepository;
    private final FirmaYetkiliRepository yetkiliRepository;
    private final KullaniciRepository kullaniciRepository;
    private final NumuneRepository numuneRepository;
    private final TeklifRepository teklifRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ZiyaretYaniti> ziyaretleriGetir(OzelKullaniciDetaylari aktifKullanici, Long firmaId, Pageable sayfalama) {
        Page<Ziyaret> ziyaretler;

        if (firmaId != null) {
            firmayaErisimYetkisiDenetle(firmaId, aktifKullanici);
            ziyaretler = ziyaretRepository.findByFirmaId(firmaId, sayfalama);
        } else if (aktifKullanici.getRol() == Rol.YONETICI) {
            ziyaretler = ziyaretRepository.findAll(sayfalama);
        } else {
            // Veri İzolasyonu: Plasiyer yalnızca kendi ziyaretlerini görebilir
            ziyaretler = ziyaretRepository.findByPlasiyerId(aktifKullanici.getId(), sayfalama);
        }

        return ziyaretler.map(this::yanitaDonustur);
    }

    @Override
    @Transactional(readOnly = true)
    public ZiyaretYaniti ziyaretDetayiGetir(Long ziyaretId, OzelKullaniciDetaylari aktifKullanici) {
        Ziyaret ziyaret = ziyaretiBulVeYetkiyiDenetle(ziyaretId, aktifKullanici);
        return yanitaDonustur(ziyaret);
    }

    @Override
    @Transactional
    public ZiyaretYaniti ziyaretOlustur(ZiyaretIstegi istek, OzelKullaniciDetaylari aktifKullanici) {
        Firma firma = firmayaErisimYetkisiDenetle(istek.getFirmaId(), aktifKullanici);
        Kullanici plasiyer = kullaniciRepository.findById(aktifKullanici.getId())
                .orElseThrow(() -> new KaynakBulunamadiHatasi("Kullanıcı bulunamadı: " + aktifKullanici.getId()));

        FirmaYetkili yetkili = null;
        if (istek.getYetkiliId() != null) {
            yetkili = yetkiliRepository.findById(istek.getYetkiliId())
                    .orElseThrow(() -> new KaynakBulunamadiHatasi("Yetkili bulunamadı: " + istek.getYetkiliId()));
        }

        // 1. Ziyaret Nesnesini Kaydet
        Ziyaret ziyaret = Ziyaret.builder()
                .firma(firma)
                .yetkili(yetkili)
                .plasiyer(plasiyer)
                .ziyaretTarihi(istek.getZiyaretTarihi())
                .ziyaretKonusu(istek.getZiyaretKonusu())
                .tedarikEttigiUrunler(istek.getTedarikEttigiUrunler())
                .aylikKullanimMiktari(istek.getAylikKullanimMiktari())
                .bizdenAldigiUrunler(istek.getBizdenAldigiUrunler())
                .mevcutTedarikciRakip(istek.getMevcutTedarikciRakip())
                .numuneVerildiMi(Boolean.TRUE.equals(istek.getNumuneVerildiMi()))
                .teklifVerildiMi(Boolean.TRUE.equals(istek.getTeklifVerildiMi()))
                .sonrakiAksiyon(istek.getSonrakiAksiyon())
                .sonrakiZiyaretTarihi(istek.getSonrakiZiyaretTarihi())
                .notlar(istek.getNotlar())
                .build();

        Ziyaret kaydedilenZiyaret = ziyaretRepository.save(ziyaret);

        // 2. İş Kuralı: Numune Verildiyse Otomatik Numune Nesnesi Oluştur
        if (Boolean.TRUE.equals(istek.getNumuneVerildiMi()) && StringUtils.hasText(istek.getNumuneUrunAdi())) {
            Numune numune = Numune.builder()
                    .ziyaret(kaydedilenZiyaret)
                    .firma(firma)
                    .plasiyer(plasiyer)
                    .urunAdi(istek.getNumuneUrunAdi())
                    .marka(istek.getNumuneMarkasi())
                    .miktar(StringUtils.hasText(istek.getNumuneMiktari()) ? istek.getNumuneMiktari() : "1 ADET")
                    .durum(NumuneDurumu.BEKLEMEDE)
                    .sonucNotlari(istek.getNumuneSonucNotu())
                    .gonderimTarihi(kaydedilenZiyaret.getZiyaretTarihi())
                    .build();
            numuneRepository.save(numune);
            kaydedilenZiyaret.getNumuneler().add(numune);
            log.info("Ziyarete bağlı otomatik numune açıldı: ziyaretId={}, urun={}", kaydedilenZiyaret.getId(), numune.getUrunAdi());
        }

        // 3. İş Kuralı: Teklif Verildiyse Otomatik Teklif Nesnesi Oluştur
        if (Boolean.TRUE.equals(istek.getTeklifVerildiMi()) && StringUtils.hasText(istek.getTeklifBasligi())) {
            Teklif teklif = Teklif.builder()
                    .firma(firma)
                    .ziyaret(kaydedilenZiyaret)
                    .plasiyer(plasiyer)
                    .baslik(istek.getTeklifBasligi())
                    .tutar(istek.getTeklifTutari())
                    .paraBirimi(StringUtils.hasText(istek.getTeklifParaBirimi()) ? istek.getTeklifParaBirimi() : "TRY")
                    .durum(TeklifDurumu.ACIK)
                    .build();
            teklifRepository.save(teklif);
            kaydedilenZiyaret.getTeklifler().add(teklif);
            log.info("Ziyarete bağlı otomatik teklif açıldı: ziyaretId={}, baslik={}", kaydedilenZiyaret.getId(), teklif.getBaslik());
        }

        // 4. Müşteri Pazar Bilgilerini Ziyaret Bilgileriyle Senkronize Et
        firmaPazarBilgileriniGuncelle(firma, istek);

        log.info("Ziyaret başarıyla kaydedildi: ziyaretId={}, firma={}", kaydedilenZiyaret.getId(), firma.getUnvan());
        return yanitaDonustur(kaydedilenZiyaret);
    }

    @Override
    @Transactional
    public ZiyaretYaniti ziyaretGuncelle(Long ziyaretId, ZiyaretIstegi istek, OzelKullaniciDetaylari aktifKullanici) {
        Ziyaret ziyaret = ziyaretiBulVeYetkiyiDenetle(ziyaretId, aktifKullanici);

        if (istek.getYetkiliId() != null) {
            FirmaYetkili yetkili = yetkiliRepository.findById(istek.getYetkiliId())
                    .orElseThrow(() -> new KaynakBulunamadiHatasi("Yetkili bulunamadı: " + istek.getYetkiliId()));
            ziyaret.setYetkili(yetkili);
        }

        ziyaret.setZiyaretTarihi(istek.getZiyaretTarihi());
        ziyaret.setZiyaretKonusu(istek.getZiyaretKonusu());
        ziyaret.setTedarikEttigiUrunler(istek.getTedarikEttigiUrunler());
        ziyaret.setAylikKullanimMiktari(istek.getAylikKullanimMiktari());
        ziyaret.setBizdenAldigiUrunler(istek.getBizdenAldigiUrunler());
        ziyaret.setMevcutTedarikciRakip(istek.getMevcutTedarikciRakip());
        ziyaret.setNumuneVerildiMi(Boolean.TRUE.equals(istek.getNumuneVerildiMi()));
        ziyaret.setTeklifVerildiMi(Boolean.TRUE.equals(istek.getTeklifVerildiMi()));
        ziyaret.setSonrakiAksiyon(istek.getSonrakiAksiyon());
        ziyaret.setSonrakiZiyaretTarihi(istek.getSonrakiZiyaretTarihi());
        ziyaret.setNotlar(istek.getNotlar());

        Ziyaret guncellenenZiyaret = ziyaretRepository.save(ziyaret);
        return yanitaDonustur(guncellenenZiyaret);
    }

    @Override
    @Transactional
    public void ziyaretSil(Long ziyaretId, OzelKullaniciDetaylari aktifKullanici) {
        Ziyaret ziyaret = ziyaretiBulVeYetkiyiDenetle(ziyaretId, aktifKullanici);
        ziyaretRepository.delete(ziyaret);
        log.info("Ziyaret silindi: id={}", ziyaretId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZiyaretYaniti> vadesiGelenAjandaZiyaretleriniGetir(OzelKullaniciDetaylari aktifKullanici, OffsetDateTime sonTarih) {
        OffsetDateTime kiyasTarihi = sonTarih != null ? sonTarih : OffsetDateTime.now().plusDays(7);
        List<Ziyaret> vadesiGelenler = ziyaretRepository.vadesiGelenZiyaretleriGetir(kiyasTarihi);

        if (aktifKullanici.getRol() != Rol.YONETICI) {
            vadesiGelenler = vadesiGelenler.stream()
                    .filter(z -> z.getPlasiyer().getId().equals(aktifKullanici.getId()))
                    .collect(Collectors.toList());
        }

        return vadesiGelenler.stream().map(this::yanitaDonustur).collect(Collectors.toList());
    }

    private Firma firmayaErisimYetkisiDenetle(Long firmaId, OzelKullaniciDetaylari aktifKullanici) {
        Firma firma = firmaRepository.findById(firmaId)
                .orElseThrow(() -> new KaynakBulunamadiHatasi("Firma bulunamadı: " + firmaId));

        if (aktifKullanici.getRol() != Rol.YONETICI) {
            if (firma.getAtananPlasiyer() == null || !firma.getAtananPlasiyer().getId().equals(aktifKullanici.getId())) {
                throw new ErisimEngellendiHatasi("Bu firmaya ait ziyaret işlemine yetkiniz bulunmamaktadır");
            }
        }
        return firma;
    }

    private Ziyaret ziyaretiBulVeYetkiyiDenetle(Long ziyaretId, OzelKullaniciDetaylari aktifKullanici) {
        Ziyaret ziyaret = ziyaretRepository.findById(ziyaretId)
                .orElseThrow(() -> new KaynakBulunamadiHatasi("Ziyaret bulunamadı: " + ziyaretId));

        if (aktifKullanici.getRol() != Rol.YONETICI) {
            if (!ziyaret.getPlasiyer().getId().equals(aktifKullanici.getId())) {
                throw new ErisimEngellendiHatasi("Bu ziyarete erişim yetkiniz bulunmamaktadır");
            }
        }
        return ziyaret;
    }

    private void firmaPazarBilgileriniGuncelle(Firma firma, ZiyaretIstegi istek) {
        boolean degisti = false;
        if (StringUtils.hasText(istek.getTedarikEttigiUrunler())) {
            firma.setTedarikEttigiUrunler(istek.getTedarikEttigiUrunler());
            degisti = true;
        }
        if (StringUtils.hasText(istek.getAylikKullanimMiktari())) {
            firma.setAylikKullanimMiktari(istek.getAylikKullanimMiktari());
            degisti = true;
        }
        if (StringUtils.hasText(istek.getBizdenAldigiUrunler())) {
            firma.setBizdenAldigiUrunler(istek.getBizdenAldigiUrunler());
            degisti = true;
        }
        if (StringUtils.hasText(istek.getMevcutTedarikciRakip())) {
            firma.setMevcutTedarikciRakip(istek.getMevcutTedarikciRakip());
            degisti = true;
        }
        if (degisti) {
            firmaRepository.save(firma);
        }
    }

    private ZiyaretYaniti yanitaDonustur(Ziyaret ziyaret) {
        return ZiyaretYaniti.builder()
                .id(ziyaret.getId())
                .firmaId(ziyaret.getFirma().getId())
                .firmaAdi(ziyaret.getFirma().getUnvan())
                .sehirBolge(ziyaret.getFirma().getSehirBolge())
                .yetkiliId(ziyaret.getYetkili() != null ? ziyaret.getYetkili().getId() : null)
                .yetkiliAdi(ziyaret.getYetkili() != null ? ziyaret.getYetkili().getAdSoyad() : null)
                .yetkiliGorevi(ziyaret.getYetkili() != null ? ziyaret.getYetkili().getUnvanGorev() : null)
                .plasiyerId(ziyaret.getPlasiyer().getId())
                .plasiyerAdi(ziyaret.getPlasiyer().getAdSoyad())
                .ziyaretTarihi(ziyaret.getZiyaretTarihi())
                .ziyaretKonusu(ziyaret.getZiyaretKonusu())
                .tedarikEttigiUrunler(ziyaret.getTedarikEttigiUrunler())
                .aylikKullanimMiktari(ziyaret.getAylikKullanimMiktari())
                .bizdenAldigiUrunler(ziyaret.getBizdenAldigiUrunler())
                .mevcutTedarikciRakip(ziyaret.getMevcutTedarikciRakip())
                .numuneVerildiMi(ziyaret.getNumuneVerildiMi())
                .teklifVerildiMi(ziyaret.getTeklifVerildiMi())
                .sonrakiAksiyon(ziyaret.getSonrakiAksiyon())
                .sonrakiZiyaretTarihi(ziyaret.getSonrakiZiyaretTarihi())
                .notlar(ziyaret.getNotlar())
                .numuneler(ziyaret.getNumuneler().stream().map(this::numuneYanitinaDonustur).collect(Collectors.toList()))
                .teklifler(ziyaret.getTeklifler().stream().map(this::teklifYanitinaDonustur).collect(Collectors.toList()))
                .olusturulmaTarihi(ziyaret.getOlusturulmaTarihi())
                .build();
    }

    private NumuneYaniti numuneYanitinaDonustur(Numune numune) {
        return NumuneYaniti.builder()
                .id(numune.getId())
                .ziyaretId(numune.getZiyaret() != null ? numune.getZiyaret().getId() : null)
                .firmaId(numune.getFirma().getId())
                .firmaAdi(numune.getFirma().getUnvan())
                .plasiyerId(numune.getPlasiyer().getId())
                .plasiyerAdi(numune.getPlasiyer().getAdSoyad())
                .urunId(numune.getUrun() != null ? numune.getUrun().getId() : null)
                .urunAdi(numune.getUrunAdi())
                .marka(numune.getMarka())
                .miktar(numune.getMiktar())
                .durum(numune.getDurum())
                .sonucNotlari(numune.getSonucNotlari())
                .gonderimTarihi(numune.getGonderimTarihi())
                .degerlendirilmeTarihi(numune.getDegerlendirilmeTarihi())
                .olusturulmaTarihi(numune.getOlusturulmaTarihi())
                .build();
    }

    private TeklifYaniti teklifYanitinaDonustur(Teklif teklif) {
        return TeklifYaniti.builder()
                .id(teklif.getId())
                .firmaId(teklif.getFirma().getId())
                .firmaAdi(teklif.getFirma().getUnvan())
                .ziyaretId(teklif.getZiyaret() != null ? teklif.getZiyaret().getId() : null)
                .plasiyerId(teklif.getPlasiyer().getId())
                .plasiyerAdi(teklif.getPlasiyer().getAdSoyad())
                .baslik(teklif.getBaslik())
                .tutar(teklif.getTutar())
                .paraBirimi(teklif.getParaBirimi())
                .durum(teklif.getDurum())
                .notlar(teklif.getNotlar())
                .gecerlilikTarihi(teklif.getGecerlilikTarihi())
                .olusturulmaTarihi(teklif.getOlusturulmaTarihi())
                .build();
    }
}
