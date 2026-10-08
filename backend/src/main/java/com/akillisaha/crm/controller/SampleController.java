package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.SampleRequest;
import com.akillisaha.crm.dto.response.ApiResponse;
import com.akillisaha.crm.dto.response.SampleResponse;
import com.akillisaha.crm.enums.SampleStatus;
import com.akillisaha.crm.security.CustomUserDetails;
import com.akillisaha.crm.service.SampleService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/samples")
@RequiredArgsConstructor
@Tag(name = "Numune Takip (Samples)", description = "Numune gönderimleri, durum güncellemeleri ve sonuç takibi")
public class SampleController {

    private final SampleService sampleService;

    @GetMapping
    @Operation(summary = "Numune Listesi", description = "Durum ve firma filtreli, izole numune listesi")
    public ResponseEntity<ApiResponse<Page<SampleResponse>>> getSamples(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) SampleStatus status,
            @RequestParam(required = false) Long companyId,
            @PageableDefault(size = 20, sort = "sentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SampleResponse> response = sampleService.getSamples(currentUser, status, companyId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Numune Detayı", description = "ID'ye göre numune detayını döner")
    public ResponseEntity<ApiResponse<SampleResponse>> getSampleById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        SampleResponse response = sampleService.getSampleById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Manuel Numune Ekle", description = "Ziyaretten bağımsız veya doğrudan numune kaydı açar")
    public ResponseEntity<ApiResponse<SampleResponse>> createSample(
            @Valid @RequestBody SampleRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        SampleResponse response = sampleService.createSample(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Numune başarıyla kaydedildi", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Numune Durumu Güncelle", description = "Numuneyi 'BEGENDI', 'REDDETTI' veya 'SIPARISE_DONUSTU' olarak günceller")
    public ResponseEntity<ApiResponse<SampleResponse>> updateSampleStatus(
            @PathVariable Long id,
            @RequestParam SampleStatus status,
            @RequestParam(required = false) String resultNotes,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        SampleResponse response = sampleService.updateSampleStatus(id, status, resultNotes, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Numune durumu başarıyla güncellendi", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Numune Sil", description = "Numune kaydını siler")
    public ResponseEntity<ApiResponse<Void>> deleteSample(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        sampleService.deleteSample(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Numune başarıyla silindi", null));
    }

    @GetMapping("/stats")
    @Operation(summary = "Numune İstatistikleri", description = "Durumlara ve markalara (Kerry, Cargill) göre numune sayıları")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getSampleStats(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Map<String, Long> stats = sampleService.getSampleStats(currentUser);
        return ResponseEntity.ok(ApiResponse.success("İstatistikler getirildi", stats));
    }
}
