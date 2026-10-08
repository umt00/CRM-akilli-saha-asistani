package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.OfferRequest;
import com.akillisaha.crm.dto.response.OfferResponse;
import com.akillisaha.crm.enums.OfferStatus;
import com.akillisaha.crm.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OfferService {

    Page<OfferResponse> getOffers(CustomUserDetails currentUser, OfferStatus status, Long companyId, Pageable pageable);

    OfferResponse getOfferById(Long id, CustomUserDetails currentUser);

    OfferResponse createOffer(OfferRequest request, CustomUserDetails currentUser);

    OfferResponse updateOfferStatus(Long id, OfferStatus status, CustomUserDetails currentUser);

    void deleteOffer(Long id, CustomUserDetails currentUser);
}
