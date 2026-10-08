package com.akillisaha.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Satışı yapılan ve numunesi dağıtılan gıda bileşenleri kataloğunu (Kerry, Cargill, Orkide vb.) temsil eder.
 * 'products' tablosu ile eşlenir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Urun {

    /**
     * Ürünün benzersiz birincil anahtarı.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ürünün stok kodu veya barkod kodu (Örn: KRY-001).
     */
    @Column(name = "code", unique = true, length = 60)
    private String kod;

    /**
     * Ürünün ticari adı (Örn: Nacho Cheese Flavour, Karamel Aroması, DB-82 H Kakao).
     */
    @Column(name = "name", nullable = false, length = 255)
    private String ad;

    /**
     * Ürünün üretici markası (Kerry, Cargill, Orkide vb.).
     */
    @Column(name = "brand", nullable = false, length = 100)
    private String marka;

    /**
     * Ürün kategorisi (Aroma, Kakao, Endüstriyel Yağ, Sos vb.).
     */
    @Column(name = "category", length = 100)
    private String kategori;

    /**
     * Ürünün standart satış birimi (KG, GR, ADET, KOLI).
     */
    @Column(name = "unit", length = 30)
    @Builder.Default
    private String birim = "KG";

    /**
     * Ürünün katalogda aktif olarak satışta bulunup bulunmadığı.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean aktifMi = true;

    /**
     * Ürünün sisteme kaydedilme zaman damgası.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime olusturulmaTarihi;
}
