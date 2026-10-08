package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.response.ExcelIceAktarimSonucuYaniti;
import com.akillisaha.crm.dto.response.IslemSonucu;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.ExcelServisi;
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

/**
 * 17 kolonluk saha şablonunda Excel indirme (Export) ve yükleme (Import)
 * uç noktalarını sunan REST denetleyicisi.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@RestController
@RequestMapping("/api/v1/excel")
@RequiredArgsConstructor
@Tag(name = "Excel Motoru", description = "17 Kolonluk Excel Raporu İndirme ve Sisteme Toplu Yükleme")
public class ExcelDenetleyici {

    private final ExcelServisi excelServisi;

    /**
     * Saha ziyaretlerini, firmaları ve numuneleri 17 kolonluk kurumsal Excel şablonu olarak indirir.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @param plasiyerId Plasiyer filtre ID'si (opsiyonel)
     * @param baslangicTarihi Başlangıç zaman damgası (opsiyonel)
     * @param bitisTarihi Bitiş zaman damgası (opsiyonel)
     * @return .xlsx dosyası akışı
     */
    @GetMapping("/disa-aktar")
    @Operation(summary = "Excel İndir (Export)", description = "17 kolonluk orijinal saha şablonunda müşteri ziyaretleri Excel dosyasını üretir ve indirir")
    public ResponseEntity<byte[]> ziyaretleriExceleAktar(
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici,
            @RequestParam(required = false) Long plasiyerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime baslangicTarihi,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime bitisTarihi) {

        byte[] excelBaytlari = excelServisi.ziyaretleriExceleAktar(aktifKullanici, plasiyerId, baslangicTarihi, bitisTarihi);
        String dosyaAdi = "Saha_Ziyaretleri_" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dosyaAdi + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBaytlari);
    }

    /**
     * Sahadan toplanan 17 kolonluk Excel dosyasını sisteme yükleyerek firma, yetkili ve ziyaretleri otomatik işler.
     * 
     * @param dosya Yüklenen .xlsx dosyası (multipart/form-data)
     * @param aktifKullanici Oturum açan kullanıcı
     * @return İçe aktarım sonuç istatistikleri
     */
    @PostMapping(value = "/ice-aktar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Excel Yükle (Import)", description = "17 kolonluk Excel dosyasını sisteme aktarır; firma yoksa otomatik açar, yetkili ve ziyaretleri kaydeder")
    public ResponseEntity<IslemSonucu<ExcelIceAktarimSonucuYaniti>> exceldenZiyaretleriIceAktar(
            @RequestParam("dosya") MultipartFile dosya,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {

        ExcelIceAktarimSonucuYaniti yanit = excelServisi.exceldenZiyaretleriIceAktar(dosya, aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili("Excel dosyası başarıyla işlendi", yanit));
    }
}
