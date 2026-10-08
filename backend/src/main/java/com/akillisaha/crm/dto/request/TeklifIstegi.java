package com.akillisaha.crm.dto.request;

import com.akillisaha.crm.enums.TeklifDurumu;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Müşteriye yeni bir fiyat teklifi veya ticari fırsat açma istek DTO'su.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeklifIstegi {

    /**
     * Teklifin verildiği müşteri firma ID'si.
     */
    @NotNull(message = "Firma seçimi zorunludur")
    private Long firmaId;

    /**
     * Teklifin bağlı olduğu ziyaret ID'si (opsiyonel).
     */
    private Long ziyaretId;

    /**
     * Teklif başlığı veya ürün grubu özeti.
     */
    @NotBlank(message = "Teklif başlığı zorunludur")
    private String baslik;

    /**
     * Teklif parasal tutarı.
     */
    private BigDecimal tutar;

    /**
     * Para birimi (TRY, USD, EUR).
     */
    @Builder.Default
    private String paraBirimi = "TRY";

    /**
     * Teklif durumu (Varsayılan: ACIK).
     */
    @Builder.Default
    private TeklifDurumu durum = TeklifDurumu.ACIK;

    /**
     * Teklife dair plasiyer notları.
     */
    private String notlar;

    /**
     * Teklifin son geçerlilik tarihi.
     */
    private OffsetDateTime gecerlilikTarihi;
}
