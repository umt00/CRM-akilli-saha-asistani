package com.akillisaha.crm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Aranan bir varlık (Firma, Ziyaret, Kullanıcı vb.) veritabanında bulunamadığında fırlatılan özel hata sınıfı.
 * HTTP 404 Not Found durum kodu ile eşleşir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class KaynakBulunamadiHatasi extends RuntimeException {

    /**
     * Hata mesajı ile özel istisna oluşturur.
     * 
     * @param mesaj Hata ayrıntısı
     */
    public KaynakBulunamadiHatasi(String mesaj) {
        super(mesaj);
    }
}
