package com.careerpilot.resume;

import com.careerpilot.modules.resume.dto.ParsedResumeAnalysisDto;
import com.careerpilot.modules.resume.service.GeminiResumeAnalyzerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeminiResumeAnalyzerServiceTest {

    private GeminiResumeAnalyzerService analyzerService;

    @BeforeEach
    void setUp() {
        // Initialize with empty API key to test offline heuristic analyzer
        analyzerService = new GeminiResumeAnalyzerService("", "gemini-3.8-flash", 10, new ObjectMapper());
    }

    @Test
    @DisplayName("Should extract contact details and technical skills from resume text")
    void testHeuristicAnalysisExtraction() {
        String sampleResumeText = """
                Alice Smith
                alice.smith@careerpilot.ai | (555) 123-4567 | San Francisco, CA
                
                SUMMARY
                Senior Full Stack Engineer with 7 years of experience in Java, Spring Boot, React, and PostgreSQL.
                
                EXPERIENCE
                Senior Software Engineer at CloudTech (2021 - Present)
                - Architected high-throughput REST APIs handling 500k+ daily requests, improving throughput by 35%.
                - Designed automated CI/CD pipelines using Docker and AWS.
                
                EDUCATION
                B.S. in Computer Science - University of California (2020)
                """;

        ParsedResumeAnalysisDto result = analyzerService.analyzeResume(sampleResumeText);

        assertNotNull(result);
        assertEquals("alice.smith@careerpilot.ai", result.email());
        assertEquals("Alice Smith", result.candidateName());

        // Verify skills extracted
        assertTrue(result.skills().stream().anyMatch(s -> s.name().equalsIgnoreCase("Java")));
        assertTrue(result.skills().stream().anyMatch(s -> s.name().equalsIgnoreCase("Spring Boot")));
        assertTrue(result.skills().stream().anyMatch(s -> s.name().equalsIgnoreCase("React")));
        assertTrue(result.skills().stream().anyMatch(s -> s.name().equalsIgnoreCase("PostgreSQL")));

        // Verify score is between 0 and 100 and matches breakdown
        assertTrue(result.qualityScore() >= 0 && result.qualityScore() <= 100);
        assertEquals(result.qualityScore(), result.scoreBreakdown().totalScore());

        // Verify transparent disclaimer is present
        assertNotNull(result.disclaimer());
        assertTrue(result.disclaimer().contains("not an official or proprietary ATS score"));

        // Verify suggestions provided
        assertFalse(result.suggestions().isEmpty());
    }

    @Test
    @DisplayName("Should never invent missing facts when sections are absent")
    void testDoesNotInventMissingSections() {
        String minimalResumeText = """
                Bob Minimalist
                bob@example.com
                Software Developer working with Python and Linux.
                """;

        ParsedResumeAnalysisDto result = analyzerService.analyzeResume(minimalResumeText);

        assertNotNull(result);
        assertEquals("bob@example.com", result.email());
        // Projects and certifications should be empty rather than fabricated
        assertTrue(result.projects().isEmpty());
        assertTrue(result.certifications().isEmpty());

        // Absence of projects/certifications should generate an actionable improvement suggestion
        assertTrue(result.suggestions().stream().anyMatch(s -> s.category().contains("Certifications") || s.category().contains("Projects")));
    }

    @Test
    @DisplayName("Should successfully build prompt without format conversion errors")
    void testBuildPromptFormat() throws Exception {
        java.lang.reflect.Method method = GeminiResumeAnalyzerService.class.getDeclaredMethod("buildPrompt", String.class);
        method.setAccessible(true);
        String prompt = (String) method.invoke(analyzerService, "Sample resume content with 20% improvement.");
        assertNotNull(prompt);
        assertTrue(prompt.contains("(%, $, numbers, scale)"));
        assertTrue(prompt.contains("Sample resume content with 20% improvement."));
    }
}

