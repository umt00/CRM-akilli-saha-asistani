package com.akillisaha.crm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Katman 9: Sağlık ve Keep-Alive Yapılandırması.
 * 
 * Render.com ücretsiz katmanındaki 15 dakikalık hareketsizlik sonrası oluşan
 * uyku modunu (spin-down / 50 saniyelik cold-start) engellemek amacıyla
 * UptimeRobot tarafından her 5 dakikada bir çağrılan '/actuator/health' uç noktasının
 * veritabanı canlılığını ve sistem durumunu denetlemesini sağlar.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SaglikYapilandirmasi {

    private final DataSource veriKaynagi;

    /**
     * Veritabanı bağlantı durumunu ve havuz sağlığını denetleyen özel sağlık göstergesi.
     * 
     * @return HealthIndicator nesnesi
     */
    @Bean
    public HealthIndicator veritabaniSaglikGostergesi() {
        return () -> {
            try (Connection baglanti = veriKaynagi.getConnection()) {
                if (baglanti.isValid(2)) {
                    return Health.up()
                            .withDetail("veritabani", "BAGLI")
                            .withDetail("mesaj", "PostgreSQL veritabanı aktif ve yanıt veriyor.")
                            .build();
                } else {
                    log.warn("Veritabanı bağlantısı geçerlilik testini geçemedi.");
                    return Health.down()
                            .withDetail("veritabani", "GECERSIZ")
                            .withDetail("hata", "Veritabanı bağlantısı zaman aşımına uğradı.")
                            .build();
                }
            } catch (Exception istisna) {
                log.error("Sağlık kontrolü sırasında veritabanı bağlantı hatası: {}", istisna.getMessage());
                return Health.down(istisna)
                        .withDetail("veritabani", "KOPUK")
                        .withDetail("hata", istisna.getMessage())
                        .build();
            }
        };
    }
}
