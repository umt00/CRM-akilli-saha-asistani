package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Firma;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.entity.Ziyaret;
import com.akillisaha.crm.enums.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ZiyaretRepository için veri erişim ve özel JPQL sorgu (@DataJpaTest) testleri.
 * Ajanda randevuları, tarih aralığı filtreleri ve numune/teklif sayaçlarını test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@DataJpaTest
class ZiyaretRepositoryTest {

    @Autowired
    private ZiyaretRepository ziyaretRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Kullanici plasiyer1;
    private Kullanici plasiyer2;
    private Firma firma;

    @BeforeEach
    void hazirla() {
        plasiyer1 = Kullanici.builder()
                .eposta("plasiyer1@saha.com")
                .sifreOzeti("hash1")
                .adSoyad("Plasiyer Bir")
                .rol(Rol.PLASIYER)
                .aktifMi(true)
                .olusturulmaTarihi(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
        plasiyer1 = entityManager.persistAndFlush(plasiyer1);

        plasiyer2 = Kullanici.builder()
                .eposta("plasiyer2@saha.com")
                .sifreOzeti("hash2")
                .adSoyad("Plasiyer Iki")
                .rol(Rol.PLASIYER)
                .aktifMi(true)
                .olusturulmaTarihi(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
        plasiyer2 = entityManager.persistAndFlush(plasiyer2);

        firma = Firma.builder()
                .unvan("ABC Metal Sanayi")
                .sehirBolge("Kocaeli")
                .atananPlasiyer(plasiyer1)
                .build();
        firma = entityManager.persistAndFlush(firma);
    }

    @Test
    @DisplayName("findByPlasiyerId: Plasiyere göre ziyaretleri izole getirmelidir")
    void findByPlasiyerId_SadeceIlgiliPlasiyerinZiyaretleriniGetirir() {
        // Arrange
        Ziyaret z1 = Ziyaret.builder().firma(firma).plasiyer(plasiyer1).ziyaretKonusu("Ziyaret 1").ziyaretTarihi(OffsetDateTime.now(ZoneOffset.UTC)).build();
        Ziyaret z2 = Ziyaret.builder().firma(firma).plasiyer(plasiyer1).ziyaretKonusu("Ziyaret 2").ziyaretTarihi(OffsetDateTime.now(ZoneOffset.UTC)).build();
        Ziyaret z3 = Ziyaret.builder().firma(firma).plasiyer(plasiyer2).ziyaretKonusu("Ziyaret 3").ziyaretTarihi(OffsetDateTime.now(ZoneOffset.UTC)).build();

        entityManager.persist(z1);
        entityManager.persist(z2);
        entityManager.persist(z3);
        entityManager.flush();

        // Act
        Page<Ziyaret> p1Ziyaretleri = ziyaretRepository.findByPlasiyerId(plasiyer1.getId(), PageRequest.of(0, 10));

        // Assert
        assertEquals(2, p1Ziyaretleri.getTotalElements());
        assertTrue(p1Ziyaretleri.getContent().stream().allMatch(z -> z.getPlasiyer().getId().equals(plasiyer1.getId())));
    }

    @Test
    @DisplayName("findByZiyaretTarihiBetween: Belirli tarih aralığındaki ziyaretleri doğru getirmelidir")
    void findByZiyaretTarihiBetween_AraliktakiZiyaretleriDondurur() {
        // Arrange
        OffsetDateTime t1 = OffsetDateTime.of(2026, 4, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime t2 = OffsetDateTime.of(2026, 5, 10, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime t3 = OffsetDateTime.of(2026, 6, 1, 10, 0, 0, 0, ZoneOffset.UTC);

        Ziyaret z1 = Ziyaret.builder().firma(firma).plasiyer(plasiyer1).ziyaretKonusu("Nisan").ziyaretTarihi(t1).build();
        Ziyaret z2 = Ziyaret.builder().firma(firma).plasiyer(plasiyer1).ziyaretKonusu("Mayıs").ziyaretTarihi(t2).build();
        Ziyaret z3 = Ziyaret.builder().firma(firma).plasiyer(plasiyer1).ziyaretKonusu("Haziran").ziyaretTarihi(t3).build();

        entityManager.persist(z1);
        entityManager.persist(z2);
        entityManager.persist(z3);
        entityManager.flush();

        // Act
        OffsetDateTime baslangic = OffsetDateTime.of(2026, 5, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime bitis = OffsetDateTime.of(2026, 5, 31, 23, 59, 59, 0, ZoneOffset.UTC);
        List<Ziyaret> mayisZiyaretleri = ziyaretRepository.findByZiyaretTarihiBetween(baslangic, bitis);

        // Assert
        assertEquals(1, mayisZiyaretleri.size());
        assertEquals("Mayıs", mayisZiyaretleri.get(0).getZiyaretKonusu());
    }

    @Test
    @DisplayName("vadesiGelenZiyaretleriGetir: Sonraki ziyaret tarihi belirtilen tarihten önce olanları getirmelidir")
    void vadesiGelenZiyaretleriGetir_AjandaRandevulariniGetirir() {
        // Arrange
        OffsetDateTime bugun = OffsetDateTime.of(2026, 5, 15, 12, 0, 0, 0, ZoneOffset.UTC);

        // Randevusu dün olan (Gecikmiş)
        Ziyaret zGeciken = Ziyaret.builder()
                .firma(firma).plasiyer(plasiyer1).ziyaretKonusu("Geciken")
                .ziyaretTarihi(OffsetDateTime.of(2026, 5, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .sonrakiZiyaretTarihi(OffsetDateTime.of(2026, 5, 14, 10, 0, 0, 0, ZoneOffset.UTC))
                .build();

        // Randevusu gelecek hafta olan (Henüz vadesi gelmemiş)
        Ziyaret zGelecek = Ziyaret.builder()
                .firma(firma).plasiyer(plasiyer1).ziyaretKonusu("Gelecek Hafta")
                .ziyaretTarihi(OffsetDateTime.of(2026, 5, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .sonrakiZiyaretTarihi(OffsetDateTime.of(2026, 5, 25, 10, 0, 0, 0, ZoneOffset.UTC))
                .build();

        // Randevusu olmayan ziyaret
        Ziyaret zRandevusuz = Ziyaret.builder()
                .firma(firma).plasiyer(plasiyer1).ziyaretKonusu("Randevusuz")
                .ziyaretTarihi(OffsetDateTime.of(2026, 5, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .sonrakiZiyaretTarihi(null)
                .build();

        entityManager.persist(zGeciken);
        entityManager.persist(zGelecek);
        entityManager.persist(zRandevusuz);
        entityManager.flush();

        // Act
        List<Ziyaret> ajanda = ziyaretRepository.vadesiGelenZiyaretleriGetir(bugun);

        // Assert
        assertEquals(1, ajanda.size());
        assertEquals("Geciken", ajanda.get(0).getZiyaretKonusu());
    }

    @Test
    @DisplayName("numuneliZiyaretSayisi ve teklifliZiyaretSayisi: İstatistik sayaçlarını doğru hesaplamalıdır")
    void sayaçSorgulari_DogruAdetleriDondurur() {
        // Arrange
        Ziyaret z1 = Ziyaret.builder().firma(firma).plasiyer(plasiyer1).ziyaretKonusu("Z1").ziyaretTarihi(OffsetDateTime.now(ZoneOffset.UTC)).numuneVerildiMi(true).teklifVerildiMi(true).build();
        Ziyaret z2 = Ziyaret.builder().firma(firma).plasiyer(plasiyer1).ziyaretKonusu("Z2").ziyaretTarihi(OffsetDateTime.now(ZoneOffset.UTC)).numuneVerildiMi(true).teklifVerildiMi(false).build();
        Ziyaret z3 = Ziyaret.builder().firma(firma).plasiyer(plasiyer1).ziyaretKonusu("Z3").ziyaretTarihi(OffsetDateTime.now(ZoneOffset.UTC)).numuneVerildiMi(false).teklifVerildiMi(false).build();

        entityManager.persist(z1);
        entityManager.persist(z2);
        entityManager.persist(z3);
        entityManager.flush();

        // Act & Assert
        assertEquals(2, ziyaretRepository.numuneliZiyaretSayisi());
        assertEquals(1, ziyaretRepository.teklifliZiyaretSayisi());
    }
}
