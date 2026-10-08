package com.akillisaha.crm.service.impl;

import com.akillisaha.crm.dto.request.TeklifIstegi;
import com.akillisaha.crm.dto.response.TeklifYaniti;
import com.akillisaha.crm.entity.Firma;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.entity.Teklif;
import com.akillisaha.crm.entity.Ziyaret;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.enums.TeklifDurumu;
import com.akillisaha.crm.exception.ErisimEngellendiHatasi;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.repository.FirmaRepository;
import com.akillisaha.crm.repository.KullaniciRepository;
import com.akillisaha.crm.repository.TeklifRepository;
import com.akillisaha.crm.repository.ZiyaretRepository;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.TeklifServisi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ticari teklifler ve fırsatlar servisinin somut implementasyonu.
 * Plasiyer izolasyonu ve durum güncellemelerini yönetir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeklifServisiImpl implements TeklifServisi {

    private final TeklifRepository teklifRepository;
    private final FirmaRepository firmaRepository;
    private final ZiyaretRepository ziyaretRepository;
    private final KullaniciRepository kullaniciRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<TeklifYaniti> teklifleriGetir(OzelKullaniciDetaylari aktifKullanici, TeklifDurumu durum, Long firmaId, Pageable sayfalama) {
        Page<Teklif> teklifler;

        if (firmaId != null) {
            firmayaErisimYetkisiDenetle(firmaId, aktifKullanici);
            teklifler = teklifRepository.findByFirmaId(firmaId, sayfalama);
        } else if (aktifKullanici.getRol() == Rol.YONETICI) {
            if (durum != null) {
                teklifler = teklifRepository.findByDurum(durum, sayfalama);
            } else {
                teklifler = teklifRepository.findAll(sayfalama);
            }
        } else {
            // Veri İzolasyonu: Plasiyer yalnızca kendi tekliflerini görebilir
            teklifler = teklifRepository.findByPlasiyerId(aktifKullanici.getId(), sayfalama);
        }

        return teklifler.map(this::yanitaDonustur);
    }

    @Override
    @Transactional(readOnly = true)
    public TeklifYaniti teklifDetayiGetir(Long teklifId, OzelKullaniciDetaylari aktifKullanici) {
        Teklif teklif = teklifiBulVeYetkiyiDenetle(teklifId, aktifKullanici);
        return yanitaDonustur(teklif);
    }

    @Override
    @Transactional
    public TeklifYaniti teklifOlustur(TeklifIstegi istek, OzelKullaniciDetaylari aktifKullanici) {
        Firma firma = firmayaErisimYetkisiDenetle(istek.getFirmaId(), aktifKullanici);
        Kullanici plasiyer = kullaniciRepository.findById(aktifKullanici.getId())
                .orElseThrow(() -> new KaynakBulunamadiHatasi("Kullanıcı bulunamadı: " + aktifKullanici.getId()));

        Ziyaret ziyaret = null;
        if (istek.getZiyaretId() != null) {
            ziyaret = ziyaretRepository.findById(istek.getZiyaretId())
                    .orElseThrow(() -> new KaynakBulunamadiHatasi("Ziyaret bulunamadı: " + istek.getZiyaretId()));
        }

        Teklif teklif = Teklif.builder()
                .firma(firma)
                .ziyaret(ziyaret)
                .plasiyer(plasiyer)
                .baslik(istek.getBaslik())
                .tutar(istek.getTutar())
                .paraBirimi(istek.getParaBirimi() != null ? istek.getParaBirimi() : "TRY")
                .durum(istek.getDurum() != null ? istek.getDurum() : TeklifDurumu.ACIK)
                .notlar(istek.getNotlar())
                .gecerlilikTarihi(istek.getGecerlilikTarihi())
                .build();

        Teklif kaydedilenTeklif = teklifRepository.save(teklif);
        log.info("Yeni teklif açıldı: id={}, baslik={}, tutar={}", kaydedilenTeklif.getId(), kaydedilenTeklif.getBaslik(), kaydedilenTeklif.getTutar());
        return yanitaDonustur(kaydedilenTeklif);
    }

    @Override
    @Transactional
    public TeklifYaniti teklifDurumuGuncelle(Long teklifId, TeklifDurumu yeniDurum, OzelKullaniciDetaylari aktifKullanici) {
        Teklif teklif = teklifiBulVeYetkiyiDenetle(teklifId, aktifKullanici);
        teklif.setDurum(yeniDurum);
        Teklif guncellenenTeklif = teklifRepository.save(teklif);
        log.info("Teklif durumu güncellendi: id={}, durum={}", teklifId, yeniDurum);
        return yanitaDonustur(guncellenenTeklif);
    }

    @Override
    @Transactional
    public void teklifSil(Long teklifId, OzelKullaniciDetaylari aktifKullanici) {
        Teklif teklif = teklifiBulVeYetkiyiDenetle(teklifId, aktifKullanici);
        teklifRepository.delete(teklif);
        log.info("Teklif silindi: id={}", teklifId);
    }

    private Firma firmayaErisimYetkisiDenetle(Long firmaId, OzelKullaniciDetaylari aktifKullanici) {
        Firma firma = firmaRepository.findById(firmaId)
                .orElseThrow(() -> new KaynakBulunamadiHatasi("Firma bulunamadı: " + firmaId));

        if (aktifKullanici.getRol() != Rol.YONETICI) {
            if (firma.getAtananPlasiyer() == null || !firma.getAtananPlasiyer().getId().equals(aktifKullanici.getId())) {
                throw new ErisimEngellendiHatasi("Bu firmaya ait teklif işlemine yetkiniz bulunmamaktadır");
            }
        }
        return firma;
    }

    private Teklif teklifiBulVeYetkiyiDenetle(Long teklifId, OzelKullaniciDetaylari aktifKullanici) {
        Teklif teklif = teklifRepository.findById(teklifId)
                .orElseThrow(() -> new KaynakBulunamadiHatasi("Teklif bulunamadı: " + teklifId));

        if (aktifKullanici.getRol() != Rol.YONETICI) {
            if (!teklif.getPlasiyer().getId().equals(aktifKullanici.getId())) {
                throw new ErisimEngellendiHatasi("Bu teklife erişim yetkiniz bulunmamaktadır");
            }
        }
        return teklif;
    }

    private TeklifYaniti yanitaDonustur(Teklif teklif) {
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
