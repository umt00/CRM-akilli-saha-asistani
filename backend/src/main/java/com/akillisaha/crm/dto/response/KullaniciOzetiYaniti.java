package com.akillisaha.crm.dto.response;

import com.akillisaha.crm.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Aktif oturum açmış kullanıcının profil ve yetki detaylarını dönen DTO.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KullaniciOzetiYaniti {

    /**
     * Kullanıcı benzersiz kimliği.
     */
    private Long id;

    /**
     * Kullanıcı kurumsal e-posta adresi.
     */
    private String eposta;

    /**
     * Kullanıcının tam adı ve soyadı.
     */
    private String adSoyad;

    /**
     * Kullanıcının sistemdeki rolü (YONETICI veya PLASIYER).
     */
    private Rol rol;

    /**
     * Kullanıcının telefon numarası.
     */
    private String telefon;

    /**
     * Kullanıcı hesabının aktiflik durumu.
     */
    private Boolean aktifMi;

    /**
     * Kullanıcının sisteme kayıt tarihi.
     */
    private OffsetDateTime olusturulmaTarihi;
}
