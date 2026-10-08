package com.akillisaha.crm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * İstemciden gelen parametreler iş kurallarına veya sözleşmeye uymadığında fırlatılan hata sınıfı.
 * HTTP 400 Bad Request durum kodu ile eşleşir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class GecersizIstekHatasi extends RuntimeException {

    /**
     * Hata mesajı ile özel istisna oluşturur.
     * 
     * @param mesaj Hata ayrıntısı
     */
    public GecersizIstekHatasi(String mesaj) {
        super(mesaj);
    }
}
