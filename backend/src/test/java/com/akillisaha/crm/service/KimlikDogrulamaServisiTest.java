package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.GirisIstegi;
import com.akillisaha.crm.dto.request.KayitIstegi;
import com.akillisaha.crm.dto.response.KimlikDogrulamaYaniti;
import com.akillisaha.crm.dto.response.KullaniciOzetiYaniti;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.exception.GecersizIstekHatasi;
import com.akillisaha.crm.exception.KaynakBulunamadiHatasi;
import com.akillisaha.crm.repository.KullaniciRepository;
import com.akillisaha.crm.security.JwtBelirteciSaglayici;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.impl.KimlikDogrulamaServisiImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * KimlikDoğrulamaServisi (KimlikDogrulamaServisiImpl) birim test sınıfı.
 * Giriş yapma, yeni kullanıcı kaydetme ve aktif profil sorgulama senaryolarını test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@ExtendWith(MockitoExtension.class)
class KimlikDogrulamaServisiTest {

    @Mock
    private AuthenticationManager kimlikYoneticisi;

    @Mock
    private KullaniciRepository kullaniciRepository;

    @Mock
    private PasswordEncoder parolaSifreleyici;

    @Mock
    private JwtBelirteciSaglayici tokenSaglayici;

    @InjectMocks
    private KimlikDogrulamaServisiImpl kimlikDogrulamaServisi;

    private Kullanici ornekKullanici;
    private OzelKullaniciDetaylari ornekDetaylar;

    @BeforeEach
    void setUp() {
        ornekKullanici = Kullanici.builder()
                .id(1L)
                .eposta("ahmet@akillisaha.com")
                .sifreOzeti("hashlenmisSifre")
                .adSoyad("Ahmet Yılmaz")
                .rol(Rol.PLASIYER)
                .telefon("05551112233")
                .aktifMi(true)
                .olusturulmaTarihi(OffsetDateTime.now())
                .build();

        ornekDetaylar = new OzelKullaniciDetaylari(ornekKullanici);
    }

    @Test
    @DisplayName("Giriş Yap: Doğru e-posta ve şifre ile başarılı token ve kullanıcı bilgisi dönmeli")
    void girisYap_GecerliBilgilerle_BasariliKimlikDogrulamaYanitiDonmeli() {
        GirisIstegi istek = new GirisIstegi("ahmet@akillisaha.com", "saha123");
        Authentication auth = mock(Authentication.class);

        when(auth.getPrincipal()).thenReturn(ornekDetaylar);
        when(kimlikYoneticisi.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenSaglayici.tokenUret(auth)).thenReturn("ornek.jwt.token");

        KimlikDogrulamaYaniti yanit = kimlikDogrulamaServisi.girisYap(istek);

        assertNotNull(yanit, "Kimlik doğrulama yanıtı null olamaz");
        assertEquals("ornek.jwt.token", yanit.getToken());
        assertEquals("Bearer", yanit.getTokenTipi());
        assertEquals(1L, yanit.getKullaniciId());
        assertEquals("ahmet@akillisaha.com", yanit.getEposta());
        assertEquals("Ahmet Yılmaz", yanit.getAdSoyad());
        assertEquals(Rol.PLASIYER, yanit.getRol());

        verify(kimlikYoneticisi, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenSaglayici, times(1)).tokenUret(auth);
    }

