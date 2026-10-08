package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Teklif;
import com.akillisaha.crm.enums.TeklifDurumu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ticari teklifler ve fırsatlar (Teklif) için veritabanı sorgu katmanı.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Repository
public interface TeklifRepository extends JpaRepository<Teklif, Long> {

    /**
     * Veri İzolasyonu: Yalnızca belirtilen plasiyere ait teklifleri sayfalı listeler.
     * 
     * @param plasiyerId Plasiyer kullanıcı ID'si
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalanmış teklifler
     */
    Page<Teklif> findByPlasiyerId(Long plasiyerId, Pageable sayfalama);

    /**
     * Belirtilen plasiyerin tüm tekliflerinin listesi.
     * 
     * @param plasiyerId Plasiyer kullanıcı ID'si
     * @return Teklif listesi
     */
    List<Teklif> findByPlasiyerId(Long plasiyerId);

    /**
     * Belirli bir firmaya açılmış teklifleri listeler.
     * 
     * @param firmaId Firmanın benzersiz kimliği
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalı teklifler
     */
    Page<Teklif> findByFirmaId(Long firmaId, Pageable sayfalama);

    /**
     * Duruma göre (ACIK, KAZANILDI vb.) teklifleri sayfalı getirir.
     * 
     * @param durum Teklif durumu
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalı teklifler
     */
    Page<Teklif> findByDurum(TeklifDurumu durum, Pageable sayfalama);

    /**
     * Belirli bir durumdaki tekliflerin toplam parasal tutarını hesaplar.
     * 
     * @param durum Teklif durumu
     * @return Toplam parasal tutar
     */
    @Query("SELECT SUM(t.tutar) FROM Teklif t WHERE t.durum = :durum")
    BigDecimal durumaGoreToplamTutar(@Param("durum") TeklifDurumu durum);

    /**
     * Belirli bir durumdaki toplam teklif adedi.
     * 
     * @param durum Teklif durumu
     * @return Toplam adet
     */
    @Query("SELECT COUNT(t) FROM Teklif t WHERE t.durum = :durum")
    long durumaGoreTeklifSayisi(@Param("durum") TeklifDurumu durum);
}
