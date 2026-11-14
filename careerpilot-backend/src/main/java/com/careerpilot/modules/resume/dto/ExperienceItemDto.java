package com.careerpilot.modules.resume.dto;

import java.util.List;

public record ExperienceItemDto(
        String company,
        String role,
        String duration,
        String location,
        List<String> highlights
) {}

