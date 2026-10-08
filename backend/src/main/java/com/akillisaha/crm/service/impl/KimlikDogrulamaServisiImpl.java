package com.akillisaha.crm.service.impl;

import com.akillisaha.crm.dto.request.GirisIstegi;
import com.akillisaha.crm.dto.request.KayitIstegi;
import com.akillisaha.crm.dto.response.KimlikDogrulamaYaniti;
import com.akillisaha.crm.dto.response.KullaniciOzetiYaniti;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.exception.GecersizIstekHatasi;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.repository.KullaniciRepository;
import com.akillisaha.crm.security.JwtBelirteciSaglayici;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.KimlikDogrulamaServisi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kimlik doğrulama servisinin somut implementasyonu.
 * BCrypt şifreleme ve JWT belirteç entegrasyonunu yönetir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KimlikDogrulamaServisiImpl implements KimlikDogrulamaServisi {

    private final AuthenticationManager kimlikYoneticisi;
    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder parolaSifreleyici;
    private final JwtBelirteciSaglayici tokenSaglayici;

    @Override
    public KimlikDogrulamaYaniti girisYap(GirisIstegi istek) {
        Authentication kimlik = kimlikYoneticisi.authenticate(
                new UsernamePasswordAuthenticationToken(istek.getEposta(), istek.getSifre())
        );

        String token = tokenSaglayici.tokenUret(kimlik);
        OzelKullaniciDetaylari kullaniciDetaylari = (OzelKullaniciDetaylari) kimlik.getPrincipal();

        log.info("Kullanıcı başarıyla giriş yaptı: {}", kullaniciDetaylari.getUsername());

        return KimlikDogrulamaYaniti.builder()
                .token(token)
                .tokenTipi("Bearer")
                .kullaniciId(kullaniciDetaylari.getId())
                .eposta(kullaniciDetaylari.getUsername())
                .adSoyad(kullaniciDetaylari.getAdSoyad())
                .rol(kullaniciDetaylari.getRol())
                .build();
    }

    @Override
    @Transactional
    public KimlikDogrulamaYaniti kayitOl(KayitIstegi istek) {
        if (kullaniciRepository.existsByEposta(istek.getEposta())) {
            throw new GecersizIstekHatasi("Bu e-posta adresi ile kayıtlı kullanıcı zaten mevcut: " + istek.getEposta());
        }

        Kullanici yeniKullanici = Kullanici.builder()
                .eposta(istek.getEposta().toLowerCase().trim())
                .sifreOzeti(parolaSifreleyici.encode(istek.getSifre()))
                .adSoyad(istek.getAdSoyad())
                .rol(istek.getRol())
                .telefon(istek.getTelefon())
                .aktifMi(true)
                .build();

        Kullanici kaydedilenKullanici = kullaniciRepository.save(yeniKullanici);
        log.info("Yeni kullanıcı sisteme kaydedildi: {}", kaydedilenKullanici.getEposta());

        Authentication kimlik = kimlikYoneticisi.authenticate(
                new UsernamePasswordAuthenticationToken(istek.getEposta(), istek.getSifre())
        );

        String token = tokenSaglayici.tokenUret(kimlik);

        return KimlikDogrulamaYaniti.builder()
                .token(token)
                .tokenTipi("Bearer")
                .kullaniciId(kaydedilenKullanici.getId())
                .eposta(kaydedilenKullanici.getEposta())
                .adSoyad(kaydedilenKullanici.getAdSoyad())
                .rol(kaydedilenKullanici.getRol())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public KullaniciOzetiYaniti aktifKullaniciyiGetir(OzelKullaniciDetaylari aktifKullanici) {
        Kullanici kullanici = kullaniciRepository.findById(aktifKullanici.getId())
                .orElseThrow(() -> new KaynakBulunamadiHatasi("Kullanıcı bulunamadı: " + aktifKullanici.getId()));

        return KullaniciOzetiYaniti.builder()
                .id(kullanici.getId())
                .eposta(kullanici.getEposta())
                .adSoyad(kullanici.getAdSoyad())
                .rol(kullanici.getRol())
                .telefon(kullanici.getTelefon())
                .aktifMi(kullanici.getAktifMi())
                .olusturulmaTarihi(kullanici.getOlusturulmaTarihi())
                .build();
    }
}
