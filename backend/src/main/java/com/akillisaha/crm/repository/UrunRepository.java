package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Urun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Ürün kataloğu (Urun) için veritabanı sorgu katmanı.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Repository
public interface UrunRepository extends JpaRepository<Urun, Long> {

    /**
     * Satışta aktif olan tüm ürünleri getirir.
     * 
     * @return Aktif ürünler listesi
     */
    List<Urun> findByAktifMiTrue();

    /**
     * Markaya göre (Kerry, Cargill, Orkide vb.) ürünleri listeler.
     * 
     * @param marka Ürün markası
     * @return Markaya ait ürünler
     */
    List<Urun> findByMarkaIgnoreCase(String marka);

    /**
     * Ürün koduna göre ürün arar.
     * 
     * @param kod Ürünün stok kodu
     * @return Varsa ürün nesnesi
     */
    Optional<Urun> findByKodIgnoreCase(String kod);
}
