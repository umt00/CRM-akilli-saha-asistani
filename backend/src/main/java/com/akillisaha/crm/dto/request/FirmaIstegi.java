package com.akillisaha.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Yeni müşteri firma kaydetme veya mevcut firmayı güncelleme istek DTO'su.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirmaIstegi {

    /**
     * Müşteri firma resmi unvanı.
     */
    @NotBlank(message = "Firma unvanı zorunludur")
    @Size(max = 255, message = "Firma unvanı en fazla 255 karakter olabilir")
    private String unvan;

    /**
     * Şehir veya plasiyer bölgesi.
     */
    @Size(max = 100, message = "Bölge/Şehir en fazla 100 karakter olabilir")
    private String sehirBolge;

    /**
     * Açık adres bilgisi.
     */
    private String adres;

    /**
     * İletişim telefonu.
     */
    @Size(max = 50, message = "Telefon en fazla 50 karakter olabilir")
    private String telefon;

    /**
     * E-posta adresi.
     */
    private String eposta;

    /**
     * Excel Kolon I: Mevcut çalıştığı rakip tedarikçi.
     */
    private String mevcutTedarikciRakip;

    /**
     * Excel Kolon F: Dışarıdan tedarik ettiği ürünler.
     */
    private String tedarikEttigiUrunler;

    /**
     * Excel Kolon G: Aylık tahmini tüketim miktarı.
     */
    private String aylikKullanimMiktari;

    /**
     * Excel Kolon H: Halihazırda bizden aldığı ürünler.
     */
    private String bizdenAldigiUrunler;

    /**
     * Yönetici tarafından atanacak plasiyerin kullanıcı ID'si (boşsa oluşturan plasiyere atanır).
     */
    private Long atananPlasiyerId;
}
