package com.akillisaha.crm.security;

import com.akillisaha.crm.entity.Kullanici;
import com.akillisaha.crm.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security'nin kullanıcıyı e-posta adresi üzerinden veritabanından çekmesini sağlayan servis.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Service
@RequiredArgsConstructor
public class OzelKullaniciDetaylariServisi implements UserDetailsService {

    private final KullaniciRepository kullaniciRepository;

    /**
     * E-posta adresine göre kullanıcıyı bulur ve Spring Security UserDetails nesnesi olarak döner.
     * 
     * @param eposta Kullanıcının oturum açtığı e-posta adresi
     * @return OzelKullaniciDetaylari nesnesi
     * @throws UsernameNotFoundException Kullanıcı bulunamazsa fırlatılır
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String eposta) throws UsernameNotFoundException {
        Kullanici kullanici = kullaniciRepository.findByEposta(eposta)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + eposta));
        return new OzelKullaniciDetaylari(kullanici);
    }
}
