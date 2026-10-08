package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Offer;
import com.akillisaha.crm.enums.OfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    // Veri İzolasyonu: Sadece temsilcinin teklifleri
    Page<Offer> findByRepresentativeId(Long representativeId, Pageable pageable);

    List<Offer> findByRepresentativeId(Long representativeId);

    Page<Offer> findByCompanyId(Long companyId, Pageable pageable);

    Page<Offer> findByStatus(OfferStatus status, Pageable pageable);

    @Query("SELECT SUM(o.amount) FROM Offer o WHERE o.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") OfferStatus status);

    @Query("SELECT COUNT(o) FROM Offer o WHERE o.status = :status")
    long countByStatus(@Param("status") OfferStatus status);
}
