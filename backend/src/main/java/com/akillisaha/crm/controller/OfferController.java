package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.OfferRequest;
import com.akillisaha.crm.dto.response.ApiResponse;
import com.akillisaha.crm.dto.response.OfferResponse;
import com.akillisaha.crm.enums.OfferStatus;
import com.akillisaha.crm.security.CustomUserDetails;
import com.akillisaha.crm.service.OfferService;
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
@RequestMapping("/api/v1/offers")
@RequiredArgsConstructor
@Tag(name = "Teklifler & Fırsatlar (Offers)", description = "Saha teklifleri, fiyatlar ve kazanım durumu yönetimi")
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    @Operation(summary = "Teklif Listesi", description = "Temsilciye göre izole teklif listesi")
    public ResponseEntity<ApiResponse<Page<OfferResponse>>> getOffers(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) OfferStatus status,
            @RequestParam(required = false) Long companyId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<OfferResponse> response = offerService.getOffers(currentUser, status, companyId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Teklif Detayı", description = "ID'ye göre teklif detayını döner")
    public ResponseEntity<ApiResponse<OfferResponse>> getOfferById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        OfferResponse response = offerService.getOfferById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Yeni Teklif Oluştur", description = "Firmaya ticari teklif/fırsat açar")
    public ResponseEntity<ApiResponse<OfferResponse>> createOffer(
            @Valid @RequestBody OfferRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        OfferResponse response = offerService.createOffer(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Teklif başarıyla oluşturuldu", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Teklif Durumu Güncelle", description = "Teklif durumunu 'KAZANILDI', 'KAYBEDILDI' veya 'IPTAL' olarak günceller")
    public ResponseEntity<ApiResponse<OfferResponse>> updateOfferStatus(
            @PathVariable Long id,
            @RequestParam OfferStatus status,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        OfferResponse response = offerService.updateOfferStatus(id, status, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Teklif durumu başarıyla güncellendi", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Teklif Sil", description = "Teklif kaydını siler")
    public ResponseEntity<ApiResponse<Void>> deleteOffer(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        offerService.deleteOffer(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Teklif başarıyla silindi", null));
    }
}
