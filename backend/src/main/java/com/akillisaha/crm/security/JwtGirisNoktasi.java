package com.akillisaha.crm.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Kimlik doğrulaması yapılmamış (unauthenticated) istekler korumalı bir uç noktaya erişmeye çalıştığında
 * standart Türkçe JSON hata çıktısı üreten giriş noktası.
 * 
 * @author Akıllı Saha CRM Ekibi
 */
@Component
public class JwtGirisNoktasi implements AuthenticationEntryPoint {

    private final ObjectMapper nesneDonusturucu = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest istek,
                         HttpServletResponse yanit,
                         AuthenticationException authIstisnasi) throws IOException {
        yanit.setContentType(MediaType.APPLICATION_JSON_VALUE);
        yanit.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> hataGovdesi = new HashMap<>();
        hataGovdesi.put("basarili", false);
        hataGovdesi.put("durumKodu", HttpServletResponse.SC_UNAUTHORIZED);
        hataGovdesi.put("hata", "Yetkisiz Erişim");
        hataGovdesi.put("mesaj", "Bu kaynağa erişmek için geçerli bir JWT token ile oturum açmalısınız.");
        hataGovdesi.put("yol", istek.getServletPath());

        nesneDonusturucu.writeValue(yanit.getOutputStream(), hataGovdesi);
    }
}
