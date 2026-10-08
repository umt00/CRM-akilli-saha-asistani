package com.akillisaha.crm.dto.request;

import com.akillisaha.crm.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Yeni satış temsilcisi veya yönetici hesabı oluşturma istek DTO'su.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KayitIstegi {

    /**
     * Kullanıcı kurumsal e-posta adresi.
     */
    @NotBlank(message = "E-posta adresi boş bırakılamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String eposta;

    /**
     * Kullanıcı şifresi.
     */
    @NotBlank(message = "Şifre alanı boş bırakılamaz")
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    private String sifre;

    /**
     * Kullanıcının tam adı ve soyadı.
     */
    @NotBlank(message = "Ad Soyad alanı boş bırakılamaz")
    @Size(max = 120, message = "Ad Soyad en fazla 120 karakter olabilir")
    private String adSoyad;

    /**
     * Atanacak kullanıcı rolü (Varsayılan: PLASIYER).
     */
    @Builder.Default
    private Rol rol = Rol.PLASIYER;

    /**
     * İletişim telefon numarası.
     */
    private String telefon;
}