    @Test
    @DisplayName("Giriş Yap: Hatalı şifre verildiğinde kimlik yöneticisi istisna fırlatmalı")
    void girisYap_HataliSifreVerildiginde_HataFirlatmali() {
        GirisIstegi istek = new GirisIstegi("ahmet@akillisaha.com", "yanlisSifre");

        when(kimlikYoneticisi.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Geçersiz kullanıcı adı veya şifre"));

        assertThrows(BadCredentialsException.class, () -> kimlikDogrulamaServisi.girisYap(istek));
        verify(tokenSaglayici, never()).tokenUret(any());
    }

    @Test
    @DisplayName("Kayıt Ol: Geçerli bilgilerle yeni kullanıcı kaydedilmeli ve JWT token üretilmeli")
    void kayitOl_GecerliBilgilerle_YeniKullaniciKaydedipTokenUretmeli() {
        KayitIstegi istek = KayitIstegi.builder()
                .eposta("mehmet@akillisaha.com")
                .sifre("sifre123")
                .adSoyad("Mehmet Kaya")
                .telefon("05559998877")
                .rol(Rol.PLASIYER)
                .build();
        Authentication auth = mock(Authentication.class);

        when(kullaniciRepository.existsByEposta("mehmet@akillisaha.com")).thenReturn(false);
        when(parolaSifreleyici.encode("sifre123")).thenReturn("encodedSifre");
        when(kullaniciRepository.save(any(Kullanici.class))).thenAnswer(inv -> {
            Kullanici k = inv.getArgument(0);
            k.setId(2L);
            return k;
        });
        when(kimlikYoneticisi.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenSaglayici.tokenUret(auth)).thenReturn("mehmet.jwt.token");

        KimlikDogrulamaYaniti yanit = kimlikDogrulamaServisi.kayitOl(istek);

        assertNotNull(yanit);
        assertEquals("mehmet.jwt.token", yanit.getToken());
        assertEquals(2L, yanit.getKullaniciId());
        assertEquals("mehmet@akillisaha.com", yanit.getEposta());
        assertEquals("Mehmet Kaya", yanit.getAdSoyad());

        verify(kullaniciRepository, times(1)).existsByEposta("mehmet@akillisaha.com");
        verify(parolaSifreleyici, times(1)).encode("sifre123");
        verify(kullaniciRepository, times(1)).save(any(Kullanici.class));
        verify(tokenSaglayici, times(1)).tokenUret(auth);
    }

    @Test
    @DisplayName("Kayıt Ol: Mükerrer e-posta verildiğinde GecersizIstekHatasi fırlatılmalı")
    void kayitOl_MukkerrerEpostaVerildiginde_GecersizIstekHatasiFirlatmali() {
        KayitIstegi istek = KayitIstegi.builder()
                .eposta("ahmet@akillisaha.com")
                .sifre("sifre123")
                .adSoyad("Ahmet Yılmaz")
                .rol(Rol.PLASIYER)
                .build();

        when(kullaniciRepository.existsByEposta("ahmet@akillisaha.com")).thenReturn(true);

        GecersizIstekHatasi hata = assertThrows(GecersizIstekHatasi.class, () -> kimlikDogrulamaServisi.kayitOl(istek));
        assertTrue(hata.getMessage().contains("Bu e-posta adresi ile kayıtlı kullanıcı zaten mevcut"));

        verify(kullaniciRepository, never()).save(any());
        verify(tokenSaglayici, never()).tokenUret(any());
    }

    @Test
    @DisplayName("Aktif Kullanıcıyı Getir: Kullanıcı mevcutsa özet bilgileri dönmeli")
    void aktifKullaniciyiGetir_KullaniciMevcutsa_KullaniciOzetiniDonmeli() {
        when(kullaniciRepository.findById(1L)).thenReturn(Optional.of(ornekKullanici));

        KullaniciOzetiYaniti yanit = kimlikDogrulamaServisi.aktifKullaniciyiGetir(ornekDetaylar);

        assertNotNull(yanit);
        assertEquals(1L, yanit.getId());
        assertEquals("ahmet@akillisaha.com", yanit.getEposta());
        assertEquals("Ahmet Yılmaz", yanit.getAdSoyad());
        assertEquals(Rol.PLASIYER, yanit.getRol());
        assertEquals("05551112233", yanit.getTelefon());
        assertTrue(yanit.getAktifMi());

        verify(kullaniciRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Aktif Kullanıcıyı Getir: Kullanıcı veritabanında bulunamazsa KaynakBulunamadiHatasi fırlatılmalı")
    void aktifKullaniciyiGetir_KullaniciBulunamazsa_KaynakBulunamadiHatasiFirlatmali() {
        when(kullaniciRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(KaynakBulunamadiHatasi.class, () -> kimlikDogrulamaServisi.aktifKullaniciyiGetir(ornekDetaylar));
        verify(kullaniciRepository, times(1)).findById(1L);
    }
}
