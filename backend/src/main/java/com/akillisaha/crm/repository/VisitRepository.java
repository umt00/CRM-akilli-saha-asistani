package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Visit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    // Veri İzolasyonu: Sadece temsilcinin kendi ziyaretleri
    Page<Visit> findByRepresentativeId(Long representativeId, Pageable pageable);

    List<Visit> findByRepresentativeId(Long representativeId);

    Page<Visit> findByCompanyId(Long companyId, Pageable pageable);

    // Tarih aralığına göre ziyaretler
    List<Visit> findByVisitDateBetween(OffsetDateTime startDate, OffsetDateTime endDate);

    List<Visit> findByRepresentativeIdAndVisitDateBetween(Long representativeId, OffsetDateTime startDate, OffsetDateTime endDate);

    // Yaklaşan veya geciken ziyaretler (Ajanda)
    @Query("SELECT v FROM Visit v WHERE v.nextVisitDate IS NOT NULL AND v.nextVisitDate <= :date")
    List<Visit> findDueVisits(@Param("date") OffsetDateTime date);

    @Query("SELECT COUNT(v) FROM Visit v WHERE v.hasSample = true")
    long countVisitsWithSample();

    @Query("SELECT COUNT(v) FROM Visit v WHERE v.hasOffer = true")
    long countVisitsWithOffer();
}
