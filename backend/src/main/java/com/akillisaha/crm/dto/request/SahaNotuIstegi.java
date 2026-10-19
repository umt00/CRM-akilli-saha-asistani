package com.akillisaha.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * n8n veya WhatsApp botundan gelen ham saha notu isteği.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Getter
@Setter
public class SahaNotuIstegi {

    /**
     * Plasiyerin WhatsApp'tan yazdığı ham mesaj metni (Zorunlu).
     */
    @NotBlank(message = "Saha notu metni boş olamaz")
    private String hamMetin;

    /**
     * Mesajı gönderen plasiyerin telefon numarası (opsiyonel).
     */
    private String gonderenTel;

    /**
     * Mesajı gönderen plasiyerin sistem ID'si (opsiyonel).
     */
    private Long plasiyerId;

    /**
     * Mesajın sonundaki tetikleyici etiket (örn: "!", "#crm", opsiyonel).
     */
    private String tetikleyici;

    public SahaNotuIstegi() {
    }

    public SahaNotuIstegi(String hamMetin) {
        this.hamMetin = hamMetin;
    }
}
