package com.akillisaha.crm.dto.request;

import com.akillisaha.crm.enums.NumuneDurumu;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Ziyaretten bağımsız veya doğrudan numune oluşturma istek DTO'su.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumuneIstegi {

    /**
     * Varsa bağlı ziyaret ID'si.
     */
    private Long ziyaretId;

    /**
     * Numunenin verildiği müşteri firma ID'si.
     */
    @NotNull(message = "Firma seçimi zorunludur")
    private Long firmaId;

    /**
     * Katalogdaki ürün kimliği (opsiyonel).
     */
    private Long urunId;

    /**
     * Numunesi verilen ürünün adı.
     */
    @NotBlank(message = "Ürün adı zorunludur")
    private String urunAdi;

    /**
     * Ürünün markası (Kerry, Cargill vb.).
     */
    private String marka;

    /**
     * Bırakılan miktar (Örn: 250 GR).
     */
    @NotBlank(message = "Miktar alanı zorunludur (örn: 250 GR)")
    private String miktar;

    /**
     * Başlangıç durumu (Varsayılan: BEKLEMEDE).
     */
    @Builder.Default
    private NumuneDurumu durum = NumuneDurumu.BEKLEMEDE;

    /**
     * Numunenin sonuç veya tadım notları.
     */
    private String sonucNotlari;

    /**
     * Numunenin müşteriye gönderim tarihi.
     */
    private OffsetDateTime gonderimTarihi;

    /**
     * Numunenin değerlendirilme tarihi.
     */
    private OffsetDateTime degerlendirilmeTarihi;
}
