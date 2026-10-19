package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.SahaNotuIstegi;
import com.akillisaha.crm.dto.response.IslemSonucu;
import com.akillisaha.crm.dto.response.SahaNotuYaniti;
import com.akillisaha.crm.service.SahaNotuServisi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * n8n ve WhatsApp otomasyonlarından gelen saha notu webhook isteklerini karşılayan denetleyici.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
@Tag(name = "Saha Notu Webhook", description = "WhatsApp ve n8n üzerinden otomatik saha notu işleme uç noktaları")
public class SahaNotuWebhookDenetleyici {

    private final SahaNotuServisi sahaNotuServisi;

    @Value("${webhook.secret:akillisaha-crm-secret-2026}")
    private String beklenenSecret;

    /**
     * n8n veya WhatsApp botundan gelen ham saha notunu çözer ve veritabanına işler.
     * 
     * @param webhookSecret Güvenlik doğrulaması için gizli başlık tokenı (Opsiyonel)
     * @param istek Ham metin ve plasiyer bilgilerini içeren istek gövdesi
     * @return İşlem sonucu ve plasiyere iletilecek WhatsApp teyit metni
     */
    @PostMapping("/saha-notu")
    @Operation(
        summary = "WhatsApp Saha Notu Yakala ve İşle",
        description = "WhatsApp üzerinden '!' veya '#crm' ile biten notları n8n aracılığıyla karşılar, ziyaret, numune ve teklif kayıtlarını otomatik oluşturur."
    )
    public ResponseEntity<IslemSonucu<SahaNotuYaniti>> sahaNotuIsle(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String webhookSecret,
            @Valid @RequestBody SahaNotuIstegi istek
    ) {
        // Gizli anahtar doğrulaması (güvenlik katmanı)
        if (webhookSecret != null && !webhookSecret.isEmpty() && !beklenenSecret.equals(webhookSecret)) {
            log.warn("Geçersiz Webhook Secret tespit edildi: {}", webhookSecret);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(IslemSonucu.hata("Geçersiz Webhook Secret"));
        }

        log.info("Yeni WhatsApp saha notu webhook isteği alındı. Gönderen Tel: {}, Plasiyer ID: {}", 
                istek.getGonderenTel(), istek.getPlasiyerId());

        SahaNotuYaniti yanit = sahaNotuServisi.sahaNotunuIsle(istek);
        if (!yanit.isBasarili()) {
            return ResponseEntity.badRequest().body(IslemSonucu.hata(yanit.getTeyitMesaji()));
        }

        return ResponseEntity.ok(IslemSonucu.basarili("Saha notu başarıyla işlendi", yanit));
    }
}
