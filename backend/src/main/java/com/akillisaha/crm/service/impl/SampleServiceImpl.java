package com.akillisaha.crm.service.impl;

import com.akillisaha.crm.dto.request.SampleRequest;
import com.akillisaha.crm.dto.response.SampleResponse;
import com.akillisaha.crm.entity.*;
import com.akillisaha.crm.enums.Role;
import com.akillisaha.crm.enums.SampleStatus;
import com.akillisaha.crm.exception.AccessDeniedCustomException;
import com.akillisaha.crm.exception.ResourceNotFoundException;
import com.akillisaha.crm.repository.*;
import com.akillisaha.crm.security.CustomUserDetails;
import com.akillisaha.crm.service.SampleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleServiceImpl implements SampleService {

    private final SampleRepository sampleRepository;
    private final CompanyRepository companyRepository;
    private final VisitRepository visitRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<SampleResponse> getSamples(CustomUserDetails currentUser, SampleStatus status, Long companyId, Pageable pageable) {
        Page<Sample> samples;

        if (companyId != null) {
            validateCompanyAccess(companyId, currentUser);
            samples = sampleRepository.findByCompanyId(companyId, pageable);
        } else if (currentUser.getRole() == Role.ADMIN) {
            if (status != null) {
                samples = sampleRepository.findByStatus(status, pageable);
            } else {
                samples = sampleRepository.findAll(pageable);
            }
        } else {
            // Veri İzolasyonu: SALES_REP yalnızca kendi numunelerini görebilir
            if (status != null) {
                samples = sampleRepository.findByRepresentativeIdAndStatus(currentUser.getId(), status, pageable);
            } else {
                samples = sampleRepository.findByRepresentativeId(currentUser.getId(), pageable);
            }
        }

        return samples.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SampleResponse getSampleById(Long id, CustomUserDetails currentUser) {
        Sample sample = findSampleAndValidateAccess(id, currentUser);
        return mapToResponse(sample);
    }

    @Override
    @Transactional
    public SampleResponse createSample(SampleRequest request, CustomUserDetails currentUser) {
        Company company = validateCompanyAccess(request.getCompanyId(), currentUser);
        User representative = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + currentUser.getId()));

        Visit visit = null;
        if (request.getVisitId() != null) {
            visit = visitRepository.findById(request.getVisitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ziyaret bulunamadı: " + request.getVisitId()));
        }

        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı: " + request.getProductId()));
        }

        Sample sample = Sample.builder()
                .company(company)
                .visit(visit)
                .representative(representative)
                .product(product)
                .productName(request.getProductName())
                .brand(request.getBrand())
                .quantity(request.getQuantity())
                .status(request.getStatus() != null ? request.getStatus() : SampleStatus.BEKLEMEDE)
                .resultNotes(request.getResultNotes())
                .sentDate(request.getSentDate() != null ? request.getSentDate() : OffsetDateTime.now())
                .evaluatedDate(request.getEvaluatedDate())
                .build();

        Sample savedSample = sampleRepository.save(sample);
        log.info("Numune kaydı oluşturuldu: id={}, product={}", savedSample.getId(), savedSample.getProductName());
        return mapToResponse(savedSample);
    }

    @Override
    @Transactional
    public SampleResponse updateSampleStatus(Long id, SampleStatus status, String resultNotes, CustomUserDetails currentUser) {
        Sample sample = findSampleAndValidateAccess(id, currentUser);

        sample.setStatus(status);
        if (resultNotes != null) {
            sample.setResultNotes(resultNotes);
        }
        if (status != SampleStatus.BEKLEMEDE && status != SampleStatus.TEST_ASAMASINDA) {
            sample.setEvaluatedDate(OffsetDateTime.now());
        }

        Sample updatedSample = sampleRepository.save(sample);
        log.info("Numune durumu güncellendi: id={}, yeniDurum={}", id, status);
        return mapToResponse(updatedSample);
    }

    @Override
    @Transactional
    public void deleteSample(Long id, CustomUserDetails currentUser) {
        Sample sample = findSampleAndValidateAccess(id, currentUser);
        sampleRepository.delete(sample);
        log.info("Numune silindi: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getSampleStats(CustomUserDetails currentUser) {
        Map<String, Long> stats = new HashMap<>();
        for (SampleStatus status : SampleStatus.values()) {
            stats.put(status.name(), sampleRepository.countByStatus(status));
        }

        List<Object[]> brandStats = sampleRepository.countSamplesByBrand();
        for (Object[] row : brandStats) {
            if (row[0] != null) {
                stats.put("BRAND_" + row[0].toString(), (Long) row[1]);
            }
        }
        return stats;
    }

    private Company validateCompanyAccess(Long companyId, CustomUserDetails currentUser) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Firma bulunamadı: " + companyId));

        if (currentUser.getRole() != Role.ADMIN) {
            if (company.getAssignedUser() == null || !company.getAssignedUser().getId().equals(currentUser.getId())) {
                throw new AccessDeniedCustomException("Bu firmaya ait numune işlemine yetkiniz bulunmamaktadır");
            }
        }
        return company;
    }

    private Sample findSampleAndValidateAccess(Long sampleId, CustomUserDetails currentUser) {
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new ResourceNotFoundException("Numune bulunamadı: " + sampleId));

        if (currentUser.getRole() != Role.ADMIN) {
            if (!sample.getRepresentative().getId().equals(currentUser.getId())) {
                throw new AccessDeniedCustomException("Bu numuneye erişim yetkiniz bulunmamaktadır");
            }
        }
        return sample;
    }

    private SampleResponse mapToResponse(Sample sample) {
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
}
