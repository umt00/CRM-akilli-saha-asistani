package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.CompanyRequest;
import com.akillisaha.crm.dto.request.ContactRequest;
import com.akillisaha.crm.dto.response.CompanyResponse;
import com.akillisaha.crm.dto.response.ContactResponse;
import com.akillisaha.crm.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyService {

    Page<CompanyResponse> getCompanies(CustomUserDetails currentUser, String search, Pageable pageable);

    CompanyResponse getCompanyById(Long id, CustomUserDetails currentUser);

    CompanyResponse createCompany(CompanyRequest request, CustomUserDetails currentUser);

    CompanyResponse updateCompany(Long id, CompanyRequest request, CustomUserDetails currentUser);

    void deleteCompany(Long id, CustomUserDetails currentUser);

    ContactResponse addContact(Long companyId, ContactRequest request, CustomUserDetails currentUser);

    void deleteContact(Long companyId, Long contactId, CustomUserDetails currentUser);
}
