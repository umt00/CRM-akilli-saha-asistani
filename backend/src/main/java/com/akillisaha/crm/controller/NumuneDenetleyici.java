package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.NumuneIstegi;
import com.akillisaha.crm.dto.response.IslemSonucu;
import com.akillisaha.crm.dto.response.NumuneYaniti;
import com.akillisaha.crm.enums.NumuneDurumu;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.NumuneServisi;
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

/**
 * Ürün numunelerinin yaşam döngüsü, durum güncellemeleri ve marka bazlı rapor uç noktalarını sunan REST denetleyicisi.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@RestController
@RequestMapping("/api/v1/numuneler")
@RequiredArgsConstructor
@Tag(name = "Numune Takip", description = "Sahaya bırakılan numuneler, tadım sonuçları ve siparişe dönüşüm takibi")
public class NumuneDenetleyici {

    private final NumuneServisi numuneServisi;

    /**
     * Temsilciye göre izole numune listesini döner.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @param durum Numune durumu filtresi (opsiyonel)
     * @param firmaId Firma filtresi (opsiyonel)
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalı numuneler
     */
    @GetMapping
    @Operation(summary = "Numune Listesi", description = "Durum ve firma bazlı filtrelenebilir, izole numune listesi")
    public ResponseEntity<IslemSonucu<Page<NumuneYaniti>>> numuneleriListele(
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici,
            @RequestParam(required = false) NumuneDurumu durum,
            @RequestParam(required = false) Long firmaId,
            @PageableDefault(size = 20, sort = "gonderimTarihi", direction = Sort.Direction.DESC) Pageable sayfalama) {
        Page<NumuneYaniti> yanit = numuneServisi.numuneleriGetir(aktifKullanici, durum, firmaId, sayfalama);
        return ResponseEntity.ok(IslemSonucu.basarili(yanit));
    }

    /**
     * ID'ye göre numune detayını getirir.
     * 
     * @param id Numune kimliği
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Numune detay yanıtı
     */
    @GetMapping("/{id}")
    @Operation(summary = "Numune Detayı", description = "Belirtilen ID'ye sahip numunenin detay bilgilerini döner")
    public ResponseEntity<IslemSonucu<NumuneYaniti>> numuneDetayiGetir(
            @PathVariable Long id,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        NumuneYaniti yanit = numuneServisi.numuneDetayiGetir(id, aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili(yanit));
    }

    /**
     * Ziyaretten bağımsız doğrudan numune kaydı açar.
     * 
     * @param istek Numune formu
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Kaydedilen numune
     */
    @PostMapping
    @Operation(summary = "Yeni Numune Ekle", description = "Doğrudan yeni bir numune takip kartı açar")
    public ResponseEntity<IslemSonucu<NumuneYaniti>> numuneOlustur(
            @Valid @RequestBody NumuneIstegi istek,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        NumuneYaniti yanit = numuneServisi.numuneOlustur(istek, aktifKullanici);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(IslemSonucu.basarili("Numune başarıyla kaydedildi", yanit));
    }

    /**
     * Numunenin durumunu ('BEGENDI', 'REDDETTI' vb.) ve sonuç notunu günceller.
     * 
     * @param id Numune kimliği
     * @param durum Yeni numune durumu
     * @param sonucNotlari Müşteri geri bildirim notu (opsiyonel)
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Güncellenen numune
     */
    @PatchMapping("/{id}/durum")
    @Operation(summary = "Numune Durumu Güncelle", description = "Numuneyi 'BEGENDI', 'REDDETTI' veya 'SIPARISE_DONUSTU' olarak günceller")
    public ResponseEntity<IslemSonucu<NumuneYaniti>> numuneDurumuGuncelle(
            @PathVariable Long id,
            @RequestParam NumuneDurumu durum,
            @RequestParam(required = false) String sonucNotlari,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        NumuneYaniti yanit = numuneServisi.numuneDurumuGuncelle(id, durum, sonucNotlari, aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili("Numune durumu başarıyla güncellendi", yanit));
    }

    /**
     * Numune kaydını siler.
     * 
     * @param id Numune kimliği
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Boş başarılı yanıt
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Numune Sil", description = "Numune kaydını siler")
    public ResponseEntity<IslemSonucu<Void>> numuneSil(
            @PathVariable Long id,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        numuneServisi.numuneSil(id, aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili("Numune başarıyla silindi", null));
    }

    /**
     * Markalara (Kerry, Cargill vb.) ve durumlara göre numune sayıları dağılımını döner.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @return İstatistik haritası
     */
    @GetMapping("/istatistikler")
    @Operation(summary = "Numune İstatistikleri", description = "Durum ve marka bazlı numune dağılım sayılarını döner")
    public ResponseEntity<IslemSonucu<Map<String, Long>>> istatistikleriGetir(
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        Map<String, Long> istatistikler = numuneServisi.numuneIstatistikleriniGetir(aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili("İstatistikler başarıyla getirildi", istatistikler));
    }
}
