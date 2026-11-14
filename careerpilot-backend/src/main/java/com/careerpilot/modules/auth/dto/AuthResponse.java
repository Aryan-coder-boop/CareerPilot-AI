package com.careerpilot.modules.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInMs,
        UserProfileDto user
) {
    public AuthResponse(String accessToken, String refreshToken, long expiresInMs, UserProfileDto user) {
        this(accessToken, refreshToken, "Bearer", expiresInMs, user);
    }
}

