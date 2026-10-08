package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.CompanyRequest;
import com.akillisaha.crm.dto.request.ContactRequest;
import com.akillisaha.crm.dto.response.ApiResponse;
import com.akillisaha.crm.dto.response.CompanyResponse;
import com.akillisaha.crm.dto.response.ContactResponse;
import com.akillisaha.crm.security.CustomUserDetails;
import com.akillisaha.crm.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Müşteri / Firmalar (Companies)", description = "Müşteri portföyü ve yetkili kişi yönetimi (İzolasyon Korumalı)")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @Operation(summary = "Firma Listesi", description = "Temsilciye göre izole edilmiş sayfalanabilir firma listesi")
    public ResponseEntity<ApiResponse<Page<CompanyResponse>>> getCompanies(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CompanyResponse> response = companyService.getCompanies(currentUser, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Firma Detayı", description = "ID'ye göre firma ve yetkili bilgilerini döner")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompanyById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        CompanyResponse response = companyService.getCompanyById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Yeni Firma Ekle", description = "Yeni müşteri/firma kaydı oluşturur")
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CompanyRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        CompanyResponse response = companyService.createCompany(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Firma başarıyla oluşturuldu", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Firma Güncelle", description = "Mevcut firma bilgilerini günceller")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        CompanyResponse response = companyService.updateCompany(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Firma başarıyla güncellendi", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Firma Sil", description = "Firmayı ve bağlı kayıtları siler")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        companyService.deleteCompany(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Firma başarıyla silindi", null));
    }

    @PostMapping("/{companyId}/contacts")
    @Operation(summary = "Yetkili Kişi Ekle", description = "Firmaya yeni yetkili/şef/satın almacı ekler")
    public ResponseEntity<ApiResponse<ContactResponse>> addContact(
            @PathVariable Long companyId,
            @Valid @RequestBody ContactRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ContactResponse response = companyService.addContact(companyId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Yetkili başarıyla eklendi", response));
    }

    @DeleteMapping("/{companyId}/contacts/{contactId}")
    @Operation(summary = "Yetkili Kişi Sil", description = "Firmaya bağlı yetkiliyi siler")
    public ResponseEntity<ApiResponse<Void>> deleteContact(
            @PathVariable Long companyId,
            @PathVariable Long contactId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        companyService.deleteContact(companyId, contactId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Yetkili başarıyla silindi", null));
    }
}
