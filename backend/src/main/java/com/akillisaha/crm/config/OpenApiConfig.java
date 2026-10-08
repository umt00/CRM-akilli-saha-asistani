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

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI crmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Akıllı Saha CRM API")
                        .description("Gıda Distribütörlüğü Saha Satış, Ziyaret, Numune & Teklif Takip Sistemi REST API Dokümantasyonu")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("CRM Core Backend Team")
                                .email("backend@akillisaha.com"))
                        .license(new License().name("Proprietary").url("https://akillisaha.com")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Token ile kimlik doğrulama. Örnek: 'Bearer {token}'")));
    }
}
