package com.akillisaha.crm;

import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.security.JwtBelirteciSaglayici;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT Belirteç Sağlayıcı (JwtBelirteciSaglayici) birim test sınıfı.
 * Belirteç üretimi, doğrulanması ve kullanıcı taleplerinin çözümlenmesini test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
class JwtBelirteciSaglayiciTest {

    private JwtBelirteciSaglayici tokenSaglayici;
    private final String gizliAnahtar = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long gecerlilikSuresiMs = 3600000; // 1 saat

    @BeforeEach
    void baslat() {
        tokenSaglayici = new JwtBelirteciSaglayici(gizliAnahtar, gecerlilikSuresiMs);
    }

    @Test
    @DisplayName("Geçerli kullanıcı bilgisi ile başarılı JWT token üretilmeli ve doğrulanmalıdır")
    void basariliTokenUretimiVeDogrulamasi() {
        Kullanici kullanici = Kullanici.builder()
                .id(1L)
                .eposta("ahmet@akillisaha.com")
                .sifreOzeti("sifrelenmisParola")
                .adSoyad("Ahmet Saha Plasiyeri")
                .rol(Rol.PLASIYER)
                .aktifMi(true)
                .build();

        OzelKullaniciDetaylari detaylar = new OzelKullaniciDetaylari(kullanici);
        Authentication kimlik = new UsernamePasswordAuthenticationToken(detaylar, null, detaylar.getAuthorities());

        String token = tokenSaglayici.tokenUret(kimlik);
        assertNotNull(token, "Üretilen JWT token null olamaz");

        boolean gecerliMi = tokenSaglayici.tokenGecerliMi(token);
        assertTrue(gecerliMi, "Üretilen token geçerli olmalıdır");

        String eposta = tokenSaglayici.tokenEpostasiniAl(token);
        assertEquals("ahmet@akillisaha.com", eposta, "Token'dan çözümlenen e-posta eşleşmelidir");
    }
}
