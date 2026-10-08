package com.akillisaha.crm.service.impl;

import com.akillisaha.crm.dto.request.NumuneIstegi;
import com.akillisaha.crm.dto.response.NumuneYaniti;
import com.akillisaha.crm.entity.*;
import com.akillisaha.crm.enums.NumuneDurumu;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.exception.ErisimEngellendiHatasi;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.repository.*;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.NumuneServisi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Numune takibi servisinin somut implementasyonu.
 * Plasiyer izolasyonunu denetler ve tadım sonuç tarihlerini damgalar.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NumuneServisiImpl implements NumuneServisi {

    private final NumuneRepository numuneRepository;
    private final FirmaRepository firmaRepository;
    private final ZiyaretRepository ziyaretRepository;
    private final UrunRepository urunRepository;
    private final KullaniciRepository kullaniciRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<NumuneYaniti> numuneleriGetir(OzelKullaniciDetaylari aktifKullanici, NumuneDurumu durum, Long firmaId, Pageable sayfalama) {
        Page<Numune> numuneler;

        if (firmaId != null) {
            firmayaErisimYetkisiDenetle(firmaId, aktifKullanici);
            numuneler = numuneRepository.findByFirmaId(firmaId, sayfalama);
        } else if (aktifKullanici.getRol() == Rol.YONETICI) {
            if (durum != null) {
                numuneler = numuneRepository.findByDurum(durum, sayfalama);
            } else {
                numuneler = numuneRepository.findAll(sayfalama);
            }
        } else {
            // Veri İzolasyonu: Plasiyer yalnızca kendi numunelerini görebilir
            if (durum != null) {
                numuneler = numuneRepository.findByPlasiyerIdAndDurum(aktifKullanici.getId(), durum, sayfalama);
            } else {
                numuneler = numuneRepository.findByPlasiyerId(aktifKullanici.getId(), sayfalama);
            }
        }

        return numuneler.map(this::yanitaDonustur);
    }

    @Override
    @Transactional(readOnly = true)
    public NumuneYaniti numuneDetayiGetir(Long numuneId, OzelKullaniciDetaylari aktifKullanici) {
        Numune numune = numuneyiBulVeYetkiyiDenetle(numuneId, aktifKullanici);
        return yanitaDonustur(numune);
    }

    @Override
    @Transactional
    public NumuneYaniti numuneOlustur(NumuneIstegi istek, OzelKullaniciDetaylari aktifKullanici) {
        Firma firma = firmayaErisimYetkisiDenetle(istek.getFirmaId(), aktifKullanici);
        Kullanici plasiyer = kullaniciRepository.findById(aktifKullanici.getId())
                .orElseThrow(() -> new KaynakBulunamadiHatasi("Kullanıcı bulunamadı: " + aktifKullanici.getId()));

        Ziyaret ziyaret = null;
        if (istek.getZiyaretId() != null) {
            ziyaret = ziyaretRepository.findById(istek.getZiyaretId())
                    .orElseThrow(() -> new KaynakBulunamadiHatasi("Ziyaret bulunamadı: " + istek.getZiyaretId()));
        }

        Urun urun = null;
        if (istek.getUrunId() != null) {
            urun = urunRepository.findById(istek.getUrunId())
                    .orElseThrow(() -> new KaynakBulunamadiHatasi("Ürün bulunamadı: " + istek.getUrunId()));
        }

        Numune numune = Numune.builder()
                .firma(firma)
                .ziyaret(ziyaret)
                .plasiyer(plasiyer)
                .urun(urun)
                .urunAdi(istek.getUrunAdi())
                .marka(istek.getMarka())
                .miktar(istek.getMiktar())
                .durum(istek.getDurum() != null ? istek.getDurum() : NumuneDurumu.BEKLEMEDE)
                .sonucNotlari(istek.getSonucNotlari())
                .gonderimTarihi(istek.getGonderimTarihi() != null ? istek.getGonderimTarihi() : OffsetDateTime.now())
                .degerlendirilmeTarihi(istek.getDegerlendirilmeTarihi())
                .build();

        Numune kaydedilenNumune = numuneRepository.save(numune);
        log.info("Numune kaydı oluşturuldu: id={}, urun={}", kaydedilenNumune.getId(), kaydedilenNumune.getUrunAdi());
        return yanitaDonustur(kaydedilenNumune);
    }

    @Override
    @Transactional
    public NumuneYaniti numuneDurumuGuncelle(Long numuneId, NumuneDurumu yeniDurum, String sonucNotlari, OzelKullaniciDetaylari aktifKullanici) {
        Numune numune = numuneyiBulVeYetkiyiDenetle(numuneId, aktifKullanici);

        numune.setDurum(yeniDurum);
        if (sonucNotlari != null) {
            numune.setSonucNotlari(sonucNotlari);
        }
        if (yeniDurum != NumuneDurumu.BEKLEMEDE && yeniDurum != NumuneDurumu.TEST_ASAMASINDA) {
            numune.setDegerlendirilmeTarihi(OffsetDateTime.now());
        }

        Numune guncellenenNumune = numuneRepository.save(numune);
        log.info("Numune durumu güncellendi: id={}, durum={}", numuneId, yeniDurum);
        return yanitaDonustur(guncellenenNumune);
    }

    @Override
    @Transactional
    public void numuneSil(Long numuneId, OzelKullaniciDetaylari aktifKullanici) {
        Numune numune = numuneyiBulVeYetkiyiDenetle(numuneId, aktifKullanici);
        numuneRepository.delete(numune);
        log.info("Numune silindi: id={}", numuneId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> numuneIstatistikleriniGetir(OzelKullaniciDetaylari aktifKullanici) {
        Map<String, Long> istatistikler = new HashMap<>();
        for (NumuneDurumu durum : NumuneDurumu.values()) {
            istatistikler.put(durum.name(), numuneRepository.durumaGoreNumuneSayisi(durum));
        }

        List<Object[]> markaDagilimi = numuneRepository.markayaGoreNumuneSayilari();
        for (Object[] satir : markaDagilimi) {
            if (satir[0] != null) {
                istatistikler.put("MARKA_" + satir[0].toString(), (Long) satir[1]);
            }
        }
        return istatistikler;
    }

    private Firma firmayaErisimYetkisiDenetle(Long firmaId, OzelKullaniciDetaylari aktifKullanici) {
        Firma firma = firmaRepository.findById(firmaId)
                .orElseThrow(() -> new KaynakBulunamadiHatasi("Firma bulunamadı: " + firmaId));

        if (aktifKullanici.getRol() != Rol.YONETICI) {
            if (firma.getAtananPlasiyer() == null || !firma.getAtananPlasiyer().getId().equals(aktifKullanici.getId())) {
                throw new ErisimEngellendiHatasi("Bu firmaya ait numune işlemine yetkiniz bulunmamaktadır");
            }
        }
        return firma;
    }

    private Numune numuneyiBulVeYetkiyiDenetle(Long numuneId, OzelKullaniciDetaylari aktifKullanici) {
        Numune numune = numuneRepository.findById(numuneId)
                .orElseThrow(() -> new KaynakBulunamadiHatasi("Numune bulunamadı: " + numuneId));

        if (aktifKullanici.getRol() != Rol.YONETICI) {
            if (!numune.getPlasiyer().getId().equals(aktifKullanici.getId())) {
                throw new ErisimEngellendiHatasi("Bu numuneye erişim yetkiniz bulunmamaktadır");
            }
        }
        return numune;
    }

    private NumuneYaniti yanitaDonustur(Numune numune) {
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
}
