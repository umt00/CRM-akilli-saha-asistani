package com.akillisaha.crm.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Bitrix24 tarzı interaktif takvimde ve müşteri kartlarında gösterilecek
 * ajanda, ziyaret, numune ve teklif etkinliklerini temsil eden DTO sınıfı.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TakvimEtkinlikYaniti {

    /**
     * Ön yüzde benzersiz etkinlik anahtarı (Örn: "RANDEVU-15", "ZIYARET-4")
     */
    private String id;

    /**
     * Takvim çipinde ve ajanda kartında görünecek başlık
     */
    private String baslik;

    /**
     * Etkinliğin gerçekleşme veya hedeflenen zamanı
     */
    private OffsetDateTime tarih;

    /**
     * Bitiş veya vade zamanı (opsiyonel)
     */
    private OffsetDateTime bitisTarihi;

    /**
     * Etkinliğin tür kodu: RANDEVU, ZIYARET, NUMUNE, TEKLIF
     */
    private String tur;

    /**
     * İnsan tarafından okunabilir tür etiketi
     */
    private String turEtiketi;

    /**
     * Takvim çipi ve rozet renk kodu (Hex)
     */
    private String renkKodu;

    /**
     * İlgili müşteri firmanın ID'si
     */
    private Long firmaId;

    /**
     * İlgili firmanın ticari unvanı
     */
    private String firmaUnvani;

    /**
     * Firma lokasyonu (Şehir / İlçe / Bölge)
     */
    private String sehirBolge;

    /**
     * İlgili yetkili / muhatap kişinin adı
     */
    private String yetkiliAdSoyad;

    /**
     * Muhatap kişinin görevi/unvanı (Örn: Satın Alma Müdürü, Baş Aşçı)
     */
    private String yetkiliUnvan;

    /**
     * Muhatap kişinin doğrudan aranabilir telefon numarası
     */
    private String yetkiliTelefon;

    /**
     * Muhatap kişinin e-posta adresi
     */
    private String yetkiliEposta;

    /**
     * İşlemi yapan veya sorumlu plasiyer ID'si
     */
    private Long plasiyerId;

    /**
     * Satış temsilcisinin adı soyadı
     */
    private String plasiyerAdSoyad;

    /**
     * Etkinliğin detay açıklaması, görüşme notu veya sonraki aksiyon
     */
    private String aciklama;

    /**
     * Varsa ilgili durum (Örn: BEKLEMEDE, ACIK, KAZANILDI)
     */
    private String durum;

    /**
     * Veritabanındaki gerçek kayıt ID'si
     */
    private Long referansId;

    /**
     * Öncelik seviyesi (NORMAL, YUKSEK, GECIKMIS)
     */
    private String oncelik;
}
