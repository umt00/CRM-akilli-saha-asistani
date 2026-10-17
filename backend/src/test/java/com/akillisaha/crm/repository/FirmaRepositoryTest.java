package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Firma;
import com.akillisaha.crm.entity.Kullanici;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FirmaRepository için veri erişim ve özel JPQL sorgu (@DataJpaTest) testleri.
 * Veri tabanı seviyesinde plasiyer veri izolasyonu, unvan araması ve metin sorgularını test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@DataJpaTest
class FirmaRepositoryTest {

    @Autowired
    private FirmaRepository firmaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Kullanici plasiyer1;
    private Kullanici plasiyer2;

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
    }

    @Test
    @DisplayName("findByAtananPlasiyerId: Plasiyere atanan firmaları izole şekilde getirmelidir")
    void findByAtananPlasiyerId_SadeceIlgiliPlasiyerinFirmalariniGetirir() {
        // Arrange
        Firma f1 = Firma.builder().unvan("Firma 1").sehirBolge("Istanbul").atananPlasiyer(plasiyer1).build();
        Firma f2 = Firma.builder().unvan("Firma 2").sehirBolge("Kocaeli").atananPlasiyer(plasiyer1).build();
        Firma f3 = Firma.builder().unvan("Firma 3").sehirBolge("Bursa").atananPlasiyer(plasiyer2).build();

        entityManager.persist(f1);
        entityManager.persist(f2);
        entityManager.persist(f3);
        entityManager.flush();

        // Act
        Page<Firma> p1Firmalari = firmaRepository.findByAtananPlasiyerId(plasiyer1.getId(), PageRequest.of(0, 10));

        // Assert
        assertEquals(2, p1Firmalari.getTotalElements());
        assertTrue(p1Firmalari.getContent().stream().allMatch(f -> f.getAtananPlasiyer().getId().equals(plasiyer1.getId())));
    }

    @Test
    @DisplayName("existsByUnvanIgnoreCase: Harf duyarsız unvan varlığını doğru tespit etmelidir")
    void existsByUnvanIgnoreCase_HarfDuyarsizVarMiKontrolEder() {
        // Arrange
        Firma f = Firma.builder().unvan("ABC Metal Sanayi").atananPlasiyer(plasiyer1).build();
        entityManager.persistAndFlush(f);

        // Act & Assert
        assertTrue(firmaRepository.existsByUnvanIgnoreCase("abc metal sanayi"));
        assertTrue(firmaRepository.existsByUnvanIgnoreCase("Abc Metal Sanayi"));
        assertFalse(firmaRepository.existsByUnvanIgnoreCase("XYZ Metal"));
    }

    @Test
    @DisplayName("findByUnvanIgnoreCase: Harf duyarsız unvan aramasında ilgili firmayı dönmelidir")
    void findByUnvanIgnoreCase_HarfDuyarsizFirmayiBulur() {
        // Arrange
        Firma f = Firma.builder().unvan("Hedef Gıda Ltd").atananPlasiyer(plasiyer1).build();
        entityManager.persistAndFlush(f);

        // Act
        Optional<Firma> bulunan = firmaRepository.findByUnvanIgnoreCase("hedef gıda ltd");

        // Assert
        assertTrue(bulunan.isPresent());
        assertEquals("Hedef Gıda Ltd", bulunan.get().getUnvan());
    }

    @Test
    @DisplayName("firmalardaAra: JPQL özel sorgusu hem unvanda hem şehirde LIKE eşleşmesi yapmalıdır")
    void firmalardaAra_UnvanVeSehirdeAramaYapar() {
        // Arrange
        Firma f1 = Firma.builder().unvan("Tatlı Dünyası Pastanesi").sehirBolge("Ankara / Çankaya").atananPlasiyer(plasiyer1).build();
        Firma f2 = Firma.builder().unvan("Kuzey Unlu Mamulleri").sehirBolge("Istanbul / Kadıköy").atananPlasiyer(plasiyer1).build();
        Firma f3 = Firma.builder().unvan("Çankaya Gıda Pazarlama").sehirBolge("İzmir").atananPlasiyer(plasiyer2).build();

        entityManager.persist(f1);
        entityManager.persist(f2);
        entityManager.persist(f3);
        entityManager.flush();

        // Act: Unvanda "tatlı" geçenler
        Page<Firma> tatliSonucu = firmaRepository.firmalardaAra("tatlı", PageRequest.of(0, 10));

        // Assert
        assertEquals(1, tatliSonucu.getTotalElements());
        assertEquals("Tatlı Dünyası Pastanesi", tatliSonucu.getContent().get(0).getUnvan());
    }
}
