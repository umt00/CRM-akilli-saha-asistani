package com.akillisaha.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Firma yetkilisi detaylarını istemciye ileten DTO.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YetkiliYaniti {

    /**
     * Yetkilinin benzersiz kimliği.
     */
    private Long id;

    /**
     * Bağlı olduğu firmanın kimliği.
     */
    private Long firmaId;

    /**
     * Yetkilinin adı ve soyadı.
     */
    private String adSoyad;

    /**
     * Yetkilinin görevi/departmanı (Şef, Satın Alma vb.).
     */
    private String unvanGorev;

    /**
     * Yetkilinin telefon numarası.
     */
    private String telefon;

    /**
     * Yetkilinin e-posta adresi.
     */
    private String eposta;

    /**
     * Yetkiliye dair özel notlar.
     */
    private String notlar;

    /**
     * Sisteme eklenme tarihi.
     */
    private OffsetDateTime olusturulmaTarihi;
}
