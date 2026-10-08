package com.akillisaha.crm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Bir kullanıcının veri izolasyonu kurallarını ihlal ederek başka bir plasiyere ait
 * müşteriye veya ziyarete erişmeye çalışması durumunda fırlatılan güvenlik hatası.
 * HTTP 403 Forbidden durum kodu ile eşleşir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ErisimEngellendiHatasi extends RuntimeException {

    /**
     * Hata mesajı ile özel istisna oluşturur.
     * 
     * @param mesaj Hata ayrıntısı
     */
    public ErisimEngellendiHatasi(String mesaj) {
        super(mesaj);
    }
}
