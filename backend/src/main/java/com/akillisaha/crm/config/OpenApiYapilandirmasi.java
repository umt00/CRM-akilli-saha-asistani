package com.akillisaha.crm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3 canlı dokümantasyon ve tarayıcıdan JWT ile test altyapısı yapılandırması.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Configuration
public class OpenApiYapilandirmasi {

    private static final String GUVENLIK_SEMASI_ADI = "BearerAuth";

    /**
     * OpenAPI nesnesini oluşturur ve Bearer JWT yetkilendirme şemasını ekler.
     * 
     * @return OpenAPI dokümantasyon nesnesi
     */
    @Bean
    public OpenAPI crmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Akıllı Saha CRM API")
                        .description("Gıda Distribütörlüğü Saha Satış, Ziyaret, Numune & Teklif Takip Sistemi REST API Dokümantasyonu")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("CRM Çekirdek Backend Ekibi")
                                .email("backend@akillisaha.com"))
                        .license(new License().name("Şirkete Özel").url("https://akillisaha.com")))
                .addSecurityItem(new SecurityRequirement().addList(GUVENLIK_SEMASI_ADI))
                .components(new Components()
                        .addSecuritySchemes(GUVENLIK_SEMASI_ADI, new SecurityScheme()
                                .name(GUVENLIK_SEMASI_ADI)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Token ile kimlik doğrulama. Örnek: 'Bearer {token}'")));
    }
}
