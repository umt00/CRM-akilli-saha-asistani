package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.TeklifIstegi;
import com.akillisaha.crm.dto.response.IslemSonucu;
import com.akillisaha.crm.dto.response.TeklifYaniti;
import com.akillisaha.crm.enums.TeklifDurumu;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.TeklifServisi;
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

/**
 * Ticari teklifler, fiyat fırsatları ve kazanım durumları yönetim uç noktalarını sunan REST denetleyicisi.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@RestController
@RequestMapping("/api/v1/teklifler")
@RequiredArgsConstructor
@Tag(name = "Teklifler & Fırsatlar", description = "Saha fiyat teklifleri, kazanım oranları ve tutar takibi")
public class TeklifDenetleyici {

    private final TeklifServisi teklifServisi;

    /**
     * Temsilciye göre sayfalanmış teklif listesini döner.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @param durum Teklif durumu filtresi (opsiyonel)
     * @param firmaId Firma filtresi (opsiyonel)
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalı teklifler
     */
    @GetMapping
    @Operation(summary = "Teklif Listesi", description = "Plasiyere göre filtrelenmiş sayfalanabilir teklif listesi")
    public ResponseEntity<IslemSonucu<Page<TeklifYaniti>>> teklifleriListele(
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici,
            @RequestParam(required = false) TeklifDurumu durum,
            @RequestParam(required = false) Long firmaId,
            @PageableDefault(size = 20, sort = "olusturulmaTarihi", direction = Sort.Direction.DESC) Pageable sayfalama) {
        Page<TeklifYaniti> yanit = teklifServisi.teklifleriGetir(aktifKullanici, durum, firmaId, sayfalama);
        return ResponseEntity.ok(IslemSonucu.basarili(yanit));
    }

    /**
     * ID'ye göre teklif detayını döner.
     * 
     * @param id Teklif kimliği
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Teklif detay yanıtı
     */
    @GetMapping("/{id}")
    @Operation(summary = "Teklif Detayı", description = "Belirtilen ID'ye sahip teklifin tutar ve koşullarını döner")
    public ResponseEntity<IslemSonucu<TeklifYaniti>> teklifDetayiGetir(
            @PathVariable Long id,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        TeklifYaniti yanit = teklifServisi.teklifDetayiGetir(id, aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili(yanit));
    }

    /**
     * Firmaya yeni bir ticari teklif veya fırsat açar.
     * 
     * @param istek Teklif formu
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Açılan teklif
     */
    @PostMapping
    @Operation(summary = "Yeni Teklif Aç", description = "Müşteriye yeni fiyat teklifi / fırsat kaydı oluşturur")
    public ResponseEntity<IslemSonucu<TeklifYaniti>> teklifOlustur(
            @Valid @RequestBody TeklifIstegi istek,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        TeklifYaniti yanit = teklifServisi.teklifOlustur(istek, aktifKullanici);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(IslemSonucu.basarili("Teklif başarıyla oluşturuldu", yanit));
    }

    /**
     * Teklif durumunu ('KAZANILDI', 'KAYBEDILDI' veya 'IPTAL') günceller.
     * 
     * @param id Teklif kimliği
     * @param durum Yeni teklif durumu
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Güncellenen teklif
     */
    @PatchMapping("/{id}/durum")
    @Operation(summary = "Teklif Durumu Güncelle", description = "Teklifi 'KAZANILDI', 'KAYBEDILDI' veya 'IPTAL' durumuna çeker")
    public ResponseEntity<IslemSonucu<TeklifYaniti>> teklifDurumuGuncelle(
            @PathVariable Long id,
            @RequestParam TeklifDurumu durum,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        TeklifYaniti yanit = teklifServisi.teklifDurumuGuncelle(id, durum, aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili("Teklif durumu başarıyla güncellendi", yanit));
    }

    /**
     * Teklif kaydını siler.
     * 
     * @param id Teklif kimliği
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Boş başarılı yanıt
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Teklif Sil", description = "Teklif kaydını siler")
    public ResponseEntity<IslemSonucu<Void>> teklifSil(
            @PathVariable Long id,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        teklifServisi.teklifSil(id, aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili("Teklif başarıyla silindi", null));
    }
}
