package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.TeklifIstegi;
import com.akillisaha.crm.dto.response.TeklifYaniti;
import com.akillisaha.crm.enums.TeklifDurumu;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Ticari fiyat teklifleri ve satış fırsatlarını yöneten servis arayüzü.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
public interface TeklifServisi {

    /**
     * Plasiyere göre sayfalı teklif listesini döner.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @param durum Teklif durumu filtresi (opsiyonel)
     * @param firmaId Firma filtresi (opsiyonel)
     * @param sayfalama Sayfalama parametresi
     * @return Sayfalı teklifler
     */
    Page<TeklifYaniti> teklifleriGetir(OzelKullaniciDetaylari aktifKullanici, TeklifDurumu durum, Long firmaId, Pageable sayfalama);

    /**
     * ID'ye göre teklif detayını döner.
     * 
     * @param teklifId Teklif kimliği
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Teklif detay yanıtı
     */
    TeklifYaniti teklifDetayiGetir(Long teklifId, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Yeni bir teklif kaydı açar.
     * 
     * @param istek Teklif form verileri
     * @param aktifKullanici İşlemi gerçekleştiren plasiyer
     * @return Oluşturulan teklif yanıtı
     */
    TeklifYaniti teklifOlustur(TeklifIstegi istek, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Teklif durumunu ('KAZANILDI', 'KAYBEDILDI' vb.) günceller.
     * 
     * @param teklifId Teklif kimliği
     * @param yeniDurum Yeni durum
     * @param aktifKullanici İşlemi gerçekleştiren kullanıcı
     * @return Güncellenmiş teklif yanıtı
     */
    TeklifYaniti teklifDurumuGuncelle(Long teklifId, TeklifDurumu yeniDurum, OzelKullaniciDetaylari aktifKullanici);

    /**
     * Teklifi siler.
     * 
     * @param teklifId Teklif kimliği
     * @param aktifKullanici İşlemi gerçekleştiren kullanıcı
     */
    void teklifSil(Long teklifId, OzelKullaniciDetaylari aktifKullanici);
}
