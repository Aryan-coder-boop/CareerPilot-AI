package com.careerpilot.modules.resume.dto;

public record ScoreBreakdownDto(
        int impactMetricsScore,
        int actionVerbsScore,
        int formattingClarityScore,
        int sectionCompletenessScore,
        int totalScore
) {}

