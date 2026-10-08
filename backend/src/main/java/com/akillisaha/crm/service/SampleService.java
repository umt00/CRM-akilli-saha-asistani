package com.akillisaha.crm.service;

import com.akillisaha.crm.dto.request.SampleRequest;
import com.akillisaha.crm.dto.response.SampleResponse;
import com.akillisaha.crm.enums.SampleStatus;
import com.akillisaha.crm.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface SampleService {

    Page<SampleResponse> getSamples(CustomUserDetails currentUser, SampleStatus status, Long companyId, Pageable pageable);

    SampleResponse getSampleById(Long id, CustomUserDetails currentUser);

    SampleResponse createSample(SampleRequest request, CustomUserDetails currentUser);

    SampleResponse updateSampleStatus(Long id, SampleStatus status, String resultNotes, CustomUserDetails currentUser);

    void deleteSample(Long id, CustomUserDetails currentUser);

    Map<String, Long> getSampleStats(CustomUserDetails currentUser);
}
