package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.VisitRequest;
import com.akillisaha.crm.dto.response.VisitResponse;
import com.akillisaha.crm.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

public interface VisitService {

    Page<VisitResponse> getVisits(CustomUserDetails currentUser, Long companyId, Pageable pageable);

    VisitResponse getVisitById(Long id, CustomUserDetails currentUser);

    VisitResponse createVisit(VisitRequest request, CustomUserDetails currentUser);

    VisitResponse updateVisit(Long id, VisitRequest request, CustomUserDetails currentUser);

    void deleteVisit(Long id, CustomUserDetails currentUser);

    List<VisitResponse> getDueAgendaVisits(CustomUserDetails currentUser, OffsetDateTime beforeDate);
}
