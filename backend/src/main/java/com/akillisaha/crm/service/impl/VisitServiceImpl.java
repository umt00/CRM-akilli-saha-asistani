package com.akillisaha.crm.service.impl;

import com.akillisaha.crm.dto.request.VisitRequest;
import com.akillisaha.crm.dto.response.OfferResponse;
import com.akillisaha.crm.dto.response.SampleResponse;
import com.akillisaha.crm.dto.response.VisitResponse;
import com.akillisaha.crm.entity.*;
import com.akillisaha.crm.enums.OfferStatus;
import com.akillisaha.crm.enums.Role;
import com.akillisaha.crm.enums.SampleStatus;
import com.akillisaha.crm.exception.AccessDeniedCustomException;
import com.akillisaha.crm.exception.ResourceNotFoundException;
import com.akillisaha.crm.repository.*;
import com.akillisaha.crm.security.CustomUserDetails;
import com.akillisaha.crm.service.VisitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContactRepository contactRepository;
    private final UserRepository userRepository;
    private final SampleRepository sampleRepository;
    private final OfferRepository offerRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<VisitResponse> getVisits(CustomUserDetails currentUser, Long companyId, Pageable pageable) {
        Page<Visit> visits;

        if (companyId != null) {
            // Belirli bir firmanın ziyaretleri (erişim kontrolü yapılır)
            validateCompanyAccess(companyId, currentUser);
            visits = visitRepository.findByCompanyId(companyId, pageable);
        } else if (currentUser.getRole() == Role.ADMIN) {
            visits = visitRepository.findAll(pageable);
        } else {
            // Veri İzolasyonu: SALES_REP yalnızca kendi ziyaretlerini görebilir
            visits = visitRepository.findByRepresentativeId(currentUser.getId(), pageable);
        }

        return visits.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VisitResponse getVisitById(Long id, CustomUserDetails currentUser) {
        Visit visit = findVisitAndValidateAccess(id, currentUser);
        return mapToResponse(visit);
    }

    @Override
    @Transactional
    public VisitResponse createVisit(VisitRequest request, CustomUserDetails currentUser) {
        Company company = validateCompanyAccess(request.getCompanyId(), currentUser);
        User representative = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + currentUser.getId()));

        CompanyContact contact = null;
        if (request.getContactId() != null) {
            contact = contactRepository.findById(request.getContactId())
                    .orElseThrow(() -> new ResourceNotFoundException("Yetkili bulunamadı: " + request.getContactId()));
        }

        // 1. Ziyaret Nesnesini Oluştur
        Visit visit = Visit.builder()
                .company(company)
                .contact(contact)
                .representative(representative)
                .visitDate(request.getVisitDate())
                .topic(request.getTopic())
                .suppliedProducts(request.getSuppliedProducts())
                .monthlyConsumption(request.getMonthlyConsumption())
                .purchasedProducts(request.getPurchasedProducts())
                .currentSupplierCompetitor(request.getCurrentSupplierCompetitor())
                .hasSample(Boolean.TRUE.equals(request.getHasSample()))
                .hasOffer(Boolean.TRUE.equals(request.getHasOffer()))
                .nextAction(request.getNextAction())
                .nextVisitDate(request.getNextVisitDate())
                .notes(request.getNotes())
                .build();

        Visit savedVisit = visitRepository.save(visit);

        // 2. İş Kuralı: Numune Verildiyse Otomatik Sample Kaydı Aç (Katman 4)
        if (Boolean.TRUE.equals(request.getHasSample()) && StringUtils.hasText(request.getSampleProductName())) {
            Sample sample = Sample.builder()
                    .visit(savedVisit)
                    .company(company)
                    .representative(representative)
                    .productName(request.getSampleProductName())
                    .brand(request.getSampleBrand())
                    .quantity(StringUtils.hasText(request.getSampleQuantity()) ? request.getSampleQuantity() : "1 ADET")
                    .status(SampleStatus.BEKLEMEDE)
                    .resultNotes(request.getSampleResultNotes())
                    .sentDate(savedVisit.getVisitDate())
                    .build();
            sampleRepository.save(sample);
            savedVisit.getSamples().add(sample);
            log.info("Ziyarete bağlı otomatik numune açıldı: visitId={}, product={}", savedVisit.getId(), sample.getProductName());
        }

        // 3. İş Kuralı: Teklif Verildiyse Otomatik Offer Kaydı Aç
        if (Boolean.TRUE.equals(request.getHasOffer()) && StringUtils.hasText(request.getOfferTitle())) {
            Offer offer = Offer.builder()
                    .company(company)
                    .visit(savedVisit)
                    .representative(representative)
                    .title(request.getOfferTitle())
                    .amount(request.getOfferAmount())
                    .currency(StringUtils.hasText(request.getOfferCurrency()) ? request.getOfferCurrency() : "TRY")
                    .status(OfferStatus.ACIK)
                    .build();
            offerRepository.save(offer);
            savedVisit.getOffers().add(offer);
            log.info("Ziyarete bağlı otomatik teklif açıldı: visitId={}, title={}", savedVisit.getId(), offer.getTitle());
        }

        // 4. Firma Bilgilerini Ziyaret Bilgileriyle Güncelle (Excel Senkronizasyonu)
        updateCompanyMarketInfo(company, request);

        log.info("Ziyaret başarıyla kaydedildi: visitId={}, company={}", savedVisit.getId(), company.getName());
        return mapToResponse(savedVisit);
    }

    @Override
    @Transactional
    public VisitResponse updateVisit(Long id, VisitRequest request, CustomUserDetails currentUser) {
        Visit visit = findVisitAndValidateAccess(id, currentUser);

        if (request.getContactId() != null) {
            CompanyContact contact = contactRepository.findById(request.getContactId())
                    .orElseThrow(() -> new ResourceNotFoundException("Yetkili bulunamadı: " + request.getContactId()));
            visit.setContact(contact);
        }

        visit.setVisitDate(request.getVisitDate());
        visit.setTopic(request.getTopic());
        visit.setSuppliedProducts(request.getSuppliedProducts());
        visit.setMonthlyConsumption(request.getMonthlyConsumption());
        visit.setPurchasedProducts(request.getPurchasedProducts());
        visit.setCurrentSupplierCompetitor(request.getCurrentSupplierCompetitor());
        visit.setHasSample(Boolean.TRUE.equals(request.getHasSample()));
        visit.setHasOffer(Boolean.TRUE.equals(request.getHasOffer()));
        visit.setNextAction(request.getNextAction());
        visit.setNextVisitDate(request.getNextVisitDate());
        visit.setNotes(request.getNotes());

        Visit updatedVisit = visitRepository.save(visit);
        return mapToResponse(updatedVisit);
    }

    @Override
    @Transactional
    public void deleteVisit(Long id, CustomUserDetails currentUser) {
        Visit visit = findVisitAndValidateAccess(id, currentUser);
        visitRepository.delete(visit);
        log.info("Ziyaret silindi: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisitResponse> getDueAgendaVisits(CustomUserDetails currentUser, OffsetDateTime beforeDate) {
        OffsetDateTime queryDate = beforeDate != null ? beforeDate : OffsetDateTime.now().plusDays(7);
        List<Visit> dueVisits = visitRepository.findDueVisits(queryDate);

        if (currentUser.getRole() != Role.ADMIN) {
            dueVisits = dueVisits.stream()
                    .filter(v -> v.getRepresentative().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }

        return dueVisits.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private Company validateCompanyAccess(Long companyId, CustomUserDetails currentUser) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Firma bulunamadı: " + companyId));

        if (currentUser.getRole() != Role.ADMIN) {
            if (company.getAssignedUser() == null || !company.getAssignedUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedCustomException("Bu firmaya ait ziyaret işlemine yetkiniz bulunmamaktadır");
            }
        }
        return company;
    }

    private Visit findVisitAndValidateAccess(Long visitId, CustomUserDetails currentUser) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Ziyaret bulunamadı: " + visitId));

        if (currentUser.getRole() != Role.ADMIN) {
            if (!visit.getRepresentative().getId().equals(currentUser.getId())) {
                throw new AccessDeniedCustomException("Bu ziyarete erişim yetkiniz bulunmamaktadır");
            }
        }
        return visit;
    }

    private void updateCompanyMarketInfo(Company company, VisitRequest request) {
        boolean modified = false;
        if (StringUtils.hasText(request.getSuppliedProducts())) {
            company.setSuppliedProducts(request.getSuppliedProducts());
            modified = true;
        }
        if (StringUtils.hasText(request.getMonthlyConsumption())) {
            company.setMonthlyConsumption(request.getMonthlyConsumption());
            modified = true;
        }
        if (StringUtils.hasText(request.getPurchasedProducts())) {
            company.setPurchasedProducts(request.getPurchasedProducts());
            modified = true;
        }
        if (StringUtils.hasText(request.getCurrentSupplierCompetitor())) {
            company.setCurrentSupplierCompetitor(request.getCurrentSupplierCompetitor());
            modified = true;
        }
        if (modified) {
            companyRepository.save(company);
        }
    }

    private VisitResponse mapToResponse(Visit visit) {
        return VisitResponse.builder()
                .id(visit.getId())
                .companyId(visit.getCompany().getId())
                .companyName(visit.getCompany().getName())
                .cityRegion(visit.getCompany().getCityRegion())
                .contactId(visit.getContact() != null ? visit.getContact().getId() : null)
                .contactName(visit.getContact() != null ? visit.getContact().getFullName() : null)
                .contactDepartmentRole(visit.getContact() != null ? visit.getContact().getDepartmentRole() : null)
                .representativeId(visit.getRepresentative().getId())
                .representativeName(visit.getRepresentative().getFullName())
                .visitDate(visit.getVisitDate())
                .topic(visit.getTopic())
                .suppliedProducts(visit.getSuppliedProducts())
                .monthlyConsumption(visit.getMonthlyConsumption())
                .purchasedProducts(visit.getPurchasedProducts())
                .currentSupplierCompetitor(visit.getCurrentSupplierCompetitor())
                .hasSample(visit.getHasSample())
                .hasOffer(visit.getHasOffer())
                .nextAction(visit.getNextAction())
                .nextVisitDate(visit.getNextVisitDate())
                .notes(visit.getNotes())
                .samples(visit.getSamples().stream().map(this::mapSampleToResponse).collect(Collectors.toList()))
                .offers(visit.getOffers().stream().map(this::mapOfferToResponse).collect(Collectors.toList()))
                .createdAt(visit.getCreatedAt())
                .build();
    }

    private SampleResponse mapSampleToResponse(Sample sample) {
        return SampleResponse.builder()
                .id(sample.getId())
                .visitId(sample.getVisit() != null ? sample.getVisit().getId() : null)
                .companyId(sample.getCompany().getId())
                .companyName(sample.getCompany().getName())
                .representativeId(sample.getRepresentative().getId())
                .representativeName(sample.getRepresentative().getFullName())
                .productId(sample.getProduct() != null ? sample.getProduct().getId() : null)
                .productName(sample.getProductName())
                .brand(sample.getBrand())
                .quantity(sample.getQuantity())
                .status(sample.getStatus())
                .resultNotes(sample.getResultNotes())
                .sentDate(sample.getSentDate())
                .evaluatedDate(sample.getEvaluatedDate())
                .createdAt(sample.getCreatedAt())
                .build();
    }

    private OfferResponse mapOfferToResponse(Offer offer) {
        return OfferResponse.builder()
                .id(offer.getId())
                .companyId(offer.getCompany().getId())
                .companyName(offer.getCompany().getName())
                .visitId(offer.getVisit() != null ? offer.getVisit().getId() : null)
                .representativeId(offer.getRepresentative().getId())
                .representativeName(offer.getRepresentative().getFullName())
                .title(offer.getTitle())
                .amount(offer.getAmount())
                .currency(offer.getCurrency())
                .status(offer.getStatus())
                .notes(offer.getNotes())
                .validUntil(offer.getValidUntil())
                .createdAt(offer.getCreatedAt())
                .build();
    }
}
