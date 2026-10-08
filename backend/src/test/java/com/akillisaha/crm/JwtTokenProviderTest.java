package com.akillisaha.crm;

import com.akillisaha.crm.entity.User;
import com.akillisaha.crm.enums.Role;
import com.akillisaha.crm.security.CustomUserDetails;
import com.akillisaha.crm.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000; // 1 saat

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, expirationMs);
    }

    @Test
    void shouldGenerateAndValidateTokenSuccessfully() {
        User user = User.builder()
                .id(1L)
                .email("ahmet@akillisaha.com")
                .passwordHash("hashedPass")
                .fullName("Ahmet Yılmaz")
                .role(Role.SALES_REP)
                .isActive(true)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = jwtTokenProvider.generateToken(auth);
        assertNotNull(token);

        boolean isValid = jwtTokenProvider.validateToken(token);
        assertTrue(isValid);

        String email = jwtTokenProvider.getEmailFromToken(token);
        assertEquals("ahmet@akillisaha.com", email);
    }
}
