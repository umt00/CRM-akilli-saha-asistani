package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Firma;
import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.entity.Numune;
import com.akillisaha.crm.enums.NumuneDurumu;
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
 * NumuneRepository için veri erişim ve özel JPQL sorgu (@DataJpaTest) testleri.
 * Durum filtreleri, marka bazlı gruplama ve sayaç sorgularını test eder.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@DataJpaTest
class NumuneRepositoryTest {

    @Autowired
    private NumuneRepository numuneRepository;

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
    @DisplayName("findByDurum: Belirli durumdaki numuneleri sayfalı getirmelidir")
    void findByDurum_DurumaGoreSayfaliNumuneleriGetirir() {
        // Arrange
        Numune n1 = Numune.builder().firma(firma).plasiyer(plasiyer).urunAdi("Aroma 1").miktar("100 GR").durum(NumuneDurumu.BEKLEMEDE).build();
        Numune n2 = Numune.builder().firma(firma).plasiyer(plasiyer).urunAdi("Aroma 2").miktar("200 GR").durum(NumuneDurumu.BEGENDI).build();
        Numune n3 = Numune.builder().firma(firma).plasiyer(plasiyer).urunAdi("Aroma 3").miktar("300 GR").durum(NumuneDurumu.BEKLEMEDE).build();

        entityManager.persist(n1);
        entityManager.persist(n2);
        entityManager.persist(n3);
        entityManager.flush();

        // Act
        Page<Numune> bekleyenler = numuneRepository.findByDurum(NumuneDurumu.BEKLEMEDE, PageRequest.of(0, 10));

        // Assert
        assertEquals(2, bekleyenler.getTotalElements());
        assertTrue(bekleyenler.getContent().stream().allMatch(n -> n.getDurum() == NumuneDurumu.BEKLEMEDE));
    }

    @Test
    @DisplayName("durumaGoreNumuneSayisi: Duruma göre adet sayacını doğru hesaplamalıdır")
    void durumaGoreNumuneSayisi_AdetSayisiniDondurur() {
        // Arrange
        Numune n1 = Numune.builder().firma(firma).plasiyer(plasiyer).urunAdi("U1").miktar("1 KG").durum(NumuneDurumu.BEGENDI).build();
        Numune n2 = Numune.builder().firma(firma).plasiyer(plasiyer).urunAdi("U2").miktar("1 KG").durum(NumuneDurumu.BEGENDI).build();
        Numune n3 = Numune.builder().firma(firma).plasiyer(plasiyer).urunAdi("U3").miktar("1 KG").durum(NumuneDurumu.REDDETTI).build();

        entityManager.persist(n1);
        entityManager.persist(n2);
        entityManager.persist(n3);
        entityManager.flush();

        // Act & Assert
        assertEquals(2, numuneRepository.durumaGoreNumuneSayisi(NumuneDurumu.BEGENDI));
        assertEquals(1, numuneRepository.durumaGoreNumuneSayisi(NumuneDurumu.REDDETTI));
        assertEquals(0, numuneRepository.durumaGoreNumuneSayisi(NumuneDurumu.SIPARISE_DONUSTU));
    }

    @Test
    @DisplayName("markayaGoreNumuneSayilari: Marka bazlı numune dağılımını gruplayarak getirmelidir")
    void markayaGoreNumuneSayilari_GruplanmisIstatistikleriDondurur() {
        // Arrange
        Numune n1 = Numune.builder().firma(firma).plasiyer(plasiyer).urunAdi("Vanilya").marka("Kerry").miktar("100 GR").durum(NumuneDurumu.BEKLEMEDE).build();
        Numune n2 = Numune.builder().firma(firma).plasiyer(plasiyer).urunAdi("Çilek").marka("Kerry").miktar("100 GR").durum(NumuneDurumu.BEKLEMEDE).build();
        Numune n3 = Numune.builder().firma(firma).plasiyer(plasiyer).urunAdi("Kakao").marka("Cargill").miktar("100 GR").durum(NumuneDurumu.BEKLEMEDE).build();

        entityManager.persist(n1);
        entityManager.persist(n2);
        entityManager.persist(n3);
        entityManager.flush();

        // Act
        List<Object[]> istatistikler = numuneRepository.markayaGoreNumuneSayilari();

        // Assert
        assertEquals(2, istatistikler.size());
        for (Object[] satir : istatistikler) {
            String marka = (String) satir[0];
            Long adet = (Long) satir[1];
            if ("Kerry".equals(marka)) {
                assertEquals(2L, adet);
            } else if ("Cargill".equals(marka)) {
                assertEquals(1L, adet);
            }
        }
    }
}
