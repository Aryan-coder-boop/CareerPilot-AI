package com.careerpilot.security;

import com.careerpilot.common.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 60000; // 1 min
    private final long refreshExpirationMs = 120000;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(secret, expirationMs, refreshExpirationMs);
    }

    @Test
    @DisplayName("Should successfully generate and validate a JWT access token")
    void testGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        String email = "test@careerpilot.ai";
        String fullName = "Test Engineer";
        String role = "ROLE_USER";

        String token = tokenProvider.generateTokenFromUser(userId, email, fullName, role);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(email, tokenProvider.getEmailFromToken(token));
        assertEquals(userId, tokenProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("Should return false for a malformed token")
    void testValidateMalformedToken() {
        assertFalse(tokenProvider.validateToken("invalid.jwt.token"));
    }

    @Test
    @DisplayName("Should return false for an empty token")
    void testValidateEmptyToken() {
        assertFalse(tokenProvider.validateToken(""));
    }
}

