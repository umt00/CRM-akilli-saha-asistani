package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.response.IslemSonucu;
import com.akillisaha.crm.dto.response.TakvimEtkinlikYaniti;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.TakvimServisi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Bitrix24 tarzı interaktif takvim, randevular ve müşteri zaman çizelgesi
 * için REST uç noktalarını sunan denetleyici.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@RestController
@RequestMapping("/api/v1/takvim")
@RequiredArgsConstructor
@Tag(name = "İnteraktif Takvim", description = "Bitrix24 tarzı birleşik ajanda, randevu, numune ve teklif takvimi")
public class TakvimDenetleyici {

    private final TakvimServisi takvimServisi;

    /**
     * Belirli tarih aralığındaki tüm saha etkinliklerini (ziyaret, ajanda, numune, teklif) döner.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @param baslangic Başlangıç zaman damgası (opsiyonel)
     * @param bitis Bitiş zaman damgası (opsiyonel)
     * @param tur Filtrelenecek etkinlik türü (RANDEVU, ZIYARET, NUMUNE, TEKLIF)
     * @return Takvim etkinlik listesi
     */
    @GetMapping("/etkinlikler")
    @Operation(summary = "Takvim Etkinlikleri", description = "Belirtilen tarih aralığında saha ekibinin randevularını, numune takiplerini ve teklif vadelerini döner")
    public ResponseEntity<IslemSonucu<List<TakvimEtkinlikYaniti>>> etkinlikleriGetir(
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime baslangic,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime bitis,
            @RequestParam(required = false) String tur) {

        List<TakvimEtkinlikYaniti> liste = takvimServisi.takvimEtkinlikleriniGetir(aktifKullanici, baslangic, bitis, tur);
        return ResponseEntity.ok(IslemSonucu.basarili("Takvim etkinlikleri başarıyla listelendi", liste));
    }
}
