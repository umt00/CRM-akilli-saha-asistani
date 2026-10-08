package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Sample;
import com.akillisaha.crm.enums.SampleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleRepository extends JpaRepository<Sample, Long> {

    // Veri İzolasyonu: Sadece temsilcinin numuneleri
    Page<Sample> findByRepresentativeId(Long representativeId, Pageable pageable);

    List<Sample> findByRepresentativeId(Long representativeId);

    Page<Sample> findByCompanyId(Long companyId, Pageable pageable);

    Page<Sample> findByStatus(SampleStatus status, Pageable pageable);

    Page<Sample> findByRepresentativeIdAndStatus(Long representativeId, SampleStatus status, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Sample s WHERE s.status = :status")
    long countByStatus(@Param("status") SampleStatus status);

    @Query("SELECT s.brand, COUNT(s) FROM Sample s GROUP BY s.brand")
    List<Object[]> countSamplesByBrand();
}
