package com.careerpilot.modules.resume.dto;

import java.time.Instant;
import java.util.UUID;

public record ResumeDetailResponseDto(
        UUID id,
        String fileName,
        Integer qualityScore,
        String processingStatus,
        Instant createdAt,
        ParsedResumeAnalysisDto analysis,
        String rawTextPreview
) {}

