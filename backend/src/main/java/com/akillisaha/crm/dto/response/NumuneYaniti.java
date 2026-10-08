package com.akillisaha.crm.dto.response;

import com.akillisaha.crm.enums.NumuneDurumu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Numune takip kartı bilgilerini istemciye ileten yanıt DTO.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumuneYaniti {

    /**
     * Numune benzersiz kimliği.
     */
    private Long id;

    /**
     * Bağlı ziyaretin kimliği (varsa).
     */
    private Long ziyaretId;

    /**
     * Numunenin verildiği firma kimliği.
     */
    private Long firmaId;

    /**
     * Firma unvanı.
     */
    private String firmaAdi;

    /**
     * Numuneyi bırakan plasiyerin kullanıcı ID'si.
     */
    private Long plasiyerId;

    /**
     * Plasiyerin adı ve soyadı.
     */
    private String plasiyerAdi;

    /**
     * Katalog ürün kimliği (varsa).
     */
    private Long urunId;

    /**
     * Numunesi verilen ürünün adı.
     */
    private String urunAdi;

    /**
     * Ürünün markası (Kerry, Cargill vb.).
     */
    private String marka;

    /**
     * Verilen miktar (Örn: 250 GR, 500 GR).
     */
    private String miktar;

    /**
     * Numunenin yaşam döngüsü durumu (BEKLEMEDE, BEGENDI vb.).
     */
    private NumuneDurumu durum;

    /**
     * Müşteri tadım/kullanım sonuç notları (Excel Kolon L).
     */
    private String sonucNotlari;

    /**
     * Numunenin müşteriye ulaştığı tarih.
     */
    private OffsetDateTime gonderimTarihi;

    /**
     * Numunenin onaylandığı/reddedildiği tarih.
     */
    private OffsetDateTime degerlendirilmeTarihi;

    /**
     * Kayıt tarihi.
     */
    private OffsetDateTime olusturulmaTarihi;
}
