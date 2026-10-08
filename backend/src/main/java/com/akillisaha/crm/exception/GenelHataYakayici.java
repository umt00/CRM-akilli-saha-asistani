package com.akillisaha.crm.exception;

import com.akillisaha.crm.dto.response.IslemSonucu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Uygulama genelinde fırlatılan tüm istisnaları merkezi olarak yakalayan
 * ve standart 'IslemSonucu' JSON formatında istemciye dönen hata denetleyicisi.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Slf4j
@RestControllerAdvice
public class GenelHataYakayici {

    /**
     * Jakarta Validation (@NotNull, @NotBlank, @Size vb.) doğrulama hatalarını yakalar
     * ve hatalı alanları Türkçe hata mesajlarıyla döner.
     * 
     * @param istisna Doğrulama istisnası
     * @return HTTP 400 Bad Request ve alan bazlı hata haritası
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<IslemSonucu<Map<String, String>>> dogrulamaHatasiYonet(MethodArgumentNotValidException istisna) {
        Map<String, String> hatalar = new HashMap<>();
        for (FieldError alanHatasi : istisna.getBindingResult().getFieldErrors()) {
            hatalar.put(alanHatasi.getField(), alanHatasi.getDefaultMessage());
        }
        log.warn("Alan doğrulama hatası oluştu: {}", hatalar);
        IslemSonucu<Map<String, String>> yanit = IslemSonucu.<Map<String, String>>builder()
                .basarili(false)
                .mesaj("Girilen form alanları doğrulanamadı")
                .veri(hatalar)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(yanit);
    }

    /**
     * Veritabanında aranan kaydın bulunamadığı durumları yönetir.
     * 
     * @param istisna Kaynak bulunamadı hatası
     * @return HTTP 404 Not Found
     */
    @ExceptionHandler(KaynakBulunamadiHatasi.class)
    public ResponseEntity<IslemSonucu<Void>> kaynakBulunamadiYonet(KaynakBulunamadiHatasi istisna) {
        log.warn("Kayıt bulunamadı: {}", istisna.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(IslemSonucu.hata(istisna.getMessage()));
    }

    /**
     * Geçersiz parametre veya iş kuralı ihlali isteklerini yönetir.
     * 
     * @param istisna Geçersiz istek hatası
     * @return HTTP 400 Bad Request
     */
    @ExceptionHandler(GecersizIstekHatasi.class)
    public ResponseEntity<IslemSonucu<Void>> gecersizIstekYonet(GecersizIstekHatasi istisna) {
        log.warn("Geçersiz istek: {}", istisna.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(IslemSonucu.hata(istisna.getMessage()));
    }

    /**
     * Yetkisiz işlem ve veri izolasyonu ihlali girişimlerini yönetir.
     * 
     * @param istisna Erişim engellendi hatası
     * @return HTTP 403 Forbidden
     */
    @ExceptionHandler({AccessDeniedException.class, ErisimEngellendiHatasi.class})
    public ResponseEntity<IslemSonucu<Void>> erisimEngellendiYonet(Exception istisna) {
        log.warn("Erişim engellendi: {}", istisna.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(IslemSonucu.hata("Bu işlem için yetkiniz bulunmamaktadır"));
    }

    /**
     * Hatalı e-posta veya parola ile giriş denemelerini yönetir.
     * 
     * @param istisna Hatalı kimlik bilgisi
     * @return HTTP 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<IslemSonucu<Void>> hataliKimlikBilgisiYonet(BadCredentialsException istisna) {
        log.warn("Hatalı kimlik bilgisi girişi: {}", istisna.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(IslemSonucu.hata("E-posta adresi veya şifre hatalı"));
    }

    /**
     * Öngörülemeyen tüm genel sunucu içi hataları yakalar.
     * 
     * @param istisna Beklenmeyen sistem hatası
     * @return HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<IslemSonucu<Void>> genelSunucuHatasiYonet(Exception istisna) {
        log.error("Sunucu içi beklenmeyen hata meydana geldi: ", istisna);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(IslemSonucu.hata("Sunucu içi beklenmeyen bir hata meydana geldi: " + istisna.getMessage()));
    }
}
