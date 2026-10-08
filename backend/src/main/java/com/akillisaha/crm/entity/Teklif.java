package com.akillisaha.crm.entity;

import com.akillisaha.crm.enums.TeklifDurumu;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Sahada müşteriye sunulan fiyat tekliflerini ve ticari fırsatları temsil eden varlık sınıfı.
 * 'offers' tablosu ile eşlenir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teklif {

    /**
     * Teklifin benzersiz birincil anahtarı.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Teklifin sunulduğu müşteri firma.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Firma firma;

    /**
     * Teklifin açılmasına vesile olan saha ziyareti (varsa).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id")
    private Ziyaret ziyaret;

    /**
     * Teklifi hazırlayan ve takip eden plasiyer.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representative_id", nullable = false)
    private Kullanici plasiyer;

    /**
     * Teklifin başlığı veya konusu (Örn: Kerry Nacho Yıllık Tedarik Teklifi).
     */
    @Column(name = "title", nullable = false, length = 255)
    private String baslik;

    /**
     * Teklif edilen toplam tutar (Excel Kolon N).
     */
    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal tutar;

    /**
     * Teklifin para birimi (TRY, USD, EUR).
     */
    @Column(name = "currency", length = 10)
    @Builder.Default
    private String paraBirimi = "TRY";

    /**
     * Teklifin kazanım durumu (ACIK, KAZANILDI, KAYBEDILDI, IPTAL).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    @Builder.Default
    private TeklifDurumu durum = TeklifDurumu.ACIK;

    /**
     * Teklife dair plasiyer veya yönetim notları.
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notlar;

    /**
     * Teklifin geçerli olduğu son tarih.
     */
    @Column(name = "valid_until")
    private OffsetDateTime gecerlilikTarihi;

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
