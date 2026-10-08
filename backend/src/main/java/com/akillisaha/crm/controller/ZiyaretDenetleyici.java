package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.ZiyaretIstegi;
import com.akillisaha.crm.dto.response.IslemSonucu;
import com.akillisaha.crm.dto.response.ZiyaretYaniti;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.ZiyaretServisi;
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

/**
 * Saha ziyaretleri, 17 kolonluk form ve ajanda/randevu yönetim uç noktalarını sunan REST denetleyicisi.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@RestController
@RequestMapping("/api/v1/ziyaretler")
@RequiredArgsConstructor
@Tag(name = "Saha Ziyaretleri", description = "Saha plasiyer ziyaretleri, otomatik numune/teklif tetikleme ve ajanda")
public class ZiyaretDenetleyici {

    private final ZiyaretServisi ziyaretServisi;

    /**
     * Temsilciye göre sayfalanmış ziyaret listesini getirir.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @param firmaId Belirli bir firmanın ziyaretleri (opsiyonel)
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalı ziyaret listesi
     */
    @GetMapping
    @Operation(summary = "Ziyaret Listesi", description = "Plasiyere göre filtrelenmiş sayfalanabilir ziyaret listesi")
    public ResponseEntity<IslemSonucu<Page<ZiyaretYaniti>>> ziyaretleriListele(
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici,
            @RequestParam(required = false) Long firmaId,
            @PageableDefault(size = 20, sort = "ziyaretTarihi", direction = Sort.Direction.DESC) Pageable sayfalama) {
        Page<ZiyaretYaniti> yanit = ziyaretServisi.ziyaretleriGetir(aktifKullanici, firmaId, sayfalama);
        return ResponseEntity.ok(IslemSonucu.basarili(yanit));
    }

    /**
     * ID'ye göre ziyaret detayını döner.
     * 
     * @param id Ziyaret kimliği
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Ziyaret detay yanıtı
     */
    @GetMapping("/{id}")
    @Operation(summary = "Ziyaret Detayı", description = "Belirtilen ziyaretin numuneleri ve teklifleriyle tam dökümü")
    public ResponseEntity<IslemSonucu<ZiyaretYaniti>> ziyaretDetayiGetir(
            @PathVariable Long id,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        ZiyaretYaniti yanit = ziyaretServisi.ziyaretDetayiGetir(id, aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili(yanit));
    }

    /**
     * Plasiyerin 30 saniyede dolduracağı saha ziyaretini kaydeder.
     * Numune işaretlendiyse otomatik Numune Takip nesnesi açar.
     * 
     * @param istek Ziyaret formu
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Kaydedilen ziyaret
     */
    @PostMapping
    @Operation(summary = "Yeni Ziyaret Kaydet", description = "Saha ziyareti oluşturur; numune veya teklif varsa tek işlemde (@Transactional) otomatik bağlar")
    public ResponseEntity<IslemSonucu<ZiyaretYaniti>> ziyaretOlustur(
            @Valid @RequestBody ZiyaretIstegi istek,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        ZiyaretYaniti yanit = ziyaretServisi.ziyaretOlustur(istek, aktifKullanici);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(IslemSonucu.basarili("Ziyaret başarıyla kaydedildi", yanit));
    }

    /**
     * Ziyaret kaydını günceller.
     * 
     * @param id Ziyaret kimliği
     * @param istek Güncelleme formu
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Güncellenen ziyaret
     */
    @PutMapping("/{id}")
    @Operation(summary = "Ziyaret Güncelle", description = "Ziyaret notlarını veya tarihini günceller")
    public ResponseEntity<IslemSonucu<ZiyaretYaniti>> ziyaretGuncelle(
            @PathVariable Long id,
            @Valid @RequestBody ZiyaretIstegi istek,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        ZiyaretYaniti yanit = ziyaretServisi.ziyaretGuncelle(id, istek, aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili("Ziyaret başarıyla güncellendi", yanit));
    }

    /**
     * Ziyaret kaydını siler.
     * 
     * @param id Ziyaret kimliği
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Boş başarılı yanıt
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Ziyaret Sil", description = "Ziyaret kaydını siler")
    public ResponseEntity<IslemSonucu<Void>> ziyaretSil(
            @PathVariable Long id,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        ziyaretServisi.ziyaretSil(id, aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili("Ziyaret başarıyla silindi", null));
    }

    /**
     * Ajanda / Yaklaşan Ziyaretler: Vadesi yaklaşan veya geciken randevuları listeler.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @param sonTarih Karşılaştırma tarihi (opsiyonel)
     * @return Randevu listesi
     */
    @GetMapping("/ajanda")
    @Operation(summary = "Ajanda / Yaklaşan Randevular", description = "Sonraki randevu tarihi yaklaşan ve geciken ziyaretlerin listesi")
    public ResponseEntity<IslemSonucu<List<ZiyaretYaniti>>> vadesiGelenleriGetir(
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime sonTarih) {
        List<ZiyaretYaniti> yanit = ziyaretServisi.vadesiGelenAjandaZiyaretleriniGetir(aktifKullanici, sonTarih);
        return ResponseEntity.ok(IslemSonucu.basarili("Ajanda listesi başarıyla getirildi", yanit));
    }
}
