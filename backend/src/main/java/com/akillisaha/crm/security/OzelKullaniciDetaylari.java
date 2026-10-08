package com.akillisaha.crm.security;

import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.enums.Rol;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security kimlik doğrulama mekanizması için Kullanici varlığını sarmalayan
 * özel UserDetails implementasyonu.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Getter
public class OzelKullaniciDetaylari implements UserDetails {

    private final Long id;
    private final String eposta;
    private final String sifre;
    private final String adSoyad;
    private final Rol rol;
    private final boolean aktifMi;
    private final Collection<? extends GrantedAuthority> yetkiler;

    /**
     * Veritabanından çekilen Kullanici nesnesiyle UserDetails modelini başlatır.
     * 
     * @param kullanici Kullanıcı entity nesnesi
     */
    public OzelKullaniciDetaylari(Kullanici kullanici) {
        this.id = kullanici.getId();
        this.eposta = kullanici.getEposta();
        this.sifre = kullanici.getSifreOzeti();
        this.adSoyad = kullanici.getAdSoyad();
        this.rol = kullanici.getRol();
        this.aktifMi = Boolean.TRUE.equals(kullanici.getAktifMi());
        this.yetkiler = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + kullanici.getRol().name())
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return yetkiler;
    }

    @Override
    public String getPassword() {
        return sifre;
    }

    @Override
    public String getUsername() {
        return eposta;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return aktifMi;
    }
}
