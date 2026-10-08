package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Firma;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Müşteri firmalar (Firma) için veritabanı sorgu ve erişim katmanı.
 * Veri izolasyonu kurallarını destekleyen sorguları barındırır.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Repository
public interface FirmaRepository extends JpaRepository<Firma, Long> {

    /**
     * Veri İzolasyonu: Yalnızca belirli bir plasiyere atanan firmaları sayfalı olarak getirir.
     * 
     * @param plasiyerId Atanan plasiyerin kullanıcı ID'si
     * @param sayfalama Sayfalama ve sıralama parametresi
     * @return Sayfalanmış firma listesi
     */
    Page<Firma> findByAtananPlasiyerId(Long plasiyerId, Pageable sayfalama);

    /**
     * Belirli bir plasiyere atanan tüm firmaların tam listesini döner.
     * 
     * @param plasiyerId Atanan plasiyerin kullanıcı ID'si
     * @return Firma listesi
     */
    List<Firma> findByAtananPlasiyerId(Long plasiyerId);

    /**
     * Unvanına göre firma varlığını kontrol eder (büyük/küçük harf duyarsız).
     * 
     * @param unvan Kontrol edilecek firma unvanı
     * @return Firma mevcutsa true
     */
    boolean existsByUnvanIgnoreCase(String unvan);

    /**
     * Unvana göre firmayı bulur (Excel import işlemi için).
     * 
     * @param unvan Aranan firma unvanı
     * @return Firma nesnesi
     */
    Optional<Firma> findByUnvanIgnoreCase(String unvan);

    /**
     * Şehir veya bölgeye göre sayfalı firma listesi.
     * 
     * @param sehirBolge Şehir veya bölge adı
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalı firmalar
     */
    Page<Firma> findBySehirBolgeIgnoreCase(String sehirBolge, Pageable sayfalama);

    /**
     * Firma unvanında veya şehir/bölge alanında metin araması yapar.
     * 
     * @param aramaKelimesi Arama metni
     * @param sayfalama Sayfalama nesnesi
     * @return Eşleşen sayfalı firmalar
     */
    @Query("SELECT f FROM Firma f WHERE LOWER(f.unvan) LIKE LOWER(CONCAT('%', :aramaKelimesi, '%')) " +
           "OR LOWER(f.sehirBolge) LIKE LOWER(CONCAT('%', :aramaKelimesi, '%'))")
    Page<Firma> firmalardaAra(@Param("aramaKelimesi") String aramaKelimesi, Pageable sayfalama);
}
