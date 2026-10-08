package com.akillisaha.crm.repository;

import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Kullanıcı varlığı (Kullanici) için veritabanı erişim katmanı.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, Long> {

    /**
     * E-posta adresine göre kullanıcı kaydını bulur.
     * 
     * @param eposta Aranan e-posta adresi
     * @return Kullanıcı nesnesi (varsa)
     */
    Optional<Kullanici> findByEposta(String eposta);

    /**
     * Belirtilen e-posta adresiyle kayıtlı bir kullanıcının olup olmadığını kontrol eder.
     * 
     * @param eposta Kontrol edilecek e-posta adresi
     * @return Kayıt varsa true, yoksa false
     */
    boolean existsByEposta(String eposta);

    /**
     * Belirtilen role (YONETICI veya PLASIYER) sahip kullanıcıları listeler.
     * 
     * @param rol Kullanıcı rolü
     * @return Kullanıcılar listesi
     */
    List<Kullanici> findByRol(Rol rol);

    /**
     * Sistemde aktif durumda olan tüm kullanıcıları getirir.
     * 
     * @return Aktif kullanıcılar listesi
     */
    List<Kullanici> findByAktifMiTrue();
}
