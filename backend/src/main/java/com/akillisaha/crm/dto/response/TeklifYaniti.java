package com.akillisaha.crm.dto.response;

import com.akillisaha.crm.enums.TeklifDurumu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Ticari teklif detaylarını ve durumunu istemciye ileten yanıt DTO.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeklifYaniti {

    /**
     * Teklif benzersiz kimliği.
     */
    private Long id;

    /**
     * Teklifin verildiği firma ID'si.
     */
    private Long firmaId;

    /**
     * Firma unvanı.
     */
    private String firmaAdi;

    /**
     * Bağlı ziyaretin ID'si (varsa).
     */
    private Long ziyaretId;

    /**
     * Teklifi açan plasiyerin kullanıcı ID'si.
     */
    private Long plasiyerId;

    /**
     * Plasiyer adı ve soyadı.
     */
    private String plasiyerAdi;

    /**
     * Teklif başlığı/özeti.
     */
    private String baslik;

    /**
     * Teklif tutarı (Excel Kolon N).
     */
    private BigDecimal tutar;

    /**
     * Para birimi (TRY, USD, EUR).
     */
    private String paraBirimi;

    /**
     * Teklif durumu (ACIK, KAZANILDI, KAYBEDILDI, IPTAL).
     */
    private TeklifDurumu durum;

    /**
     * Teklif notları.
     */
    private String notlar;

    /**
     * Teklif geçerlilik tarihi.
     */
    private OffsetDateTime gecerlilikTarihi;

    /**
     * Kayıt tarihi.
     */
    private OffsetDateTime olusturulmaTarihi;
}
