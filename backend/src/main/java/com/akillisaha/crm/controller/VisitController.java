package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.VisitRequest;
import com.akillisaha.crm.dto.response.ApiResponse;
import com.akillisaha.crm.dto.response.VisitResponse;
import com.akillisaha.crm.security.CustomUserDetails;
import com.akillisaha.crm.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/visits")
@RequiredArgsConstructor
@Tag(name = "Saha Ziyaretleri (Visits)", description = "Saha temsilcisi ziyaretleri, 17 kolonluk form ve ajanda")
public class VisitController {

    private final VisitService visitService;

    @GetMapping
    @Operation(summary = "Ziyaret Listesi", description = "Temsilciye göre izole edilmiş, sayfalanabilir ziyaret listesi")
    public ResponseEntity<ApiResponse<Page<VisitResponse>>> getVisits(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) Long companyId,
            @PageableDefault(size = 20, sort = "visitDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<VisitResponse> response = visitService.getVisits(currentUser, companyId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ziyaret Detayı", description = "ID'ye göre ziyaret detayını döner")
    public ResponseEntity<ApiResponse<VisitResponse>> getVisitById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        VisitResponse response = visitService.getVisitById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Yeni Ziyaret Kaydet", description = "30 saniyelik saha ziyareti kaydı; numune işaretlendiyse otomatik numune takip kaydı açar")
    public ResponseEntity<ApiResponse<VisitResponse>> createVisit(
            @Valid @RequestBody VisitRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        VisitResponse response = visitService.createVisit(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ziyaret başarıyla kaydedildi", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Ziyaret Güncelle", description = "Ziyaret kaydını günceller")
    public ResponseEntity<ApiResponse<VisitResponse>> updateVisit(
            @PathVariable Long id,
            @Valid @RequestBody VisitRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        VisitResponse response = visitService.updateVisit(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Ziyaret başarıyla güncellendi", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ziyaret Sil", description = "Ziyaret kaydını siler")
    public ResponseEntity<ApiResponse<Void>> deleteVisit(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        visitService.deleteVisit(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Ziyaret başarıyla silindi", null));
    }

    @GetMapping("/agenda")
    @Operation(summary = "Ajanda / Yaklaşan Ziyaretler", description = "Sonraki randevu tarihi yaklaşan ve geciken ziyaretlerin listesi")
    public ResponseEntity<ApiResponse<List<VisitResponse>>> getDueAgendaVisits(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime beforeDate) {
        List<VisitResponse> response = visitService.getDueAgendaVisits(currentUser, beforeDate);
        return ResponseEntity.ok(ApiResponse.success("Ajanda listesi başarıyla getirildi", response));
    }
}
