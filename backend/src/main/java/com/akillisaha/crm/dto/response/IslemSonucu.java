package com.akillisaha.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Tüm REST API yanıtlarını sarmalayan kurumsal standart JSON zarfı DTO sınıfı.
 * İstemcinin tutarlı bir veri yapısı almasını sağlar.
 * 
 * @param <T> Dönen veri gövdesinin generic tipi
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IslemSonucu<T> {

    /**
     * İşlemin başarıyla tamamlanıp tamamlanmadığı bayrağı.
     */
    private boolean basarili;

    /**
     * Kullanıcıya veya arayüze gösterilecek anlaşılır bilgi/hata mesajı.
     */
    private String mesaj;

    /**
     * Yanıtın taşıdığı asıl veri nesnesi (liste, tekil nesne veya null).
     */
    private T veri;

    /**
     * Yanıtın üretildiği ISO-8601 formatındaki zaman damgası.
     */
    @Builder.Default
    private OffsetDateTime zamanDamgasi = OffsetDateTime.now();

    /**
     * Başarılı bir işlem sonucu üretir.
     * 
     * @param veri Dönecek veri nesnesi
     * @param <T> Veri tipi
     * @return Başarılı IslemSonucu sarmalayıcısı
     */
    public static <T> IslemSonucu<T> basarili(T veri) {
        return IslemSonucu.<T>builder()
                .basarili(true)
                .mesaj("İşlem başarıyla tamamlandı")
                .veri(veri)
                .zamanDamgasi(OffsetDateTime.now())
                .build();
    }

    /**
     * Özel mesaj içeren başarılı bir işlem sonucu üretir.
     * 
     * @param mesaj Özel bilgilendirme mesajı
     * @param veri Dönecek veri nesnesi
     * @param <T> Veri tipi
     * @return Başarılı IslemSonucu sarmalayıcısı
     */
    public static <T> IslemSonucu<T> basarili(String mesaj, T veri) {
        return IslemSonucu.<T>builder()
                .basarili(true)
                .mesaj(mesaj)
                .veri(veri)
                .zamanDamgasi(OffsetDateTime.now())
                .build();
    }

    /**
     * Hata durumunda dönecek standart işlem sonucunu üretir.
     * 
     * @param hataMesaji Hata ayrıntısı
     * @param <T> Veri tipi
     * @return Hatalı IslemSonucu sarmalayıcısı
     */
    public static <T> IslemSonucu<T> hata(String hataMesaji) {
        return IslemSonucu.<T>builder()
                .basarili(false)
                .mesaj(hataMesaji)
                .veri(null)
                .zamanDamgasi(OffsetDateTime.now())
                .build();
    }
}
