package com.akillisaha.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Saha satış temsilcisinin müşteriye yaptığı fiziki veya online görüşmeleri temsil eder.
 * 17 kolonluk Excel şablonunun çekirdeğini oluşturur ve 'visits' tablosu ile eşlenir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Entity
@Table(name = "visits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ziyaret {

    /**
     * Ziyaret kaydının benzersiz birincil anahtarı.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ziyaret edilen müşteri firma.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Firma firma;

    /**
     * Ziyarette bizzat görüşülen yetkili kişi (varsa).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private FirmaYetkili yetkili;

    /**
     * Ziyareti gerçekleştiren saha satış temsilcisi (Plasiyer).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representative_id", nullable = false)
    private Kullanici plasiyer;

    /**
     * Ziyaretin yapıldığı tarih ve saat (Excel Kolon E).
     */
    @Column(name = "visit_date", nullable = false)
    private OffsetDateTime ziyaretTarihi;

    /**
     * Ziyaretin temel konusu veya gündem maddesi (Excel Kolon J).
     */
    @Column(name = "topic", nullable = false, length = 255)
    private String ziyaretKonusu;

    /**
     * Ziyarette görüşülen tedarik ürünleri (Excel Kolon F).
     */
    @Column(name = "supplied_products", columnDefinition = "TEXT")
    private String tedarikEttigiUrunler;

    /**
     * Ziyarette teyit edilen aylık kullanım miktarı (Excel Kolon G).
     */
    @Column(name = "monthly_consumption", length = 100)
    private String aylikKullanimMiktari;

    /**
     * Ziyarette bahsedilen bizden aldığı ürünler (Excel Kolon H).
     */
    @Column(name = "purchased_products", columnDefinition = "TEXT")
    private String bizdenAldigiUrunler;

    /**
     * Ziyarette öğrenilen güncel rakip tedarikçi bilgisi (Excel Kolon I).
     */
    @Column(name = "current_supplier_competitor", length = 255)
    private String mevcutTedarikciRakip;

    /**
     * Görüşmede müşteriye numune bırakılıp bırakılmadığı bayrağı (Excel Kolon K).
     */
    @Column(name = "has_sample", nullable = false)
    @Builder.Default
    private Boolean numuneVerildiMi = false;

    /**
     * Görüşmede müşteriye teklif sunulup sunulmadığı bayrağı (Excel Kolon M).
     */
    @Column(name = "has_offer", nullable = false)
    @Builder.Default
    private Boolean teklifVerildiMi = false;

    /**
     * Ziyaret sonrası atılacak sonraki aksiyon adımı (Excel Kolon O).
     */
    @Column(name = "next_action", columnDefinition = "TEXT")
    private String sonrakiAksiyon;

    /**
     * Randevu / Ajanda: Bir sonraki planlanan ziyaret tarihi (Excel Kolon P).
     */
    @Column(name = "next_visit_date")
    private OffsetDateTime sonrakiZiyaretTarihi;

    /**
     * Ziyaret hakkında plasiyerin aldığı detaylı saha notları (Excel Kolon Q).
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notlar;

    /**
     * Bu ziyaret sırasında açılan ve takip edilen ürün numuneleri.
     */
    @OneToMany(mappedBy = "ziyaret", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Numune> numuneler = new ArrayList<>();

    /**
     * Bu ziyaret sırasında açılan ticari teklifler / fırsatlar.
     */
    @OneToMany(mappedBy = "ziyaret", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Teklif> teklifler = new ArrayList<>();

    /**
     * Ziyaret kaydının sisteme girilme tarihi.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime olusturulmaTarihi;

    /**
     * Ziyaret kaydının güncellenme tarihi.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime guncellenmeTarihi;
}
