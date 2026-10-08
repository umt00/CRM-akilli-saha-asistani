package com.akillisaha.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Saha ziyareti detaylarını, pazar analizini, bağlı numune ve tekliflerini içeren yanıt DTO.
 * 17 kolonluk Excel'in tüm verisini istemciye eksiksiz taşır.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZiyaretYaniti {

    /**
     * Ziyaretin benzersiz kimliği.
     */
    private Long id;

    /**
     * Ziyaret edilen firma ID'si.
     */
    private Long firmaId;

    /**
     * Firma unvanı (Excel Kolon A).
     */
    private String firmaAdi;

    /**
     * Firmanın bulunduğu şehir/bölge (Excel Kolon D).
     */
    private String sehirBolge;

    /**
     * Görüşülen yetkili kimliği.
     */
    private Long yetkiliId;

    /**
     * Görüşülen yetkili adı soyadı (Excel Kolon B).
     */
    private String yetkiliAdi;

    /**
     * Görüşülen yetkilinin görevi (Excel Kolon C).
     */
    private String yetkiliGorevi;

    /**
     * Ziyareti yapan plasiyerin kullanıcı ID'si.
     */
    private Long plasiyerId;

    /**
     * Plasiyer adı ve soyadı.
     */
    private String plasiyerAdi;

    /**
     * Ziyaret tarihi (Excel Kolon E).
     */
    private OffsetDateTime ziyaretTarihi;

    /**
     * Ziyaret konusu (Excel Kolon J).
     */
    private String ziyaretKonusu;

    /**
     * Görüşülen tedarik ürünleri (Excel Kolon F).
     */
    private String tedarikEttigiUrunler;

    /**
     * Aylık tüketim miktarı (Excel Kolon G).
     */
    private String aylikKullanimMiktari;

    /**
     * Bizden aldığı ürünler (Excel Kolon H).
     */
    private String bizdenAldigiUrunler;

    /**
     * Rakip tedarikçi bilgisi (Excel Kolon I).
     */
    private String mevcutTedarikciRakip;

    /**
     * Numune verildi mi bayrağı (Excel Kolon K).
     */
    private Boolean numuneVerildiMi;

    /**
     * Teklif verildi mi bayrağı (Excel Kolon M).
     */
    private Boolean teklifVerildiMi;

    /**
     * Sonraki aksiyon adımı (Excel Kolon O).
     */
    private String sonrakiAksiyon;

    /**
     * Sonraki randevu tarihi (Excel Kolon P).
     */
    private OffsetDateTime sonrakiZiyaretTarihi;

    /**
     * Ziyaret saha notları (Excel Kolon Q).
     */
    private String notlar;

    /**
     * Ziyarete bağlı açılan numunelerin listesi.
     */
    private List<NumuneYaniti> numuneler;

    /**
     * Ziyarete bağlı açılan tekliflerin listesi.
     */
    private List<TeklifYaniti> teklifler;

    /**
     * Sisteme kayıt tarihi.
     */
    private OffsetDateTime olusturulmaTarihi;
}
