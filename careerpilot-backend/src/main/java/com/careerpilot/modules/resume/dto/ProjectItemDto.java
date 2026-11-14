package com.careerpilot.modules.resume.dto;

import java.util.List;

public record ProjectItemDto(
        String title,
        String description,
        List<String> technologies,
        String link
) {}

