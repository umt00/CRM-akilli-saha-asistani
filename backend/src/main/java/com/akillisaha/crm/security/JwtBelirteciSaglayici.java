package com.akillisaha.crm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT (JSON Web Token) üretimi, şifrelenmesi, doğrulanması ve kullanıcı taleplerinin (claims)
 * ayrıştırılmasından sorumlu bileşen.
 * JJWT 0.12.x modern API standartlarını kullanır.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Slf4j
@Component
public class JwtBelirteciSaglayici {

    private final SecretKey gizliAnahtar;
    private final long gecerlilikSuresiMs;

    /**
     * application.yml dosyasından okunan gizli anahtar ve süre değerleriyle başlatılır.
     * 
     * @param gizliAnahtarMetni 256-bit HMAC-SHA gizli anahtar metni
     * @param gecerlilikSuresiMs Token geçerlilik süresi (milisaniye)
     */
    public JwtBelirteciSaglayici(
            @Value("${app.jwt.secret}") String gizliAnahtarMetni,
            @Value("${app.jwt.expiration-ms}") long gecerlilikSuresiMs) {
        this.gizliAnahtar = Keys.hmacShaKeyFor(gizliAnahtarMetni.getBytes(StandardCharsets.UTF_8));
        this.gecerlilikSuresiMs = gecerlilikSuresiMs;
    }

    /**
     * Başarılı kimlik doğrulama işlemi sonucunda kullanıcı adına imzalı bir JWT belirteci üretir.
     * 
     * @param kimlikDogrulama Spring Security kimlik doğrulama nesnesi
     * @return İmzalanmış JWT token dizgesi
     */
    public String tokenUret(Authentication kimlikDogrulama) {
        OzelKullaniciDetaylari kullanici = (OzelKullaniciDetaylari) kimlikDogrulama.getPrincipal();
        Date simdikiZaman = new Date();
        Date bitisZamani = new Date(simdikiZaman.getTime() + gecerlilikSuresiMs);

        return Jwts.builder()
                .subject(kullanici.getUsername())
                .claim("kullaniciId", kullanici.getId())
                .claim("adSoyad", kullanici.getAdSoyad())
                .claim("rol", kullanici.getRol().name())
                .issuedAt(simdikiZaman)
                .expiration(bitisZamani)
                .signWith(gizliAnahtar)
                .compact();
    }

    /**
     * İmzalanmış JWT belirtecinin içinden kullanıcının e-posta adresini (subject) çıkarır.
     * 
     * @param token JWT token
     * @return Kullanıcı e-posta adresi
     */
    public String tokenEpostasiniAl(String token) {
        Claims talepler = Jwts.parser()
                .verifyWith(gizliAnahtar)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return talepler.getSubject();
    }

    /**
     * Belirtecin geçerli, imzasının bozulmamış ve süresinin dolmamış olduğunu teyit eder.
     * 
     * @param token Kontrol edilecek token
     * @return Geçerliyse true, aksi halde false
     */
    public boolean tokenGecerliMi(String token) {
        try {
            Jwts.parser()
                    .verifyWith(gizliAnahtar)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException istisna) {
            log.error("Geçersiz JWT imzası veya süresi dolmuş token: {}", istisna.getMessage());
        }
        return false;
    }
}
