package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.response.ExcelIceAktarimSonucuYaniti;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

/**
 * Apache POI kütüphanesini kullanarak 17 kolonluk saha şablonunda Excel dışa aktarma (Export)
 * ve sisteme toplu içe aktarma (Import) işlemlerini yöneten servis arayüzü.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
public interface ExcelServisi {

    /**
     * Filtrelenmiş ziyaret, firma ve numune verilerini 17 kolonluk kurumsal Excel (.xlsx) dosyası olarak üretir.
     * Düşük bellek tüketimi için SXSSFWorkbook akış mimarisini kullanır.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @param plasiyerId Plasiyer filtresi (Yönetici kullanabilir)
     * @param baslangicTarihi Başlangıç tarihi
     * @param bitisTarihi Bitiş tarihi
     * @return Üretilen .xlsx dosyasının bayt dizisi (byte[])
     */
    byte[] ziyaretleriExceleAktar(OzelKullaniciDetaylari aktifKullanici, Long plasiyerId, OffsetDateTime baslangicTarihi, OffsetDateTime bitisTarihi);

    /**
     * Sahadan gelen 17 kolonluk Excel dosyasını satır satır okuyarak firmaları,
     * yetkilileri, ziyaretleri ve numuneleri sisteme toplu aktarır.
     * 
     * @param dosya Yüklenen Excel dosyası
     * @param aktifKullanici İşlemi gerçekleştiren kullanıcı
     * @return İçe aktarım istatistikleri ve hata raporu
     */
    ExcelIceAktarimSonucuYaniti exceldenZiyaretleriIceAktar(MultipartFile dosya, OzelKullaniciDetaylari aktifKullanici);
}
