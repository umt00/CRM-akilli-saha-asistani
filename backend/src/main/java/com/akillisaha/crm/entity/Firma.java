package com.akillisaha.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sahada ziyaret edilen müşteri ve hedef firmaları temsil eden varlık sınıfı.
 * 'companies' tablosu ile eşlenir ve 17 kolonluk Excel'deki müşteri pazar bilgilerini tutar.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Firma {

    /**
     * Firmanın benzersiz birincil anahtarı.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Müşteri firma resmi ticari unvanı veya tabela adı.
     */
    @Column(name = "name", nullable = false, length = 255)
    private String unvan;

    /**
     * Firmanın bulunduğu şehir veya plasiyer bölgesi (Örn: İstanbul Anadolu, Kadıköy).
     */
    @Column(name = "city_region", length = 100)
    private String sehirBolge;

    /**
     * Firmanın açık cadde/sokak/ilçe adresi.
     */
    @Column(name = "address", columnDefinition = "TEXT")
    private String adres;

    /**
     * Firmanın sabit santral veya genel iletişim telefonu.
     */
    @Column(name = "phone", length = 50)
    private String telefon;

    /**
     * Firmanın kurumsal e-posta adresi.
     */
    @Column(name = "email", length = 120)
    private String eposta;

    /**
     * Excel Kolon I: Müşterinin mevcut çalıştığı rakip tedarikçi bilgisi.
     */
    @Column(name = "current_supplier_competitor", length = 255)
    private String mevcutTedarikciRakip;

    /**
     * Excel Kolon F: Firmanın dışarıdan tedarik ettiği hammadde/bileşen ürünleri.
     */
    @Column(name = "supplied_products", columnDefinition = "TEXT")
    private String tedarikEttigiUrunler;

    /**
     * Excel Kolon G: Firmanın bu ürün grubundaki aylık tahmini kullanım/tüketim miktarı.
     */
    @Column(name = "monthly_consumption", length = 100)
    private String aylikKullanimMiktari;

    /**
     * Excel Kolon H: Firmanın halihazırda bizden satın aldığı ürünler.
     */
    @Column(name = "purchased_products", columnDefinition = "TEXT")
    private String bizdenAldigiUrunler;

    /**
     * Veri İzolasyonu: Bu firmadan ve portföyünden sorumlu olan saha plasiyeri.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private Kullanici atananPlasiyer;

    /**
     * Firmada çalışan kayıtlı yetkili kişilerin (şef, satın almacı) listesi.
     */
    @OneToMany(mappedBy = "firma", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FirmaYetkili> yetkililer = new ArrayList<>();

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
