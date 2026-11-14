package com.careerpilot.roadmap;

import com.careerpilot.modules.roadmap.service.GeminiRoadmapService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeminiRoadmapServiceTest {

    private GeminiRoadmapService geminiRoadmapService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Empty API key triggers offline heuristic roadmap generation
        geminiRoadmapService = new GeminiRoadmapService("", "gemini-3.8-flash", 10, objectMapper);
    }

    @Test
    @DisplayName("Should generate week-by-week curriculum with practical milestones and resources")
    void testGenerateJavaBackendRoadmap() {
        List<GeminiRoadmapService.PlannedMilestone> plan = geminiRoadmapService.generateRoadmapPlan(
                "Senior Java Cloud Engineer", 6, List.of("Docker", "Kubernetes", "Kafka"), "Target Role"
        );

        assertNotNull(plan);
        assertEquals(6, plan.size());

        for (int i = 0; i < plan.size(); i++) {
            GeminiRoadmapService.PlannedMilestone m = plan.get(i);
            assertEquals(i + 1, m.weekNumber());
            assertNotNull(m.title());
            assertFalse(m.title().isBlank());
            assertNotNull(m.description());
            assertFalse(m.description().isBlank());
            assertNotNull(m.resources());
            assertFalse(m.resources().isEmpty());
        }

        // Verify specific topics covered
        assertTrue(plan.stream().anyMatch(m -> m.title().contains("Spring Boot") || m.title().contains("Java")));
        assertTrue(plan.stream().anyMatch(m -> m.title().contains("Kafka") || m.title().contains("Containerization")));
    }
}

