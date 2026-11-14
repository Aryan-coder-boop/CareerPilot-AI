package com.careerpilot.modules.resume.dto;

import java.util.List;

public record ParsedResumeAnalysisDto(
        String candidateName,
        String email,
        String phone,
        String location,
        String summary,
        List<SkillItemDto> skills,
        List<String> softSkills,
        List<ExperienceItemDto> experiences,
        List<EducationItemDto> education,
        List<ProjectItemDto> projects,
        List<String> certifications,
        ScoreBreakdownDto scoreBreakdown,
        int qualityScore,
        List<String> strengths,
        List<ImprovementSuggestionDto> suggestions,
        String disclaimer
) {
    public static final String DEFAULT_DISCLAIMER =
            "The CareerPilot Quality Score is an estimated diagnostic metric (0-100) based on action verbs, impact quantification, structure completeness, and clarity. It is not an official or proprietary ATS score.";
}

