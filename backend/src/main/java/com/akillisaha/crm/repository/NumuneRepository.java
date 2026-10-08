package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Numune;
import com.akillisaha.crm.enums.NumuneDurumu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Numune takibi (Numune) için veritabanı sorgu katmanı.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Repository
public interface NumuneRepository extends JpaRepository<Numune, Long> {

    /**
     * Veri İzolasyonu: Yalnızca belirtilen plasiyere ait numuneleri sayfalı listeler.
     * 
     * @param plasiyerId Plasiyer kullanıcı ID'si
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalanmış numuneler
     */
    Page<Numune> findByPlasiyerId(Long plasiyerId, Pageable sayfalama);

    /**
     * Belirtilen plasiyerin tüm numunelerinin listesi.
     * 
     * @param plasiyerId Plasiyer kullanıcı ID'si
     * @return Numune listesi
     */
    List<Numune> findByPlasiyerId(Long plasiyerId);

    /**
     * Belirli bir firmaya bırakılmış numuneleri getirir.
     * 
     * @param firmaId Firmanın benzersiz kimliği
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalanmış numuneler
     */
    Page<Numune> findByFirmaId(Long firmaId, Pageable sayfalama);

    /**
     * Belirli bir yaşam döngüsü durumundaki numuneleri sayfalı listeler.
     * 
     * @param durum Numune durumu (BEKLEMEDE, BEGENDI vb.)
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalı numuneler
     */
    Page<Numune> findByDurum(NumuneDurumu durum, Pageable sayfalama);

    /**
     * Belirli bir plasiyerin ve belirli bir durumdaki numunelerini getirir.
     * 
     * @param plasiyerId Plasiyer ID'si
     * @param durum Numune durumu
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalı numuneler
     */
    Page<Numune> findByPlasiyerIdAndDurum(Long plasiyerId, NumuneDurumu durum, Pageable sayfalama);

    /**
     * Belirli bir durumdaki toplam numune sayısını döner.
     * 
     * @param durum Numune durumu
     * @return Toplam adet
     */
    @Query("SELECT COUNT(n) FROM Numune n WHERE n.durum = :durum")
    long durumaGoreNumuneSayisi(@Param("durum") NumuneDurumu durum);

    /**
     * Marka bazlı (Kerry, Cargill vb.) toplam numune dağılımını gruplayarak getirir.
     * 
     * @return Marka ve adet ikilileri
     */
    @Query("SELECT n.marka, COUNT(n) FROM Numune n GROUP BY n.marka")
    List<Object[]> markayaGoreNumuneSayilari();
}
