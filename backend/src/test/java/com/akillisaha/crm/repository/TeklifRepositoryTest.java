package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Firma;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.entity.Teklif;
import com.akillisaha.crm.enums.Rol;
import com.akillisaha.crm.enums.TeklifDurumu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TeklifRepository için veri erişim ve özel JPQL sorgu (@DataJpaTest) testleri.
 * Toplam ciro/tutar toplamları (SUM), durum bazlı filtreler ve sayaçları test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@DataJpaTest
class TeklifRepositoryTest {

    @Autowired
    private TeklifRepository teklifRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Kullanici plasiyer;
    private Firma firma;

    @BeforeEach
    void hazirla() {
        plasiyer = Kullanici.builder()
                .eposta("plasiyer@saha.com")
                .sifreOzeti("hash")
                .adSoyad("Plasiyer Ali")
                .rol(Rol.PLASIYER)
                .aktifMi(true)
                .olusturulmaTarihi(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
        plasiyer = entityManager.persistAndFlush(plasiyer);

        firma = Firma.builder()
                .unvan("Lezzet Pastanesi")
                .sehirBolge("Istanbul")
                .atananPlasiyer(plasiyer)
                .build();
        firma = entityManager.persistAndFlush(firma);
    }

    @Test
    @DisplayName("findByDurum: Duruma göre sayfalı teklifleri getirmelidir")
    void findByDurum_DurumaGoreTeklifleriDondurur() {
        // Arrange
        Teklif t1 = Teklif.builder().firma(firma).plasiyer(plasiyer).baslik("T1").tutar(new BigDecimal("10000")).durum(TeklifDurumu.ACIK).build();
        Teklif t2 = Teklif.builder().firma(firma).plasiyer(plasiyer).baslik("T2").tutar(new BigDecimal("20000")).durum(TeklifDurumu.KAZANILDI).build();
        Teklif t3 = Teklif.builder().firma(firma).plasiyer(plasiyer).baslik("T3").tutar(new BigDecimal("30000")).durum(TeklifDurumu.ACIK).build();

        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.persist(t3);
        entityManager.flush();

        // Act
        Page<Teklif> acikTeklifler = teklifRepository.findByDurum(TeklifDurumu.ACIK, PageRequest.of(0, 10));

        // Assert
        assertEquals(2, acikTeklifler.getTotalElements());
        assertTrue(acikTeklifler.getContent().stream().allMatch(t -> t.getDurum() == TeklifDurumu.ACIK));
    }

    @Test
    @DisplayName("durumaGoreToplamTutar: Duruma göre parasal toplamı (SUM) doğru hesaplamalıdır")
    void durumaGoreToplamTutar_ParasalToplamiDondurur() {
        // Arrange
        Teklif t1 = Teklif.builder().firma(firma).plasiyer(plasiyer).baslik("T1").tutar(new BigDecimal("150000.50")).durum(TeklifDurumu.KAZANILDI).build();
        Teklif t2 = Teklif.builder().firma(firma).plasiyer(plasiyer).baslik("T2").tutar(new BigDecimal("50000.25")).durum(TeklifDurumu.KAZANILDI).build();
        Teklif t3 = Teklif.builder().firma(firma).plasiyer(plasiyer).baslik("T3").tutar(new BigDecimal("30000.00")).durum(TeklifDurumu.KAYBEDILDI).build();

        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.persist(t3);
        entityManager.flush();

        // Act
        BigDecimal kazanilanToplam = teklifRepository.durumaGoreToplamTutar(TeklifDurumu.KAZANILDI);
        BigDecimal kaybedilenToplam = teklifRepository.durumaGoreToplamTutar(TeklifDurumu.KAYBEDILDI);

        // Assert
        assertNotNull(kazanilanToplam);
        assertEquals(new BigDecimal("200000.75"), kazanilanToplam);
        assertEquals(new BigDecimal("30000.00"), kaybedilenToplam);
    }

    @Test
    @DisplayName("durumaGoreTeklifSayisi: Duruma göre teklif adedini doğru saymalıdır")
    void durumaGoreTeklifSayisi_AdetSayisiniDondurur() {
        // Arrange
        Teklif t1 = Teklif.builder().firma(firma).plasiyer(plasiyer).baslik("T1").tutar(BigDecimal.TEN).durum(TeklifDurumu.ACIK).build();
        Teklif t2 = Teklif.builder().firma(firma).plasiyer(plasiyer).baslik("T2").tutar(BigDecimal.TEN).durum(TeklifDurumu.ACIK).build();

        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.flush();

        // Act & Assert
        assertEquals(2, teklifRepository.durumaGoreTeklifSayisi(TeklifDurumu.ACIK));
        assertEquals(0, teklifRepository.durumaGoreTeklifSayisi(TeklifDurumu.IPTAL));
    }
}
