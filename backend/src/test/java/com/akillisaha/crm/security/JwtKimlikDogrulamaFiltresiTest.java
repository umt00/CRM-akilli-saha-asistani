package com.akillisaha.crm.security;

import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.enums.Rol;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JWT Kimlik Doğrulama Filtresi (JwtKimlikDogrulamaFiltresi) birim test sınıfı.
 * Gelen HTTP isteklerindeki Authorization başlığının çözümlenmesi ve SecurityContext bağlamını test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@ExtendWith(MockitoExtension.class)
class JwtKimlikDogrulamaFiltresiTest {

    @Mock
    private JwtBelirteciSaglayici tokenSaglayici;

    @Mock
    private OzelKullaniciDetaylariServisi kullaniciDetaylariServisi;

    @Mock
    private HttpServletRequest istek;

    @Mock
    private HttpServletResponse yanit;

    @Mock
    private FilterChain filtreZinciri;

    @InjectMocks
    private JwtKimlikDogrulamaFiltresi jwtFiltresi;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        Kullanici kullanici = Kullanici.builder()
                .id(1L)
                .eposta("ahmet@akillisaha.com")
                .sifreOzeti("hashlenmisSifre")
                .adSoyad("Ahmet Plasiyer")
                .rol(Rol.PLASIYER)
                .aktifMi(true)
                .build();

        userDetails = new OzelKullaniciDetaylari(kullanici);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Filtre: Geçerli Bearer token verildiğinde kullanıcı SecurityContext'e yerleştirilmeli")
    void doFilterInternal_GecerliToken_SecurityContextGuncellenmeli() throws ServletException, IOException {
        String token = "gecerli.jwt.token";
        when(istek.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenSaglayici.tokenGecerliMi(token)).thenReturn(true);
        when(tokenSaglayici.tokenEpostasiniAl(token)).thenReturn("ahmet@akillisaha.com");
        when(kullaniciDetaylariServisi.loadUserByUsername("ahmet@akillisaha.com")).thenReturn(userDetails);

        jwtFiltresi.doFilter(istek, yanit, filtreZinciri);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("ahmet@akillisaha.com", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filtreZinciri, times(1)).doFilter(istek, yanit);
    }

    @Test
    @DisplayName("Filtre: Authorization başlığı yoksa SecurityContext boş kalmalı ve zincir devam etmeli")
    void doFilterInternal_BaslikYoksa_SecurityContextBosKalmali() throws ServletException, IOException {
        when(istek.getHeader("Authorization")).thenReturn(null);

        jwtFiltresi.doFilter(istek, yanit, filtreZinciri);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenSaglayici, never()).tokenGecerliMi(any());
        verify(filtreZinciri, times(1)).doFilter(istek, yanit);
    }

    @Test
    @DisplayName("Filtre: Geçersiz token verildiğinde SecurityContext'e kimlik atanmamalı")
    void doFilterInternal_GecersizToken_SecurityContextBosKalmali() throws ServletException, IOException {
        String token = "gecersiz.token";
        when(istek.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenSaglayici.tokenGecerliMi(token)).thenReturn(false);

        jwtFiltresi.doFilter(istek, yanit, filtreZinciri);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(kullaniciDetaylariServisi, never()).loadUserByUsername(any());
        verify(filtreZinciri, times(1)).doFilter(istek, yanit);
    }

    @Test
    @DisplayName("Filtre: Bearer ön eki olmayan başlık verildiğinde token çözümlenmemeli")
    void doFilterInternal_BearerPrefixYoksa_SecurityContextBosKalmali() throws ServletException, IOException {
        when(istek.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        jwtFiltresi.doFilter(istek, yanit, filtreZinciri);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(tokenSaglayici, never()).tokenGecerliMi(any());
        verify(filtreZinciri, times(1)).doFilter(istek, yanit);
    }
}
