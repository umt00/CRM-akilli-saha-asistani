package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.GirisIstegi;
import com.akillisaha.crm.dto.request.KayitIstegi;
import com.akillisaha.crm.dto.response.IslemSonucu;
import com.akillisaha.crm.dto.response.KimlikDogrulamaYaniti;
import com.akillisaha.crm.dto.response.KullaniciOzetiYaniti;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.KimlikDogrulamaServisi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Kullanıcı girişi, kayıt olma ve aktif oturum bilgilerini sunan REST API denetleyicisi.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@RestController
@RequestMapping("/api/v1/kimlik-dogrulama")
@RequiredArgsConstructor
@Tag(name = "Kimlik Doğrulama", description = "Giriş yapma, yeni hesap açma ve profil sorgulama uç noktaları")
public class KimlikDogrulamaDenetleyici {

    private final KimlikDogrulamaServisi kimlikDogrulamaServisi;

    /**
     * E-posta ve şifre ile sisteme giriş yapar.
     * 
     * @param istek Giriş istek formu
     * @return Üretilen JWT token ve kullanıcı profil bilgileri
     */
    @PostMapping("/giris")
    @Operation(summary = "Kullanıcı Girişi (Login)", description = "E-posta ve şifre ile oturum açarak JWT Bearer belirteci alır")
    public ResponseEntity<IslemSonucu<KimlikDogrulamaYaniti>> girisYap(@Valid @RequestBody GirisIstegi istek) {
        KimlikDogrulamaYaniti yanit = kimlikDogrulamaServisi.girisYap(istek);
        return ResponseEntity.ok(IslemSonucu.basarili("Giriş başarılı", yanit));
    }

    /**
     * Yeni bir plasiyer veya yönetici kullanıcısı kaydeder.
     * 
     * @param istek Kayıt form verileri
     * @return Kaydedilen hesap ve otomatik oluşturulan token
     */
    @PostMapping("/kayit")
    @Operation(summary = "Yeni Kullanıcı Kaydı (Register)", description = "Sisteme yeni satış temsilcisi veya yönetici hesabı tanımlar")
    public ResponseEntity<IslemSonucu<KimlikDogrulamaYaniti>> kayitOl(@Valid @RequestBody KayitIstegi istek) {
        KimlikDogrulamaYaniti yanit = kimlikDogrulamaServisi.kayitOl(istek);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(IslemSonucu.basarili("Kullanıcı başarıyla kaydedildi", yanit));
    }

    /**
     * Oturum açmış aktif kullanıcının kendi profil detaylarını döner.
     * 
     * @param aktifKullanici Token'dan çözümlenen oturum sahibi
     * @return Kullanıcı profil detayları
     */
    @GetMapping("/profilim")
    @Operation(summary = "Aktif Kullanıcı Profili", description = "Mevcut geçerli JWT belirteciyle oturum açmış kullanıcının bilgilerini döner")
    public ResponseEntity<IslemSonucu<KullaniciOzetiYaniti>> profilGetir(
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        KullaniciOzetiYaniti yanit = kimlikDogrulamaServisi.aktifKullaniciyiGetir(aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili(yanit));
    }
}
