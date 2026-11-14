package com.careerpilot.modules.resume.dto;

public record EducationItemDto(
        String institution,
        String degree,
        String fieldOfStudy,
        String graduationYear,
        String gpaOrHonors
) {}

