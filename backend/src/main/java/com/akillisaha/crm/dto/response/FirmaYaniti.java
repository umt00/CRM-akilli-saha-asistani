package com.akillisaha.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Müşteri firma kartı ve pazar bilgilerini içeren yanıt DTO.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirmaYaniti {

    /**
     * Firmanın benzersiz kimliği.
     */
    private Long id;

    /**
     * Müşteri firma unvanı.
     */
    private String unvan;

    /**
     * Şehir veya satış bölgesi.
     */
    private String sehirBolge;

    /**
     * Açık adres.
     */
    private String adres;

    /**
     * İletişim telefonu.
     */
    private String telefon;

    /**
     * E-posta adresi.
     */
    private String eposta;

    /**
     * Mevcut tedarikçi / rakip bilgisi (Excel Kolon I).
     */
    private String mevcutTedarikciRakip;

    /**
     * Tedarik ettiği ürünler (Excel Kolon F).
     */
    private String tedarikEttigiUrunler;

    /**
     * Aylık kullanım miktarı (Excel Kolon G).
     */
    private String aylikKullanimMiktari;

    /**
     * Bizden aldığı ürünler (Excel Kolon H).
     */
    private String bizdenAldigiUrunler;

    /**
     * Firmadan sorumlu plasiyerin kullanıcı ID'si.
     */
    private Long atananPlasiyerId;

    /**
     * Firmadan sorumlu plasiyerin adı ve soyadı.
     */
    private String atananPlasiyerAdi;

    /**
     * Firmaya kayıtlı yetkili kişilerin listesi.
     */
    private List<YetkiliYaniti> yetkililer;

    /**
     * Kayıt oluşturulma tarihi.
     */
    private OffsetDateTime olusturulmaTarihi;

    /**
     * Son güncelleme tarihi.
     */
    private OffsetDateTime guncellenmeTarihi;
}
