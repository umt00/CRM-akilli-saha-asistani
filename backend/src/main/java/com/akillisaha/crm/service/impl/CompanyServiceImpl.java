package com.akillisaha.crm.service.impl;

import com.akillisaha.crm.dto.request.CompanyRequest;
import com.akillisaha.crm.dto.request.ContactRequest;
import com.akillisaha.crm.dto.response.CompanyResponse;
import com.akillisaha.crm.dto.response.ContactResponse;
import com.akillisaha.crm.entity.Company;
import com.akillisaha.crm.entity.CompanyContact;
import com.akillisaha.crm.entity.User;
import com.akillisaha.crm.enums.Role;
import com.akillisaha.crm.exception.AccessDeniedCustomException;
import com.akillisaha.crm.exception.ResourceNotFoundException;
import com.akillisaha.crm.repository.CompanyContactRepository;
import com.akillisaha.crm.repository.CompanyRepository;
import com.akillisaha.crm.repository.UserRepository;
import com.akillisaha.crm.security.CustomUserDetails;
import com.akillisaha.crm.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyContactRepository contactRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyResponse> getCompanies(CustomUserDetails currentUser, String search, Pageable pageable) {
        Page<Company> companies;

        if (currentUser.getRole() == Role.ADMIN) {
            if (StringUtils.hasText(search)) {
                companies = companyRepository.searchCompanies(search, pageable);
            } else {
                companies = companyRepository.findAll(pageable);
            }
        } else {
            // Veri İzolasyonu: SALES_REP yalnızca kendisine atanan firmaları görebilir
            companies = companyRepository.findByAssignedUserId(currentUser.getId(), pageable);
        }

        return companies.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long id, CustomUserDetails currentUser) {
        Company company = findCompanyAndValidateAccess(id, currentUser);
        return mapToResponse(company);
    }

    @Override
    @Transactional
    public CompanyResponse createCompany(CompanyRequest request, CustomUserDetails currentUser) {
        User assignedUser;
        if (currentUser.getRole() == Role.ADMIN && request.getAssignedUserId() != null) {
            assignedUser = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Atanan kullanıcı bulunamadı: " + request.getAssignedUserId()));
        } else {
            assignedUser = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + currentUser.getId()));
        }

        Company company = Company.builder()
                .name(request.getName())
                .cityRegion(request.getCityRegion())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .currentSupplierCompetitor(request.getCurrentSupplierCompetitor())
                .suppliedProducts(request.getSuppliedProducts())
                .monthlyConsumption(request.getMonthlyConsumption())
                .purchasedProducts(request.getPurchasedProducts())
                .assignedUser(assignedUser)
                .build();

        Company savedCompany = companyRepository.save(company);
        log.info("Yeni firma oluşturuldu: id={}, name={}", savedCompany.getId(), savedCompany.getName());
        return mapToResponse(savedCompany);
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest request, CustomUserDetails currentUser) {
        Company company = findCompanyAndValidateAccess(id, currentUser);

        company.setName(request.getName());
        company.setCityRegion(request.getCityRegion());
        company.setAddress(request.getAddress());
        company.setPhone(request.getPhone());
        company.setEmail(request.getEmail());
        company.setCurrentSupplierCompetitor(request.getCurrentSupplierCompetitor());
        company.setSuppliedProducts(request.getSuppliedProducts());
        company.setMonthlyConsumption(request.getMonthlyConsumption());
        company.setPurchasedProducts(request.getPurchasedProducts());

        if (currentUser.getRole() == Role.ADMIN && request.getAssignedUserId() != null) {
            User newAssignee = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Atanan kullanıcı bulunamadı: " + request.getAssignedUserId()));
            company.setAssignedUser(newAssignee);
        }

        Company updatedCompany = companyRepository.save(company);
        return mapToResponse(updatedCompany);
    }

    @Override
    @Transactional
    public void deleteCompany(Long id, CustomUserDetails currentUser) {
        Company company = findCompanyAndValidateAccess(id, currentUser);
        companyRepository.delete(company);
        log.info("Firma silindi: id={}", id);
    }

    @Override
    @Transactional
    public ContactResponse addContact(Long companyId, ContactRequest request, CustomUserDetails currentUser) {
        Company company = findCompanyAndValidateAccess(companyId, currentUser);

        CompanyContact contact = CompanyContact.builder()
                .company(company)
                .fullName(request.getFullName())
                .departmentRole(request.getDepartmentRole())
                .phone(request.getPhone())
                .email(request.getEmail())
                .notes(request.getNotes())
                .build();

        CompanyContact savedContact = contactRepository.save(contact);
        return mapContactToResponse(savedContact);
    }

    @Override
    @Transactional
    public void deleteContact(Long companyId, Long contactId, CustomUserDetails currentUser) {
        findCompanyAndValidateAccess(companyId, currentUser);
        CompanyContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Yetkili bulunamadı: " + contactId));
        contactRepository.delete(contact);
    }

    private Company findCompanyAndValidateAccess(Long companyId, CustomUserDetails currentUser) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Firma bulunamadı: " + companyId));

        if (currentUser.getRole() != Role.ADMIN) {
            if (company.getAssignedUser() == null || !company.getAssignedUser().getId().equals(currentUser.getId())) {
                log.warn("Erişim reddedildi: Kullanıcı {} firma {} üzerinde yetkili değil", currentUser.getId(), companyId);
                throw new AccessDeniedCustomException("Bu firmaya erişim yetkiniz bulunmamaktadır");
            }
        }
        return company;
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .cityRegion(company.getCityRegion())
                .address(company.getAddress())
                .phone(company.getPhone())
                .email(company.getEmail())
                .currentSupplierCompetitor(company.getCurrentSupplierCompetitor())
                .suppliedProducts(company.getSuppliedProducts())
                .monthlyConsumption(company.getMonthlyConsumption())
                .purchasedProducts(company.getPurchasedProducts())
                .assignedUserId(company.getAssignedUser() != null ? company.getAssignedUser().getId() : null)
                .assignedUserName(company.getAssignedUser() != null ? company.getAssignedUser().getFullName() : null)
                .contacts(company.getContacts().stream()
                        .map(this::mapContactToResponse)
                        .collect(Collectors.toList()))
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    private ContactResponse mapContactToResponse(CompanyContact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .companyId(contact.getCompany().getId())
                .fullName(contact.getFullName())
                .departmentRole(contact.getDepartmentRole())
                .phone(contact.getPhone())
                .email(contact.getEmail())
                .notes(contact.getNotes())
                .createdAt(contact.getCreatedAt())
                .build();
    }
}
