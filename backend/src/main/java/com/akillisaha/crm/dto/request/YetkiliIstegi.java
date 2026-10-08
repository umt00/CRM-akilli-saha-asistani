package com.akillisaha.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Firmaya yeni bir yetkili kişi (satın almacı, aşçı vb.) ekleme istek DTO'su.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YetkiliIstegi {

    /**
     * Yetkilinin ekleneceği müşteri firmanın benzersiz kimliği.
     */
    @NotNull(message = "Firma seçimi zorunludur")
    private Long firmaId;

    /**
     * Yetkili kişinin tam adı ve soyadı.
     */
    @NotBlank(message = "Yetkili kişi adı zorunludur")
    @Size(max = 120, message = "Ad Soyad en fazla 120 karakter olabilir")
    private String adSoyad;

    /**
     * Yetkilinin görevi veya unvanı (Örn: Satın Alma Müdürü, Baş Şef).
     */
    @Size(max = 120, message = "Görev unvanı en fazla 120 karakter olabilir")
    private String unvanGorev;

    /**
     * İletişim telefonu.
     */
    private String telefon;

    /**
     * E-posta adresi.
     */
    private String eposta;

    /**
     * Yetkili hakkında özel notlar.
     */
    private String notlar;
}
