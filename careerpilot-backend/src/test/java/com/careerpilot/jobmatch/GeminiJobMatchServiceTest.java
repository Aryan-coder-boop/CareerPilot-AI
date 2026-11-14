package com.careerpilot.jobmatch;

import com.careerpilot.modules.auth.entity.Role;
import com.careerpilot.modules.auth.entity.User;
import com.careerpilot.modules.jobmatch.dto.JobMatchReportDto;
import com.careerpilot.modules.jobmatch.service.GeminiJobMatchService;
import com.careerpilot.modules.resume.entity.Resume;
import com.careerpilot.modules.resume.entity.ResumeSkill;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GeminiJobMatchServiceTest {

    private GeminiJobMatchService geminiJobMatchService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Construct with empty API key to test deterministic heuristic matching
        geminiJobMatchService = new GeminiJobMatchService("", "gemini-3.8-flash", 10, objectMapper);
    }

    @Test
    @DisplayName("Should extract matched and missing skills correctly in offline heuristic mode")
    void testHeuristicJobMatch() {
        User user = new User("jane@example.com", "hash", "Jane Doe", Role.ROLE_USER);
        user.setId(UUID.randomUUID());

        Resume resume = new Resume(user, "test_resume.pdf", "/uploads/test.pdf");
        resume.setId(UUID.randomUUID());
        resume.setRawText("Proficient in Java, Spring Boot, PostgreSQL, and Git with 4 years experience.");

        ResumeSkill skill1 = new ResumeSkill(resume, "Java", "BACKEND", "ADVANCED");
        ResumeSkill skill2 = new ResumeSkill(resume, "Spring Boot", "BACKEND", "ADVANCED");
        ResumeSkill skill3 = new ResumeSkill(resume, "PostgreSQL", "DATABASE", "INTERMEDIATE");

        String jobTitle = "Senior Java Cloud Engineer";
        String companyName = "Acme Cloud Corp";
        String jobDescription = """
                We are looking for a Senior Java Cloud Engineer.
                Requirements:
                - Strong proficiency in Java and Spring Boot.
                - Hands-on experience with Docker and Kubernetes for container orchestration.
                - Knowledge of AWS cloud infrastructure and CI/CD pipelines.
                - Database experience with PostgreSQL.
                """;

        JobMatchReportDto report = geminiJobMatchService.matchResumeWithJob(
                resume,
                List.of(skill1, skill2, skill3),
                jobTitle,
                companyName,
                jobDescription
        );

        assertNotNull(report);
        assertEquals(jobTitle, report.getJobTitle());
        assertEquals(companyName, report.getCompanyName());
        assertTrue(report.getMatchScore() > 0 && report.getMatchScore() <= 100);

        // Check matched skills contains Java, Spring Boot, PostgreSQL
        List<String> matched = report.getMatchedSkills();
        assertTrue(matched.stream().anyMatch(s -> s.equalsIgnoreCase("Java")));
        assertTrue(matched.stream().anyMatch(s -> s.equalsIgnoreCase("Spring Boot")));
        assertTrue(matched.stream().anyMatch(s -> s.equalsIgnoreCase("PostgreSQL")));

        // Check missing skills identifies Docker, Kubernetes, AWS
        assertTrue(report.getMissingSkills().stream().anyMatch(s -> s.getSkillName().equalsIgnoreCase("Docker")));
        assertTrue(report.getMissingSkills().stream().anyMatch(s -> s.getSkillName().equalsIgnoreCase("Kubernetes")));
        assertTrue(report.getMissingSkills().stream().anyMatch(s -> s.getSkillName().equalsIgnoreCase("AWS")));

        // Check recommendations
        assertFalse(report.getRecommendations().isEmpty());
    }
}
