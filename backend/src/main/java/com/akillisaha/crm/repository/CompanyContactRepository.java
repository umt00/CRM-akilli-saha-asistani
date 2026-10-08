package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.CompanyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyContactRepository extends JpaRepository<CompanyContact, Long> {

    List<CompanyContact> findByCompanyId(Long companyId);
}
