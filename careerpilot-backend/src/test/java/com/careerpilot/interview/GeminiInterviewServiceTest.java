package com.careerpilot.interview;

import com.careerpilot.modules.interview.dto.EvaluationResponseDto;
import com.careerpilot.modules.interview.service.GeminiInterviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeminiInterviewServiceTest {

    private GeminiInterviewService geminiInterviewService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Empty API key triggers offline heuristic generation
        geminiInterviewService = new GeminiInterviewService("", "gemini-3.8-flash", 10, objectMapper);
    }

    @Test
    @DisplayName("Should generate role-specific heuristic questions for Java Backend role")
    void testGenerateJavaBackendQuestions() {
        List<GeminiInterviewService.GeneratedQuestion> questions =
                geminiInterviewService.generateQuestions("Senior Java Backend Engineer", "SENIOR", 5, "Java, Spring Boot", "");

        assertNotNull(questions);
        assertEquals(5, questions.size());

        for (GeminiInterviewService.GeneratedQuestion q : questions) {
            assertNotNull(q.questionText());
            assertFalse(q.questionText().isBlank());
            assertNotNull(q.expectedConcepts());
            assertFalse(q.expectedConcepts().isEmpty());
        }

        // Verify technical depth
        assertTrue(questions.stream().anyMatch(q -> q.questionText().contains("Spring Boot") || q.questionText().contains("Java")));
    }

    @Test
    @DisplayName("Should evaluate candidate answer with accuracy, clarity, and constructive feedback")
    void testEvaluateCandidateAnswer() {
        String question = "Explain the difference between optimistic and pessimistic locking in JPA.";
        List<String> concepts = List.of("Optimistic Locking", "Pessimistic Locking", "@Version", "Concurrency");
        String candidateAnswer = "Optimistic locking uses a version field (@Version) to detect concurrent updates without locking records at the database level. Pessimistic locking locks the database row directly to prevent any concurrent read/write until transaction completion.";

        EvaluationResponseDto evaluation = geminiInterviewService.evaluateAnswer(
                question, concepts, candidateAnswer, "Backend Engineer", "MID"
        );

        assertNotNull(evaluation);
        assertTrue(evaluation.getScore() >= 70, "Score should reflect solid understanding");
        assertTrue(evaluation.getTechnicalAccuracy() >= 70);
        assertTrue(evaluation.getClarityScore() >= 60);
        assertNotNull(evaluation.getFeedback());
        assertNotNull(evaluation.getModelAnswer());
    }

    @Test
    @DisplayName("Should penalize overly short or empty answers appropriately")
    void testEvaluateTooShortAnswer() {
        EvaluationResponseDto evaluation = geminiInterviewService.evaluateAnswer(
                "Question", List.of("Concept"), "idk", "Dev", "MID"
        );

        assertNotNull(evaluation);
        assertTrue(evaluation.getScore() <= 40, "Overly short answer should receive low score");
    }
}

