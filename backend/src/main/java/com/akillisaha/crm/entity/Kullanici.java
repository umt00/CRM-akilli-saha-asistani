package com.akillisaha.crm.entity;

import com.akillisaha.crm.enums.Rol;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Sistemde oturum açabilen kullanıcıları (Yönetici ve Saha Satış Temsilcileri) temsil eden varlık sınıfı.
 * 'users' veritabanı tablosu ile eşlenir.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kullanici {

    /**
     * Kullanıcının benzersiz birincil anahtarı (Primary Key).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Kullanıcının sisteme giriş yaptığı e-posta adresi. Sistem genelinde benzersizdir.
     */
    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String eposta;

    /**
     * BCrypt ile tuzlanarak (salt) şifrelenmiş parola hash değeri. Asla düz metin saklanmaz.
     */
    @Column(name = "password_hash", nullable = false)
    private String sifreOzeti;

    /**
     * Kullanıcının adı ve soyadı.
     */
    @Column(name = "full_name", nullable = false, length = 120)
    private String adSoyad;

    /**
     * Kullanıcının sistemdeki rolü (YONETICI veya PLASIYER).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Rol rol;

    /**
     * Kullanıcının kurumsal iletişim telefon numarası.
     */
    @Column(name = "phone", length = 30)
    private String telefon;

    /**
     * Kullanıcı hesabının aktif olup olmadığı durumu. Pasife alınan kullanıcılar oturum açamaz.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean aktifMi = true;

    /**
     * Kullanıcı kaydının ilk oluşturulma zaman damgası.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime olusturulmaTarihi;

    /**
     * Kullanıcı kaydının son güncellenme zaman damgası.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime guncellenmeTarihi;
}
