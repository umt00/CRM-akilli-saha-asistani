package com.akillisaha.crm.dto.response;

import com.akillisaha.crm.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kullanıcı başarıyla oturum açtığında dönen JWT belirteci ve profil bilgilerini içeren DTO.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KimlikDogrulamaYaniti {

    /**
     * İstemcinin sonraki API çağrılarında 'Authorization: Bearer <token>' olarak göndereceği JWT belirteci.
     */
    private String token;

    /**
     * Belirteç tipi (Standart olarak 'Bearer').
     */
    @Builder.Default
    private String tokenTipi = "Bearer";

    /**
     * Giriş yapan kullanıcının veritabanındaki benzersiz kimliği.
     */
    private Long kullaniciId;

    /**
     * Giriş yapan kullanıcının e-posta adresi.
     */
    private String eposta;

    /**
     * Kullanıcının adı ve soyadı.
     */
    private String adSoyad;

    /**
     * Kullanıcının sistemdeki rolü (YONETICI veya PLASIYER).
     */
    private Rol rol;
}
