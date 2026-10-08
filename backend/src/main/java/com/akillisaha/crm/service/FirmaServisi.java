package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.FirmaIstegi;
import com.akillisaha.crm.dto.request.YetkiliIstegi;
import com.akillisaha.crm.dto.response.FirmaYaniti;
import com.akillisaha.crm.dto.response.YetkiliYaniti;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Müşteri firma ve yetkili kişi yönetim iş mantığını tanımlayan servis arayüzü.
 * Veri izolasyonu kurallarını zorunlu tutar.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
public interface FirmaServisi {

    /**
     * Oturum açmış kullanıcının yetkisine göre sayfalanmış firma listesini döner.
     * Plasiyer ise sadece kendi firmalarını, yönetici ise tüm firmaları görür.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @param aramaMetni İsteğe bağlı filtreleme arama metni
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalı firma listesi
     */
    Page<FirmaYaniti> firmalariGetir(OzelKullaniciDetaylari aktifKullanici, String aramaMetni, Pageable sayfalama);

    /**
     * ID'ye göre tekil firma detayını ve bağlı yetkililerini getirir.
     * 
     * @param firmaId Firmanın benzersiz kimliği
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Firma detay yanıtı
     */
    FirmaYaniti firmaDetayiGetir(Long firmaId, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Yeni bir müşteri firma kaydı oluşturur.
     * 
     * @param istek Firma kayıt formu verileri
     * @param aktifKullanici İşlemi yapan kullanıcı
     * @return Oluşturulan firma yanıtı
     */
    FirmaYaniti firmaOlustur(FirmaIstegi istek, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Mevcut bir firmanın pazar ve iletişim bilgilerini günceller.
     * 
     * @param firmaId Güncellenecek firma ID'si
     * @param istek Güncelleme form verileri
     * @param aktifKullanici İşlemi yapan kullanıcı
     * @return Güncellenmiş firma yanıtı
     */
    FirmaYaniti firmaGuncelle(Long firmaId, FirmaIstegi istek, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Firmayı ve bağlı tüm yetkili, ziyaret ve numunelerini siler.
     * 
     * @param firmaId Silinecek firma ID'si
     * @param aktifKullanici İşlemi yapan kullanıcı
     */
    void firmaSil(Long firmaId, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Firmaya yeni bir yetkili kişi ekler.
     * 
     * @param firmaId Firmanın kimliği
     * @param istek Yetkili kişi form verileri
     * @param aktifKullanici İşlemi yapan kullanıcı
     * @return Eklenen yetkili yanıtı
     */
    YetkiliYaniti yetkiliEkle(Long firmaId, YetkiliIstegi istek, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Firmaya bağlı bir yetkili kişiyi siler.
     * 
     * @param firmaId Firma ID'si
     * @param yetkiliId Yetkili ID'si
     * @param aktifKullanici İşlemi yapan kullanıcı
     */
    void yetkiliSil(Long firmaId, Long yetkiliId, OzelKullaniciDetaylari aktifKullanici);
}
