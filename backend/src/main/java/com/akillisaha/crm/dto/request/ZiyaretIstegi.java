package com.akillisaha.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Plasiyerin sahada 30 saniyede dolduracağı ziyaret formundan gelen istek DTO'su.
 * Ziyaret kaydının yanı sıra opsiyonel numune ve teklif alanlarını da içerir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZiyaretIstegi {

    /**
     * Ziyaret edilen müşteri firma ID'si (Excel Kolon A).
     */
    @NotNull(message = "Ziyaret edilen firma seçimi zorunludur")
    private Long firmaId;

    /**
     * Görüşülen yetkili kişi ID'si (Excel Kolon B).
     */
    private Long yetkiliId;

    /**
     * Ziyaretin yapıldığı tarih (Excel Kolon E).
     */
    @NotNull(message = "Ziyaret tarihi zorunludur")
    private OffsetDateTime ziyaretTarihi;

    /**
     * Ziyaretin temel konusu (Excel Kolon J).
     */
    @NotBlank(message = "Ziyaret konusu zorunludur")
    private String ziyaretKonusu;

    /**
     * Görüşülen tedarik ürünleri (Excel Kolon F).
     */
    private String tedarikEttigiUrunler;

    /**
     * Firmanın aylık kullanım miktarı (Excel Kolon G).
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
     * Görüşmede numune verildi mi bayrağı (Excel Kolon K).
     */
    @Builder.Default
    private Boolean numuneVerildiMi = false;

    /**
     * Görüşmede teklif verildi mi bayrağı (Excel Kolon M).
     */
    @Builder.Default
    private Boolean teklifVerildiMi = false;

    /**
     * Sonraki planlanan aksiyon (Excel Kolon O).
     */
    private String sonrakiAksiyon;

    /**
     * Sonraki ziyaret tarihi / Ajanda randevusu (Excel Kolon P).
     */
    private OffsetDateTime sonrakiZiyaretTarihi;

    /**
     * Saha detay notları (Excel Kolon Q).
     */
    private String notlar;

    // --- ZİYARET ANINDA VERİLEN NUMUNE ALANLARI ---
    /**
     * Numunesi verilen ürünün adı (örn: Nacho Cheese, Çikolata Aroması).
     */
    private String numuneUrunAdi;

    /**
     * Numune üretici markası (Kerry, Cargill vb.).
     */
    private String numuneMarkasi;

    /**
     * Bırakılan miktar (Örn: 250 GR, 500 GR, 25 KG).
     */
    private String numuneMiktari;

    /**
     * Numuneye dair ilk gözlem veya tadım notu.
     */
    private String numuneSonucNotu;

    // --- ZİYARET ANINDA AÇILAN TEKLİF ALANLARI ---
    /**
     * Teklif başlığı/özeti.
     */
    private String teklifBasligi;

    /**
     * Teklif parasal tutarı.
     */
    private BigDecimal teklifTutari;

    /**
     * Teklif para birimi (TRY, USD, EUR).
     */
    private String teklifParaBirimi;
}
