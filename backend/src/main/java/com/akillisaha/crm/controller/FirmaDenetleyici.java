package com.akillisaha.crm.controller;

import com.akillisaha.crm.dto.request.FirmaIstegi;
import com.akillisaha.crm.dto.request.YetkiliIstegi;
import com.akillisaha.crm.dto.response.FirmaYaniti;
import com.akillisaha.crm.dto.response.IslemSonucu;
import com.akillisaha.crm.dto.response.YetkiliYaniti;
import com.akillisaha.crm.security.OzelKullaniciDetaylari;
import com.akillisaha.crm.service.FirmaServisi;
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
 * Müşteri firmalar ve firma yetkililerinin yönetim uç noktalarını sunan REST denetleyicisi.
 * Temsilci bazlı veri izolasyonunu tam olarak uygular.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@RestController
@RequestMapping("/api/v1/firmalar")
@RequiredArgsConstructor
@Tag(name = "Müşteri Firmalar", description = "Müşteri portföyü, rakip bilgileri ve yetkili kişi yönetimi")
public class FirmaDenetleyici {

    private final FirmaServisi firmaServisi;

    /**
     * Temsilciye göre izole edilmiş sayfalı firma listesini döner.
     * 
     * @param aktifKullanici Oturum açan kullanıcı
     * @param arama İsteğe bağlı unvan veya bölge arama kelimesi
     * @param sayfalama Sayfalama nesnesi
     * @return Sayfalanmış firma listesi
     */
    @GetMapping
    @Operation(summary = "Firma Listesi", description = "Plasiyere göre filtrelenmiş sayfalanabilir firma listesi")
    public ResponseEntity<IslemSonucu<Page<FirmaYaniti>>> firmalariListele(
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici,
            @RequestParam(required = false) String arama,
            @PageableDefault(size = 20, sort = "olusturulmaTarihi", direction = Sort.Direction.DESC) Pageable sayfalama) {
        Page<FirmaYaniti> yanit = firmaServisi.firmalariGetir(aktifKullanici, arama, sayfalama);
        return ResponseEntity.ok(IslemSonucu.basarili(yanit));
    }

    /**
     * ID'ye göre firma detayını ve yetkililerini döner.
     * 
     * @param id Firma kimliği
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Firma detay yanıtı
     */
    @GetMapping("/{id}")
    @Operation(summary = "Firma Detayı", description = "Belirtilen ID'ye sahip firmanın 360 derece kartını döner")
    public ResponseEntity<IslemSonucu<FirmaYaniti>> firmaDetayiGetir(
            @PathVariable Long id,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        FirmaYaniti yanit = firmaServisi.firmaDetayiGetir(id, aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili(yanit));
    }

    /**
     * Sisteme yeni bir müşteri firma kaydeder.
     * 
     * @param istek Firma kayıt formu
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Oluşturulan firma
     */
    @PostMapping
    @Operation(summary = "Yeni Firma Ekle", description = "Portföye yeni müşteri firma tanımlar")
    public ResponseEntity<IslemSonucu<FirmaYaniti>> firmaOlustur(
            @Valid @RequestBody FirmaIstegi istek,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        FirmaYaniti yanit = firmaServisi.firmaOlustur(istek, aktifKullanici);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(IslemSonucu.basarili("Firma başarıyla oluşturuldu", yanit));
    }

    /**
     * Mevcut firmanın bilgilerini ve rakip tedarikçi durumunu günceller.
     * 
     * @param id Firma kimliği
     * @param istek Güncelleme formu
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Güncellenen firma
     */
    @PutMapping("/{id}")
    @Operation(summary = "Firma Güncelle", description = "Firma pazar ve adres bilgilerini günceller")
    public ResponseEntity<IslemSonucu<FirmaYaniti>> firmaGuncelle(
            @PathVariable Long id,
            @Valid @RequestBody FirmaIstegi istek,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        FirmaYaniti yanit = firmaServisi.firmaGuncelle(id, istek, aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili("Firma başarıyla güncellendi", yanit));
    }

    /**
     * Firmayı ve bağlı tüm ilişkili kayıtlarını siler.
     * 
     * @param id Firma kimliği
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Boş başarılı yanıt
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Firma Sil", description = "Firmayı ve tüm geçmiş kayıtlarını siler")
    public ResponseEntity<IslemSonucu<Void>> firmaSil(
            @PathVariable Long id,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        firmaServisi.firmaSil(id, aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili("Firma başarıyla silindi", null));
    }

    /**
     * Firmaya yeni bir yetkili kişi (aşçı, satın almacı) tanımlar.
     * 
     * @param firmaId Firma kimliği
     * @param istek Yetkili formu
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Eklenen yetkili
     */
    @PostMapping("/{firmaId}/yetkililer")
    @Operation(summary = "Yetkili Kişi Ekle", description = "Firmaya satın alma veya mutfak şefi gibi yetkili ekler")
    public ResponseEntity<IslemSonucu<YetkiliYaniti>> yetkiliEkle(
            @PathVariable Long firmaId,
            @Valid @RequestBody YetkiliIstegi istek,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        YetkiliYaniti yanit = firmaServisi.yetkiliEkle(firmaId, istek, aktifKullanici);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(IslemSonucu.basarili("Yetkili başarıyla eklendi", yanit));
    }

    /**
     * Firmaya bağlı bir yetkili kişiyi siler.
     * 
     * @param firmaId Firma kimliği
     * @param yetkiliId Yetkili kimliği
     * @param aktifKullanici Oturum açan kullanıcı
     * @return Boş başarılı yanıt
     */
    @DeleteMapping("/{firmaId}/yetkililer/{yetkiliId}")
    @Operation(summary = "Yetkili Kişi Sil", description = "Firmadan bir yetkili kişiyi siler")
    public ResponseEntity<IslemSonucu<Void>> yetkiliSil(
            @PathVariable Long firmaId,
            @PathVariable Long yetkiliId,
            @AuthenticationPrincipal OzelKullaniciDetaylari aktifKullanici) {
        firmaServisi.yetkiliSil(firmaId, yetkiliId, aktifKullanici);
        return ResponseEntity.ok(IslemSonucu.basarili("Yetkili başarıyla silindi", null));
    }
}
