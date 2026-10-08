package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.GirisIstegi;
import com.akillisaha.crm.dto.request.KayitIstegi;
import com.akillisaha.crm.dto.response.KimlikDogrulamaYaniti;
import com.akillisaha.crm.dto.response.KullaniciOzetiYaniti;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;

/**
 * Kullanıcı kimlik doğrulama, sisteme giriş, kayıt ve profil sorgulama işlemlerini tanımlayan servis arayüzü.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
public interface KimlikDogrulamaServisi {

    /**
     * E-posta ve şifre ile kullanıcı girişi yapar ve JWT belirteci üretir.
     * 
     * @param istek E-posta ve şifreyi içeren giriş isteği
     * @return Başarılı giriş yanıtı ve JWT token
     */
    KimlikDogrulamaYaniti girisYap(GirisIstegi istek);

    /**
     * Sisteme yeni bir plasiyer veya yönetici kullanıcısı kaydeder.
     * 
     * @param istek Kayıt formu verileri
     * @return Üretilen hesap ve JWT token
     */
    KimlikDogrulamaYaniti kayitOl(KayitIstegi istek);

    /**
     * Oturum açmış aktif kullanıcının profil ve rol bilgilerini döner.
     * 
     * @param aktifKullanici SecurityContext'ten alınan kullanıcı detayları
     * @return Kullanıcı profil özeti
     */
    KullaniciOzetiYaniti aktifKullaniciyiGetir(OzelKullaniciDetaylari aktifKullanici);
}
