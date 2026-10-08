package com.akillisaha.crm.entity;

import com.akillisaha.crm.enums.NumuneDurumu;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Sahaya bırakılan ürün numunelerini ve durum akışını temsil eden varlık sınıfı.
 * 'samples' tablosu ile eşlenir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Entity
@Table(name = "samples")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Numune {

    /**
     * Numune kaydının benzersiz birincil anahtarı.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Numunenin teslim edildiği saha ziyareti (bağlıysa).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id")
    private Ziyaret ziyaret;

    /**
     * Numunenin verildiği müşteri firma.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Firma firma;

    /**
     * Numuneyi teslim eden ve takibinden sorumlu olan satış plasiyeri.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representative_id", nullable = false)
    private Kullanici plasiyer;

    /**
     * Varsa katalogdaki kayıtlı ürün bağlantısı.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Urun urun;

    /**
     * Numunesi verilen ürünün adı (Örn: Nacho Cheese, Çikolata Aroması).
     */
    @Column(name = "product_name", nullable = false, length = 255)
    private String urunAdi;

    /**
     * Numunenin üretici markası (Kerry, Cargill, Orkide vb.).
     */
    @Column(name = "brand", length = 100)
    private String marka;

    /**
     * Bırakılan numunenin miktarı (Örn: 250 GR, 500 GR, 25 KG).
     */
    @Column(name = "quantity", nullable = false, length = 60)
    private String miktar;

    /**
     * Numunenin mevcut yaşam döngüsü durumu (Excel Kolon L: Numune Sonucu ile senkronize).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    @Builder.Default
    private NumuneDurumu durum = NumuneDurumu.BEKLEMEDE;

    /**
     * Müşterinin tadım ve deneme sonucu belirttiği görüş/notlar.
     */
    @Column(name = "result_notes", columnDefinition = "TEXT")
    private String sonucNotlari;

    /**
     * Numunenin müşteriye gönderildiği veya elden bırakıldığı tarih.
     */
    @Column(name = "sent_date")
    private OffsetDateTime gonderimTarihi;

    /**
     * Müşterinin numuneyi onayladığı veya reddettiği kesinleşme tarihi.
     */
    @Column(name = "evaluated_date")
    private OffsetDateTime degerlendirilmeTarihi;

    /**
     * Kaydın sisteme eklenme zaman damgası.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime olusturulmaTarihi;

    /**
     * Kaydın son güncellenme zaman damgası.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime guncellenmeTarihi;
}
