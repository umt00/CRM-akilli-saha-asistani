package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.ZiyaretIstegi;
import com.akillisaha.crm.dto.response.ZiyaretYaniti;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Saha ziyaretlerinin oluşturulması, listelenmesi, ajanda takibi ve otomatik
 * numune/teklif tetiklemelerini yöneten servis arayüzü.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
public interface ZiyaretServisi {

    /**
     * Plasiyere göre filtrelenmiş sayfalanabilir ziyaret listesini getirir.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @param firmaId Belirli bir firmaya göre filtreleme (opsiyonel)
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalı ziyaret listesi
     */
    Page<ZiyaretYaniti> ziyaretleriGetir(OzelKullaniciDetaylari aktifKullanici, Long firmaId, Pageable sayfalama);

    /**
     * Ziyaret detayını döner.
     * 
     * @param ziyaretId Ziyaret benzersiz kimliği
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Ziyaret detay yanıtı
     */
    ZiyaretYaniti ziyaretDetayiGetir(Long ziyaretId, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Yeni bir saha ziyareti kaydeder.
     * Ziyarette numune veya teklif işaretlendiyse tek bir işlemde (@Transactional) otomatik oluşturur.
     * 
     * @param istek Ziyaret form verileri
     * @param aktifKullanici İşlemi gerçekleştiren plasiyer
     * @return Oluşturulan ziyaret yanıtı
     */
    ZiyaretYaniti ziyaretOlustur(ZiyaretIstegi istek, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Ziyaret bilgilerini günceller.
     * 
     * @param ziyaretId Güncellenecek ziyaret ID'si
     * @param istek Yeni form verileri
     * @param aktifKullanici İşlemi gerçekleştiren kullanıcı
     * @return Güncellenmiş ziyaret yanıtı
     */
    ZiyaretYaniti ziyaretGuncelle(Long ziyaretId, ZiyaretIstegi istek, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Ziyaret kaydını siler.
     * 
     * @param ziyaretId Silinecek ziyaret ID'si
     * @param aktifKullanici İşlemi gerçekleştiren kullanıcı
     */
    void ziyaretSil(Long ziyaretId, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Ajanda: Sonraki ziyaret tarihi yaklaşan veya geciken randevuları listeler.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @param sonTarih Karşılaştırma tarihi (varsayılan: 7 gün sonrası)
     * @return Vadesi gelmiş ziyaretler
     */
    List<ZiyaretYaniti> vadesiGelenAjandaZiyaretleriniGetir(OzelKullaniciDetaylari aktifKullanici, OffsetDateTime sonTarih);
}
