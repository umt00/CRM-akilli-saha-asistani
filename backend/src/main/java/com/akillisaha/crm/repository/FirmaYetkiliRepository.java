package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.FirmaYetkili;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Firma yetkilileri (FirmaYetkili) için veritabanı erişim katmanı.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Repository
public interface FirmaYetkiliRepository extends JpaRepository<FirmaYetkili, Long> {

    /**
     * Belirli bir firmaya bağlı çalışan tüm yetkili kişileri listeler.
     * 
     * @param firmaId Firmanın benzersiz kimliği
     * @return Yetkili kişilerin listesi
     */
    List<FirmaYetkili> findByFirmaId(Long firmaId);
}
