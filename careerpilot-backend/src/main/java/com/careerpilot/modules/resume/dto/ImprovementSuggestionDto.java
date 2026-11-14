package com.careerpilot.modules.resume.dto;

public record ImprovementSuggestionDto(
        String category,
        String issue,
        String recommendation,
        String example
) {}

