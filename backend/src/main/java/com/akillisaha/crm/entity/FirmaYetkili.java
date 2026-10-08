package com.akillisaha.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Müşteri firmada çalışan ve sahada görüşülen yetkili kişileri (Satın Alma Müdürü, Baş Aşçı, Üretim Şefi vb.) temsil eder.
 * 'company_contacts' tablosu ile eşlenir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Entity
@Table(name = "company_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirmaYetkili {

    /**
     * Yetkilinin benzersiz birincil anahtarı.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Yetkilinin bağlı olduğu müşteri firma.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Firma firma;

    /**
     * Yetkili kişinin tam adı ve soyadı (Excel Kolon B: Ziyaret Edilen Kişi).
     */
    @Column(name = "full_name", nullable = false, length = 120)
    private String adSoyad;

    /**
     * Yetkilinin görevi veya çalıştığı departman (Excel Kolon C: Görevi / Departmanı).
     */
    @Column(name = "department_role", length = 120)
    private String unvanGorev;

    /**
     * Yetkilinin doğrudan cep veya dahili telefon numarası.
     */
    @Column(name = "phone", length = 50)
    private String telefon;

    /**
     * Yetkilinin doğrudan iletişim kurulan e-posta adresi.
     */
    @Column(name = "email", length = 120)
    private String eposta;

    /**
     * Yetkili hakkında plasiyerin aldığı özel notlar (çalışma saatleri, karar verici profili vb.).
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notlar;

    /**
     * Yetkili kaydının oluşturulma tarihi.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime olusturulmaTarihi;

    /**
     * Yetkili kaydının son güncellenme tarihi.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime guncellenmeTarihi;
}
