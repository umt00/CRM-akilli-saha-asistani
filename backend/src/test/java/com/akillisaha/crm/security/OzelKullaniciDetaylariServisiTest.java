package com.akillisaha.crm.security;

import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.repository.KullaniciRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Özel Kullanıcı Detayları Servisi (OzelKullaniciDetaylariServisi) birim test sınıfı.
 * E-posta ile kullanıcı arama, bulunamadığında istisna üretme ve Spring Security yetkilerini test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@ExtendWith(MockitoExtension.class)
class OzelKullaniciDetaylariServisiTest {

    @Mock
    private KullaniciRepository kullaniciRepository;

    @InjectMocks
    private OzelKullaniciDetaylariServisi kullaniciDetaylariServisi;

    private Kullanici plasiyerKullanici;
    private Kullanici yoneticiKullanici;

    @BeforeEach
    void setUp() {
        plasiyerKullanici = Kullanici.builder()
                .id(1L)
                .eposta("plasiyer@akillisaha.com")
                .sifreOzeti("hashlenmisSifre")
                .adSoyad("Ali Veli")
                .rol(Rol.PLASIYER)
                .aktifMi(true)
                .build();

        yoneticiKullanici = Kullanici.builder()
                .id(2L)
                .eposta("admin@akillisaha.com")
                .sifreOzeti("adminSifre")
                .adSoyad("Yönetici Fatma")
                .rol(Rol.YONETICI)
                .aktifMi(true)
                .build();
    }

    @Test
    @DisplayName("Kullanıcı Adına Göre Yükle: Mevcut e-posta ile UserDetails nesnesi başarıyla dönmeli")
    void loadUserByUsername_MevcutEposta_OzelKullaniciDetaylariDonmeli() {
        when(kullaniciRepository.findByEposta("plasiyer@akillisaha.com")).thenReturn(Optional.of(plasiyerKullanici));

        UserDetails userDetails = kullaniciDetaylariServisi.loadUserByUsername("plasiyer@akillisaha.com");

        assertNotNull(userDetails);
        assertEquals("plasiyer@akillisaha.com", userDetails.getUsername());
        assertEquals("hashlenmisSifre", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails instanceof OzelKullaniciDetaylari);

        OzelKullaniciDetaylari ozelDetay = (OzelKullaniciDetaylari) userDetails;
        assertEquals(1L, ozelDetay.getId());
        assertEquals("Ali Veli", ozelDetay.getAdSoyad());
        assertEquals(Rol.PLASIYER, ozelDetay.getRol());

        verify(kullaniciRepository, times(1)).findByEposta("plasiyer@akillisaha.com");
    }

    @Test
    @DisplayName("Kullanıcı Adına Göre Yükle: Veritabanında olmayan e-posta verildiğinde UsernameNotFoundException fırlatılmalı")
    void loadUserByUsername_OlmayanEposta_UsernameNotFoundExceptionFirlatmali() {
        when(kullaniciRepository.findByEposta("bilinmeyen@akillisaha.com")).thenReturn(Optional.empty());

        UsernameNotFoundException hata = assertThrows(
                UsernameNotFoundException.class,
                () -> kullaniciDetaylariServisi.loadUserByUsername("bilinmeyen@akillisaha.com")
        );

        assertTrue(hata.getMessage().contains("Kullanıcı bulunamadı: bilinmeyen@akillisaha.com"));
        verify(kullaniciRepository, times(1)).findByEposta("bilinmeyen@akillisaha.com");
    }

    @Test
    @DisplayName("Yetkiler Kontrolü: Kullanıcının rolüne göre ROLE_ ön ekiyle yetki üretilmeli")
    void loadUserByUsername_YetkilerKontrolu_RolePrefixIleOtoriteUretmeli() {
        when(kullaniciRepository.findByEposta("admin@akillisaha.com")).thenReturn(Optional.of(yoneticiKullanici));

        UserDetails userDetails = kullaniciDetaylariServisi.loadUserByUsername("admin@akillisaha.com");

        assertNotNull(userDetails);
        boolean adminYetkisiVarMi = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_YONETICI"));

        assertTrue(adminYetkisiVarMi, "Kullanıcının yetkileri arasında ROLE_YONETICI bulunmalıdır");
    }
}
