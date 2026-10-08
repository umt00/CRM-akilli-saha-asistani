package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    // Veri İzolasyonu: Yalnızca atanan temsilcinin firmaları
    Page<Company> findByAssignedUserId(Long userId, Pageable pageable);

    List<Company> findByAssignedUserId(Long userId);

    boolean existsByNameIgnoreCase(String name);

    Optional<Company> findByNameIgnoreCase(String name);

    // Şehir / Bölge bazlı filtreleme
    Page<Company> findByCityRegionIgnoreCase(String cityRegion, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.cityRegion) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Company> searchCompanies(@Param("query") String query, Pageable pageable);
}
