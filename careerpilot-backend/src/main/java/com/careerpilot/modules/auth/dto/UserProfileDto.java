package com.careerpilot.modules.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileDto(
        UUID id,
        String email,
        String fullName,
        String role,
        Instant createdAt
) {}

