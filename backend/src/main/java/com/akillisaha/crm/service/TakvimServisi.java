package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.response.TakvimEtkinlikYaniti;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Ziyaretler, ajanda randevuları, numune tadım günleri ve teklif vadelerini
 * Bitrix24 tarzı birleşik takvim ve müşteri kartı formatında sunan servis arayüzü.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
public interface TakvimServisi {

    /**
     * Belirtilen tarih aralığında ve kullanıcının yetki alanındaki tüm CRM etkinliklerini döner.
     * 
     * @param aktifKullanici Oturum açan kullanıcı (Yönetici tüm ekibi, Plasiyer yalnızca kendini görür)
     * @param baslangic Aralığın başlangıç tarihi (boşsa ayın başı)
     * @param bitis Aralığın bitiş tarihi (boşsa ayın sonu)
     * @param turFiltresi Opsiyonel tür filtresi (RANDEVU, ZIYARET, NUMUNE, TEKLIF veya null)
     * @return Takvim etkinlik listesi
     */
    List<TakvimEtkinlikYaniti> takvimEtkinlikleriniGetir(
            OzelKullaniciDetaylari aktifKullanici,
            OffsetDateTime baslangic,
            OffsetDateTime bitis,
            String turFiltresi
    );
}
