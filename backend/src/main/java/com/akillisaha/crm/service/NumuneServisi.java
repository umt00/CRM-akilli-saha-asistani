package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.NumuneIstegi;
import com.akillisaha.crm.dto.response.NumuneYaniti;
import com.akillisaha.crm.enums.NumuneDurumu;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Numune gönderimleri, durum güncellemeleri ve tadım takip iş mantığını yöneten servis arayüzü.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
public interface NumuneServisi {

    /**
     * Plasiyere özel filtrelenmiş numune listesini döner.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @param durum Numune durumu filtresi (opsiyonel)
     * @param firmaId Firma filtresi (opsiyonel)
     * @param sayfalama Sayfalama parametresi
     * @return Sayfalı numuneler
     */
    Page<NumuneYaniti> numuneleriGetir(OzelKullaniciDetaylari aktifKullanici, NumuneDurumu durum, Long firmaId, Pageable sayfalama);

    /**
     * ID'ye göre numune detayını getirir.
     * 
     * @param numuneId Numune kimliği
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Numune detay yanıtı
     */
    NumuneYaniti numuneDetayiGetir(Long numuneId, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Ziyaretten bağımsız doğrudan yeni bir numune kaydı oluşturur.
     * 
     * @param istek Numune formu verileri
     * @param aktifKullanici İşlemi gerçekleştiren plasiyer
     * @return Kaydedilen numune yanıtı
     */
    NumuneYaniti numuneOlustur(NumuneIstegi istek, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Numunenin durumunu ('BEGENDI', 'REDDETTI' vb.) ve müşteri sonuç notunu günceller.
     * 
     * @param numuneId Numune kimliği
     * @param yeniDurum Yeni durum
     * @param sonucNotlari Sonuç açıklaması
     * @param aktifKullanici İşlemi gerçekleştiren kullanıcı
     * @return Güncellenmiş numune yanıtı
     */
    NumuneYaniti numuneDurumuGuncelle(Long numuneId, NumuneDurumu yeniDurum, String sonucNotlari, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Numune kaydını siler.
     * 
     * @param numuneId Numune kimliği
     * @param aktifKullanici İşlemi gerçekleştiren kullanıcı
     */
    void numuneSil(Long numuneId, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Marka ve durumlara göre KPI dağılım sayılarını döner.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @return İstatistik haritası
     */
    Map<String, Long> numuneIstatistikleriniGetir(OzelKullaniciDetaylari aktifKullanici);
}
