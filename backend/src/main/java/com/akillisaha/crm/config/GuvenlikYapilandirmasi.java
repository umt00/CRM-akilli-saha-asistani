package com.akillisaha.crm.config;

import com.akillisaha.crm.security.JwtGirisNoktasi;
import com.akillisaha.crm.security.JwtKimlikDogrulamaFiltresi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 6.x güvenlik filtre zinciri, CORS ayarları, parola şifreleyici
 * ve yetkilendirme kurallarının tanımlandığı merkezi yapılandırma sınıfı.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class GuvenlikYapilandirmasi {

    private final JwtGirisNoktasi yetkisizErisimYakayici;
    private final JwtKimlikDogrulamaFiltresi jwtFiltresi;

    @Value("${cors.allowed-origins}")
    private String izinVerilenKaynaklar;

    /**
     * Parolaların güvenle tuzlanıp hashlenmesi için BCrypt algoritmasını sağlar.
     * 
     * @return BCryptPasswordEncoder örneği
     */
    @Bean
    public PasswordEncoder parolaSifreleyici() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Kimlik doğrulama yöneticisi (AuthenticationManager) bean'ini üretir.
     * 
     * @param yapilandirma Spring Security kimlik yapılandırması
     * @return AuthenticationManager
     * @throws Exception Yönetici başlatılamazsa
     */
    @Bean
    public AuthenticationManager kimlikDogrulamaYoneticisi(AuthenticationConfiguration yapilandirma) throws Exception {
        return yapilandirma.getAuthenticationManager();
    }

    /**
     * HTTP istek filtre zincirini ve açık/kapalı uç noktaları yapılandırır.
     * 
     * @param http HttpSecurity nesnesi
     * @return Yapılandırılmış SecurityFilterChain
     * @throws Exception Filtre zinciri hatası
     */
    @Bean
    public SecurityFilterChain guvenlikFiltreZinciri(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsYapilandirmaKaynagi()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(istisna -> istisna.authenticationEntryPoint(yetkisizErisimYakayici))
                .sessionManagement(oturum -> oturum.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(yetki -> yetki
                        .requestMatchers(
                                "/api/v1/kimlik-dogrulama/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtFiltresi, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Frontend SPA (Vercel) ve yerel testler için CORS izinlerini yapılandırır.
     * 
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsYapilandirmaKaynagi() {
        CorsConfiguration ayar = new CorsConfiguration();
        List<String> kaynakListesi = Arrays.asList(izinVerilenKaynaklar.split(","));
        ayar.setAllowedOrigins(kaynakListesi);
        ayar.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        ayar.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        ayar.setAllowCredentials(true);
        ayar.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource kaynak = new UrlBasedCorsConfigurationSource();
        kaynak.registerCorsConfiguration("/**", ayar);
        return kaynak;
    }
}
