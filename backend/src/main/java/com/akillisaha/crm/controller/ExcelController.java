package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.response.ApiResponse;
import com.akillisaha.crm.dto.response.ExcelImportResultResponse;
import com.akillisaha.crm.security.CustomUserDetails;
import com.akillisaha.crm.service.ExcelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/excel")
@RequiredArgsConstructor
@Tag(name = "Excel Motoru (Apache POI)", description = "17 Kolonluk Excel İçe/Dışa Aktarma (Import/Export)")
public class ExcelController {

    private final ExcelService excelService;

    @GetMapping("/export")
    @Operation(summary = "Excel İndir (Export)", description = "17 kolonluk orijinal formatta müşteri ziyaretleri Excel raporu üretir")
    public ResponseEntity<byte[]> exportVisits(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) Long representativeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {

        byte[] excelBytes = excelService.exportVisitsToExcel(currentUser, representativeId, startDate, endDate);
        String filename = "Saha_Ziyaretleri_" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Excel Yükle (Import)", description = "17 kolonluk saha Excel dosyasını sisteme yükleyerek firma, yetkili ve ziyaretleri otomatik işler")
    public ResponseEntity<ApiResponse<ExcelImportResultResponse>> importVisits(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ExcelImportResultResponse response = excelService.importVisitsFromExcel(file, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Excel başarıyla işlendi", response));
    }
}
