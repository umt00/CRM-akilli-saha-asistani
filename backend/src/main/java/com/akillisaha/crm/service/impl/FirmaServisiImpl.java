package com.akillisaha.crm.service.impl;

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
import com.akillisaha.crm.service.FirmaServisi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

/**
 * Firma yönetim servisinin somut implementasyonu.
 * Rol tabanlı veri izolasyonunu (Sales Rep Data Isolation) garanti altına alır.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirmaServisiImpl implements FirmaServisi {

    private final FirmaRepository firmaRepository;
    private final FirmaYetkiliRepository yetkiliRepository;
    private final KullaniciRepository kullaniciRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<FirmaYaniti> firmalariGetir(OzelKullaniciDetaylari aktifKullanici, String aramaMetni, Pageable sayfalama) {
        Page<Firma> firmalar;

        if (aktifKullanici.getRol() == Rol.YONETICI) {
            if (StringUtils.hasText(aramaMetni)) {
                firmalar = firmaRepository.firmalardaAra(aramaMetni, sayfalama);
            } else {
                firmalar = firmaRepository.findAll(sayfalama);
            }
        } else {
            // Veri İzolasyonu: Plasiyer yalnızca kendisine atanan firmaları görebilir
            firmalar = firmaRepository.findByAtananPlasiyerId(aktifKullanici.getId(), sayfalama);
        }

        return firmalar.map(this::yanitaDonustur);
    }

    @Override
    @Transactional(readOnly = true)
    public FirmaYaniti firmaDetayiGetir(Long firmaId, OzelKullaniciDetaylari aktifKullanici) {
        Firma firma = firmayiBulVeYetkiyiDenetle(firmaId, aktifKullanici);
        return yanitaDonustur(firma);
    }

    @Override
    @Transactional
    public FirmaYaniti firmaOlustur(FirmaIstegi istek, OzelKullaniciDetaylari aktifKullanici) {
        Kullanici atananPlasiyer;
        if (aktifKullanici.getRol() == Rol.YONETICI && istek.getAtananPlasiyerId() != null) {
            atananPlasiyer = kullaniciRepository.findById(istek.getAtananPlasiyerId())
                    .orElseThrow(() -> new KaynakBulunamadiHatasi("Atanan kullanıcı bulunamadı: " + istek.getAtananPlasiyerId()));
        } else {
            atananPlasiyer = kullaniciRepository.findById(aktifKullanici.getId())
                    .orElseThrow(() -> new KaynakBulunamadiHatasi("Kullanıcı bulunamadı: " + aktifKullanici.getId()));
        }

        Firma firma = Firma.builder()
                .unvan(istek.getUnvan())
                .sehirBolge(istek.getSehirBolge())
                .adres(istek.getAdres())
                .telefon(istek.getTelefon())
                .eposta(istek.getEposta())
                .mevcutTedarikciRakip(istek.getMevcutTedarikciRakip())
                .tedarikEttigiUrunler(istek.getTedarikEttigiUrunler())
                .aylikKullanimMiktari(istek.getAylikKullanimMiktari())
                .bizdenAldigiUrunler(istek.getBizdenAldigiUrunler())
                .atananPlasiyer(atananPlasiyer)
                .build();

        Firma kaydedilenFirma = firmaRepository.save(firma);
        log.info("Yeni firma oluşturuldu: id={}, unvan={}", kaydedilenFirma.getId(), kaydedilenFirma.getUnvan());
        return yanitaDonustur(kaydedilenFirma);
    }

    @Override
    @Transactional
    public FirmaYaniti firmaGuncelle(Long firmaId, FirmaIstegi istek, OzelKullaniciDetaylari aktifKullanici) {
        Firma firma = firmayiBulVeYetkiyiDenetle(firmaId, aktifKullanici);

        firma.setUnvan(istek.getUnvan());
        firma.setSehirBolge(istek.getSehirBolge());
        firma.setAdres(istek.getAdres());
        firma.setTelefon(istek.getTelefon());
        firma.setEposta(istek.getEposta());
        firma.setMevcutTedarikciRakip(istek.getMevcutTedarikciRakip());
        firma.setTedarikEttigiUrunler(istek.getTedarikEttigiUrunler());
        firma.setAylikKullanimMiktari(istek.getAylikKullanimMiktari());
        firma.setBizdenAldigiUrunler(istek.getBizdenAldigiUrunler());

        if (aktifKullanici.getRol() == Rol.YONETICI && istek.getAtananPlasiyerId() != null) {
            Kullanici yeniPlasiyer = kullaniciRepository.findById(istek.getAtananPlasiyerId())
                    .orElseThrow(() -> new KaynakBulunamadiHatasi("Atanan kullanıcı bulunamadı: " + istek.getAtananPlasiyerId()));
            firma.setAtananPlasiyer(yeniPlasiyer);
        }

        Firma guncellenenFirma = firmaRepository.save(firma);
        return yanitaDonustur(guncellenenFirma);
    }

    @Override
    @Transactional
    public void firmaSil(Long firmaId, OzelKullaniciDetaylari aktifKullanici) {
        Firma firma = firmayiBulVeYetkiyiDenetle(firmaId, aktifKullanici);
        firmaRepository.delete(firma);
        log.info("Firma silindi: id={}", firmaId);
    }

    @Override
    @Transactional
    public YetkiliYaniti yetkiliEkle(Long firmaId, YetkiliIstegi istek, OzelKullaniciDetaylari aktifKullanici) {
        Firma firma = firmayiBulVeYetkiyiDenetle(firmaId, aktifKullanici);

        FirmaYetkili yetkili = FirmaYetkili.builder()
                .firma(firma)
                .adSoyad(istek.getAdSoyad())
                .unvanGorev(istek.getUnvanGorev())
                .telefon(istek.getTelefon())
                .eposta(istek.getEposta())
                .notlar(istek.getNotlar())
                .build();

        FirmaYetkili kaydedilenYetkili = yetkiliRepository.save(yetkili);
        return yetkiliYanitinaDonustur(kaydedilenYetkili);
    }

    @Override
    @Transactional
    public void yetkiliSil(Long firmaId, Long yetkiliId, OzelKullaniciDetaylari aktifKullanici) {
        firmayiBulVeYetkiyiDenetle(firmaId, aktifKullanici);
        FirmaYetkili yetkili = yetkiliRepository.findById(yetkiliId)
                .orElseThrow(() -> new KaynakBulunamadiHatasi("Yetkili bulunamadı: " + yetkiliId));
        yetkiliRepository.delete(yetkili);
    }

    /**
     * Veri izolasyonu denetimi: Kullanıcı yönetici değilse sadece kendisine atanan firmalara erişebilir.
     */
    private Firma firmayiBulVeYetkiyiDenetle(Long firmaId, OzelKullaniciDetaylari aktifKullanici) {
        Firma firma = firmaRepository.findById(firmaId)
                .orElseThrow(() -> new KaynakBulunamadiHatasi("Firma bulunamadı: " + firmaId));

        if (aktifKullanici.getRol() != Rol.YONETICI) {
            if (firma.getAtananPlasiyer() == null || !firma.getAtananPlasiyer().getId().equals(aktifKullanici.getId())) {
                log.warn("Erişim engellendi: Kullanıcı {} firma {} üzerinde yetkili değil", aktifKullanici.getId(), firmaId);
                throw new ErisimEngellendiHatasi("Bu firmaya erişim yetkiniz bulunmamaktadır");
            }
        }
        return firma;
    }

    private FirmaYaniti yanitaDonustur(Firma firma) {
        return FirmaYaniti.builder()
                .id(firma.getId())
                .unvan(firma.getUnvan())
                .sehirBolge(firma.getSehirBolge())
                .adres(firma.getAdres())
                .telefon(firma.getTelefon())
                .eposta(firma.getEposta())
                .mevcutTedarikciRakip(firma.getMevcutTedarikciRakip())
                .tedarikEttigiUrunler(firma.getTedarikEttigiUrunler())
                .aylikKullanimMiktari(firma.getAylikKullanimMiktari())
                .bizdenAldigiUrunler(firma.getBizdenAldigiUrunler())
                .atananPlasiyerId(firma.getAtananPlasiyer() != null ? firma.getAtananPlasiyer().getId() : null)
                .atananPlasiyerAdi(firma.getAtananPlasiyer() != null ? firma.getAtananPlasiyer().getAdSoyad() : null)
                .yetkililer(firma.getYetkililer().stream()
                        .map(this::yetkiliYanitinaDonustur)
                        .collect(Collectors.toList()))
                .olusturulmaTarihi(firma.getOlusturulmaTarihi())
                .guncellenmeTarihi(firma.getGuncellenmeTarihi())
                .build();
    }

    private YetkiliYaniti yetkiliYanitinaDonustur(FirmaYetkili yetkili) {
        return YetkiliYaniti.builder()
                .id(yetkili.getId())
                .firmaId(yetkili.getFirma().getId())
                .adSoyad(yetkili.getAdSoyad())
                .unvanGorev(yetkili.getUnvanGorev())
                .telefon(yetkili.getTelefon())
                .eposta(yetkili.getEposta())
                .notlar(yetkili.getNotlar())
                .olusturulmaTarihi(yetkili.getOlusturulmaTarihi())
                .build();
    }
}
