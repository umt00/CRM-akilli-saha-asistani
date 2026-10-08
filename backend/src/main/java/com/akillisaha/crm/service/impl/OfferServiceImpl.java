package com.akillisaha.crm.service.impl;

import com.akillisaha.crm.dto.request.OfferRequest;
import com.akillisaha.crm.dto.response.OfferResponse;
import com.akillisaha.crm.entity.Company;
import com.akillisaha.crm.entity.Offer;
import com.akillisaha.crm.entity.User;
import com.akillisaha.crm.entity.Visit;
import com.akillisaha.crm.enums.OfferStatus;
import com.akillisaha.crm.enums.Role;
import com.akillisaha.crm.exception.AccessDeniedCustomException;
import com.akillisaha.crm.exception.ResourceNotFoundException;
import com.akillisaha.crm.repository.CompanyRepository;
import com.akillisaha.crm.repository.OfferRepository;
import com.akillisaha.crm.repository.UserRepository;
import com.akillisaha.crm.repository.VisitRepository;
import com.akillisaha.crm.security.CustomUserDetails;
import com.akillisaha.crm.service.OfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final CompanyRepository companyRepository;
    private final VisitRepository visitRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<OfferResponse> getOffers(CustomUserDetails currentUser, OfferStatus status, Long companyId, Pageable pageable) {
        Page<Offer> offers;

        if (companyId != null) {
            validateCompanyAccess(companyId, currentUser);
            offers = offerRepository.findByCompanyId(companyId, pageable);
        } else if (currentUser.getRole() == Role.ADMIN) {
            if (status != null) {
                offers = offerRepository.findByStatus(status, pageable);
            } else {
                offers = offerRepository.findAll(pageable);
            }
        } else {
            // Veri İzolasyonu: SALES_REP yalnızca kendi tekliflerini görebilir
            offers = offerRepository.findByRepresentativeId(currentUser.getId(), pageable);
        }

        return offers.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OfferResponse getOfferById(Long id, CustomUserDetails currentUser) {
        Offer offer = findOfferAndValidateAccess(id, currentUser);
        return mapToResponse(offer);
    }

    @Override
    @Transactional
    public OfferResponse createOffer(OfferRequest request, CustomUserDetails currentUser) {
        Company company = validateCompanyAccess(request.getCompanyId(), currentUser);
        User representative = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + currentUser.getId()));

        Visit visit = null;
        if (request.getVisitId() != null) {
            visit = visitRepository.findById(request.getVisitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ziyaret bulunamadı: " + request.getVisitId()));
        }

        Offer offer = Offer.builder()
                .company(company)
                .visit(visit)
                .representative(representative)
                .title(request.getTitle())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "TRY")
                .status(request.getStatus() != null ? request.getStatus() : OfferStatus.ACIK)
                .notes(request.getNotes())
                .validUntil(request.getValidUntil())
                .build();

        Offer savedOffer = offerRepository.save(offer);
        log.info("Teklif kaydı açıldı: id={}, title={}, tutar={}", savedOffer.getId(), savedOffer.getTitle(), savedOffer.getAmount());
        return mapToResponse(savedOffer);
    }

    @Override
    @Transactional
    public OfferResponse updateOfferStatus(Long id, OfferStatus status, CustomUserDetails currentUser) {
        Offer offer = findOfferAndValidateAccess(id, currentUser);
        offer.setStatus(status);
        Offer updatedOffer = offerRepository.save(offer);
        log.info("Teklif durumu güncellendi: id={}, yeniDurum={}", id, status);
        return mapToResponse(updatedOffer);
    }

    @Override
    @Transactional
    public void deleteOffer(Long id, CustomUserDetails currentUser) {
        Offer offer = findOfferAndValidateAccess(id, currentUser);
        offerRepository.delete(offer);
        log.info("Teklif silindi: id={}", id);
    }

    private Company validateCompanyAccess(Long companyId, CustomUserDetails currentUser) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Firma bulunamadı: " + companyId));

        if (currentUser.getRole() != Role.ADMIN) {
            if (company.getAssignedUser() == null || !company.getAssignedUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedCustomException("Bu firmaya ait teklif işlemine yetkiniz bulunmamaktadır");
            }
        }
        return company;
    }

    private Offer findOfferAndValidateAccess(Long offerId, CustomUserDetails currentUser) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Teklif bulunamadı: " + offerId));

        if (currentUser.getRole() != Role.ADMIN) {
            if (!offer.getRepresentative().getId().equals(currentUser.getId())) {
                throw new AccessDeniedCustomException("Bu teklife erişim yetkiniz bulunmamaktadır");
            }
        }
        return offer;
    }

    private OfferResponse mapToResponse(Offer offer) {
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
