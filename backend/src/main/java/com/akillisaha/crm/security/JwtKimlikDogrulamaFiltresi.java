package com.akillisaha.crm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Gelen her HTTP isteğinde 'Authorization: Bearer <token>' başlığını yakalayan,
 * JWT imzasını doğrulayan ve oturum açmış kullanıcıyı SecurityContext'e yerleştiren filtre sınıfı.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtKimlikDogrulamaFiltresi extends OncePerRequestFilter {

    private final JwtBelirteciSaglayici tokenSaglayici;
    private final OzelKullaniciDetaylariServisi kullaniciDetaylariServisi;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest istek,
                                    @NonNull HttpServletResponse yanit,
                                    @NonNull FilterChain filtreZinciri) throws ServletException, IOException {
        try {
            String token = istektenTokeniAl(istek);

            if (StringUtils.hasText(token) && tokenSaglayici.tokenGecerliMi(token)) {
                String eposta = tokenSaglayici.tokenEpostasiniAl(token);
                UserDetails kullaniciDetaylari = kullaniciDetaylariServisi.loadUserByUsername(eposta);

                UsernamePasswordAuthenticationToken kimlik =
                        new UsernamePasswordAuthenticationToken(kullaniciDetaylari, null, kullaniciDetaylari.getAuthorities());
                kimlik.setDetails(new WebAuthenticationDetailsSource().buildDetails(istek));

                SecurityContextHolder.getContext().setAuthentication(kimlik);
            }
        } catch (Exception istisna) {
            log.error("Kullanıcı kimliği güvenlik bağlamına (SecurityContext) yerleştirilemedi: {}", istisna.getMessage());
        }

        filtreZinciri.doFilter(istek, yanit);
    }

    private String istektenTokeniAl(HttpServletRequest istek) {
        String baslik = istek.getHeader("Authorization");
        if (StringUtils.hasText(baslik) && baslik.startsWith("Bearer ")) {
            return baslik.substring(7);
        }
        return null;
    }
}
