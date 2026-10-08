package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Ziyaret;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Saha ziyaretleri (Ziyaret) için veritabanı sorgu ve erişim katmanı.
 * Plasiyer bazlı veri izolasyonu ve ajanda randevu sorgularını yönetir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Repository
public interface ZiyaretRepository extends JpaRepository<Ziyaret, Long> {

    /**
     * Veri İzolasyonu: Yalnızca belirtilen plasiyere ait ziyaretleri sayfalı olarak döner.
     * 
     * @param plasiyerId Satış temsilcisinin kullanıcı ID'si
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalanmış ziyaretler
     */
    Page<Ziyaret> findByPlasiyerId(Long plasiyerId, Pageable sayfalama);

    /**
     * Belirtilen plasiyerin gerçekleştirdiği tüm ziyaretlerin listesi.
     * 
     * @param plasiyerId Plasiyer kullanıcı ID'si
     * @return Ziyaret listesi
     */
    List<Ziyaret> findByPlasiyerId(Long plasiyerId);

    /**
     * Belirli bir firmaya yapılmış tüm ziyaretleri sayfalı listeler.
     * 
     * @param firmaId Firmanın benzersiz kimliği
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalanmış ziyaretler
     */
    Page<Ziyaret> findByFirmaId(Long firmaId, Pageable sayfalama);

    /**
     * İki tarih arasında gerçekleşen tüm ziyaretleri listeler.
     * 
     * @param baslangicTarihi Başlangıç zaman damgası
     * @param bitisTarihi Bitiş zaman damgası
     * @return Ziyaret listesi
     */
    List<Ziyaret> findByZiyaretTarihiBetween(OffsetDateTime baslangicTarihi, OffsetDateTime bitisTarihi);

    /**
     * Belirli bir plasiyerin iki tarih arasındaki ziyaretlerini filtreler.
     * 
     * @param plasiyerId Plasiyer kullanıcı ID'si
     * @param baslangicTarihi Başlangıç zamanı
     * @param bitisTarihi Bitiş zamanı
     * @return Ziyaret listesi
     */
    List<Ziyaret> findByPlasiyerIdAndZiyaretTarihiBetween(Long plasiyerId, OffsetDateTime baslangicTarihi, OffsetDateTime bitisTarihi);

    /**
     * Ajanda / Yaklaşan Ziyaretler: Sonraki randevu tarihi belirtilen tarihten önce veya eşit olan kayıtları getirir.
     * 
     * @param tarih Karşılaştırma zamanı
     * @return Vadesi gelmiş/yaklaşan ziyaretler
     */
    @Query("SELECT z FROM Ziyaret z WHERE z.sonrakiZiyaretTarihi IS NOT NULL AND z.sonrakiZiyaretTarihi <= :tarih")
    List<Ziyaret> vadesiGelenZiyaretleriGetir(@Param("tarih") OffsetDateTime tarih);

    /**
     * Numune bırakılmış toplam ziyaret sayısı.
     * 
     * @return Numuneli ziyaret adedi
     */
    @Query("SELECT COUNT(z) FROM Ziyaret z WHERE z.numuneVerildiMi = true")
    long numuneliZiyaretSayisi();

    /**
     * Teklif açılmış toplam ziyaret sayısı.
     * 
     * @return Teklifli ziyaret adedi
     */
    @Query("SELECT COUNT(z) FROM Ziyaret z WHERE z.teklifVerildiMi = true")
    long teklifliZiyaretSayisi();
}
