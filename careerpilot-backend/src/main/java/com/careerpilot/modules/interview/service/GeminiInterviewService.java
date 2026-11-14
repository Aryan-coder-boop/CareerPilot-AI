package com.careerpilot.modules.interview.service;

import com.careerpilot.common.exception.BadRequestException;
import com.careerpilot.modules.interview.dto.EvaluationResponseDto;
import com.careerpilot.modules.interview.entity.InterviewExchange;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class GeminiInterviewService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiInterviewService.class);
    private static final String GEMINI_API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final String apiKey;
    private final String model;
    private final int timeoutSeconds;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public record GeneratedQuestion(String questionText, List<String> expectedConcepts) {}

    public GeminiInterviewService(
            @Value("${careerpilot.ai.gemini.api-key:}") String apiKey,
            @Value("${careerpilot.ai.gemini.chat-model:gemini-3.8-flash}") String model,
            @Value("${careerpilot.ai.gemini.timeout-seconds:45}") int timeoutSeconds,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = StringUtils.hasText(model) ? model.trim() : "gemini-3.8-flash";
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : 45;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(this.timeoutSeconds));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public List<GeneratedQuestion> generateQuestions(String targetRole, String difficulty, int count,
                                                     String candidateSkills, String jobContext) {
        if (!StringUtils.hasText(apiKey)) {
            logger.info("No Gemini API key configured. Executing offline question generator.");
            return generateHeuristicQuestions(targetRole, difficulty, count);
        }

        try {
            return callGeminiGenerateQuestions(targetRole, difficulty, count, candidateSkills, jobContext);
        } catch (Exception e) {
            logger.warn("Live Gemini question generation failed: {}. Using heuristic questions.", e.getMessage());
            return generateHeuristicQuestions(targetRole, difficulty, count);
        }
    }

    private List<GeneratedQuestion> callGeminiGenerateQuestions(String targetRole, String difficulty, int count,
                                                               String candidateSkills, String jobContext) {
        String prompt = String.format("""
            You are CareerPilot AI's Principal Technical Interviewer.
            Generate exactly %d technical interview questions for a %s level %s.

            Candidate context:
            Skills: %s
            Job description highlights: %s

            Requirements:
            - Blend fundamental concepts, practical scenarios, architectural decision-making, and troubleshooting.
            - Difficulty should strictly match: %s.
            - For each question, supply 3-5 core technical concepts expected in a complete answer.

            Return ONLY valid JSON matching this schema:
            {
              "questions": [
                {
                  "questionText": "Can you explain how Java's garbage collection works, specifically G1GC versus ZGC?",
                  "expectedConcepts": ["G1GC", "ZGC", "Stop-the-world pauses", "Memory regions", "Generational GC"]
                }
              ]
            }
            """, count, difficulty, targetRole,
                StringUtils.hasText(candidateSkills) ? candidateSkills : "Standard profile",
                StringUtils.hasText(jobContext) ? jobContext : "General engineering requirements",
                difficulty
        );

        String rawResponse = invokeGemini(prompt);
        return parseQuestionsResponse(rawResponse, count, targetRole, difficulty);
    }

    private List<GeneratedQuestion> parseQuestionsResponse(String responseJson, int count, String targetRole, String difficulty) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");
            String cleanedJson = cleanJsonString(textNode.asText());

            JsonNode parsed = objectMapper.readTree(cleanedJson);
            JsonNode qArray = parsed.path("questions");

            List<GeneratedQuestion> result = new ArrayList<>();
            if (qArray.isArray()) {
                for (JsonNode q : qArray) {
                    String qText = q.path("questionText").asText("").trim();
                    if (!StringUtils.hasText(qText)) continue;

                    List<String> concepts = new ArrayList<>();
                    JsonNode cNode = q.path("expectedConcepts");
                    if (cNode.isArray()) {
                        for (JsonNode c : cNode) {
                            if (c.isTextual() && StringUtils.hasText(c.asText())) {
                                concepts.add(c.asText().trim());
                            }
                        }
                    }
                    result.add(new GeneratedQuestion(qText, concepts));
                }
            }

            if (result.isEmpty()) {
                return generateHeuristicQuestions(targetRole, difficulty, count);
            }
            return result;
        } catch (Exception e) {
            logger.warn("Failed to parse Gemini questions response: {}. Falling back to heuristic.", e.getMessage());
            return generateHeuristicQuestions(targetRole, difficulty, count);
        }
    }

    public EvaluationResponseDto evaluateAnswer(String questionText, List<String> expectedConcepts,
                                               String userAnswer, String targetRole, String difficulty) {
        if (!StringUtils.hasText(apiKey)) {
            logger.info("No Gemini API key configured. Executing offline heuristic answer evaluation.");
            return generateHeuristicEvaluation(questionText, expectedConcepts, userAnswer);
        }

        try {
            return callGeminiEvaluateAnswer(questionText, expectedConcepts, userAnswer, targetRole, difficulty);
        } catch (Exception e) {
            logger.warn("Live Gemini answer evaluation failed: {}. Using heuristic evaluation.", e.getMessage());
            return generateHeuristicEvaluation(questionText, expectedConcepts, userAnswer);
        }
    }

    private EvaluationResponseDto callGeminiEvaluateAnswer(String questionText, List<String> expectedConcepts,
                                                          String userAnswer, String targetRole, String difficulty) {
        String prompt = String.format("""
            You are CareerPilot AI's Senior Technical Interview Evaluator.
            Grade the candidate's answer to this technical interview question.

            Target Role: %s (Level: %s)
            Question: "%s"
            Expected Core Concepts: %s

            Candidate's Answer:
            <ANSWER>
            %s
            </ANSWER>

            Evaluation Guidelines:
            - technicalAccuracy (0-100): Correctness, technical depth, handling edge cases.
            - clarityScore (0-100): Communication structure, precision, conciseness.
            - score (0-100): Weighted overall: (technicalAccuracy * 0.70) + (clarityScore * 0.30).
            - feedback: Constructive 2-3 sentence breakdown of what was strong and what crucial points were missed.
            - modelAnswer: Concise, exemplary 3-4 sentence response demonstrating ideal depth.

            Return ONLY valid JSON matching this schema:
            {
              "score": 85,
              "technicalAccuracy": 88,
              "clarityScore": 80,
              "feedback": "Strong understanding of concurrent execution, but missed explaining locks vs atomics.",
              "modelAnswer": "An exemplary response would state that..."
            }
            """, targetRole, difficulty, questionText,
                String.join(", ", expectedConcepts),
                userAnswer
        );

        String rawResponse = invokeGemini(prompt);
        return parseEvaluationResponse(rawResponse, questionText, expectedConcepts, userAnswer);
    }

    private EvaluationResponseDto parseEvaluationResponse(String responseJson, String questionText,
                                                         List<String> expectedConcepts, String userAnswer) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");
            String cleanedJson = cleanJsonString(textNode.asText());

            JsonNode parsed = objectMapper.readTree(cleanedJson);
            int technicalAccuracy = Math.max(0, Math.min(100, parsed.path("technicalAccuracy").asInt(75)));
            int clarityScore = Math.max(0, Math.min(100, parsed.path("clarityScore").asInt(75)));
            int score = parsed.has("score") ?
                    Math.max(0, Math.min(100, parsed.path("score").asInt())) :
                    (int) Math.round(technicalAccuracy * 0.70 + clarityScore * 0.30);

            String feedback = parsed.path("feedback").asText("Good response addressing the core question.");
            String modelAnswer = parsed.path("modelAnswer").asText("A comprehensive answer highlights key technical concepts.");

            return new EvaluationResponseDto(
                    0, score, technicalAccuracy, clarityScore, feedback, modelAnswer,
                    false, null, null, null
            );
        } catch (Exception e) {
            logger.warn("Failed to parse Gemini evaluation response: {}. Falling back to heuristic.", e.getMessage());
            return generateHeuristicEvaluation(questionText, expectedConcepts, userAnswer);
        }
    }

    public String generateOverallFeedback(String targetRole, String difficulty, int overallScore, List<InterviewExchange> exchanges) {
        if (!StringUtils.hasText(apiKey)) {
            return String.format(
                    "Candidate achieved an overall score of %d/100 in the %s (%s) technical mock interview. " +
                    "Demonstrated good baseline domain knowledge with opportunities to articulate system edge cases and architectural trade-offs.",
                    overallScore, targetRole, difficulty
            );
        }

        try {
            StringBuilder transcript = new StringBuilder();
            for (InterviewExchange ex : exchanges) {
                transcript.append(String.format("Q%d: %s\nScore: %s\nFeedback: %s\n\n",
                        ex.getQuestionIndex(), ex.getQuestionText(), ex.getScore(), ex.getFeedback()));
            }

            String prompt = String.format("""
                Synthesize a professional 3-4 sentence overall performance evaluation for a %s (%s) interview.
                Overall Score: %d/100.
                Transcript summary:
                %s

                Highlight top strengths, primary technical areas for revision, and actionable interview tips.
                """, targetRole, difficulty, overallScore, transcript);

            String raw = invokeGemini(prompt);
            JsonNode root = objectMapper.readTree(raw);
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText().trim();
        } catch (Exception e) {
            return String.format(
                    "Overall evaluation score of %d/100 across %d questions. Candidate displayed strong fundamentals for %s with recommendations to provide concrete metrics and trade-off considerations in upcoming technical rounds.",
                    overallScore, exchanges.size(), targetRole
            );
        }
    }

    private String invokeGemini(String prompt) {
        Map<String, Object> payload = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "responseMimeType", "application/json"
                )
        );

        String url = String.format(GEMINI_API_URL_TEMPLATE, this.model, this.apiKey);

        try {
            return restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        String body = new String(resp.getBody().readAllBytes());
                        throw new RuntimeException("Gemini API invocation error: " + resp.getStatusCode() + " " + body);
                    })
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Gemini HTTP error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Gemini timeout: " + e.getMessage(), e);
        }
    }

    public List<GeneratedQuestion> generateHeuristicQuestions(String targetRole, String difficulty, int count) {
        List<GeneratedQuestion> bank = new ArrayList<>();
        String roleLower = targetRole.toLowerCase();

        if (roleLower.contains("java") || roleLower.contains("backend") || roleLower.contains("spring")) {
            bank.add(new GeneratedQuestion(
                    "How does Spring Boot manage dependency injection and bean lifecycles, and what are the trade-offs between constructor injection and field injection?",
                    List.of("IoC Container", "Constructor Injection", "Immutability", "Bean Lifecycle", "Testability")
            ));
            bank.add(new GeneratedQuestion(
                    "Explain the difference between optimistic and pessimistic locking in JPA/Hibernate, and when you would choose one over the other.",
                    List.of("Optimistic Locking", "Pessimistic Locking", "@Version", "Concurrency", "Database Deadlocks")
            ));
            bank.add(new GeneratedQuestion(
                    "How would you design a rate limiter in a distributed microservices environment using Redis?",
                    List.of("Token Bucket", "Sliding Window", "Redis INCR", "Latency", "Distributed Systems")
            ));
            bank.add(new GeneratedQuestion(
                    "What strategies do you use for indexing PostgreSQL tables, and how do you analyze a slow query using EXPLAIN ANALYZE?",
                    List.of("B-Tree Index", "EXPLAIN ANALYZE", "Seq Scan vs Index Scan", "Composite Index", "Query Planning")
            ));
            bank.add(new GeneratedQuestion(
                    "Explain the architecture of Kafka and how consumer groups achieve horizontal scaling and message ordering guarantees.",
                    List.of("Partitions", "Consumer Groups", "Partition Key", "Rebalance", "Offset Management")
            ));
        } else if (roleLower.contains("react") || roleLower.contains("frontend") || roleLower.contains("full stack")) {
            bank.add(new GeneratedQuestion(
                    "How does React's Virtual DOM and reconciliation algorithm work, and what role do keys play in list rendering?",
                    List.of("Virtual DOM", "Reconciliation", "Fiber Architecture", "Component Keys", "Rerendering")
            ));
            bank.add(new GeneratedQuestion(
                    "Explain the differences between useMemo, useCallback, and React.memo, and identify common pitfalls when optimizing re-renders.",
                    List.of("useMemo", "useCallback", "Referential Equality", "Memoization", "Performance")
            ));
            bank.add(new GeneratedQuestion(
                    "How do you implement client-side token refresh and handle simultaneous 401 requests with an Axios interceptor queue?",
                    List.of("JWT", "Axios Interceptors", "Promise Queue", "Refresh Token Rotation", "Race Conditions")
            ));
            bank.add(new GeneratedQuestion(
                    "What are Web Core Vitals, and how do you optimize Largest Contentful Paint (LCP) and Cumulative Layout Shift (CLS)?",
                    List.of("LCP", "CLS", "Asset Preloading", "Layout Stability", "Critical CSS")
            ));
            bank.add(new GeneratedQuestion(
                    "How do REST and GraphQL compare regarding over-fetching, caching, and network roundtrips?",
                    List.of("Over-fetching", "Under-fetching", "Schema Definition", "HTTP Caching", "Resolver Performance")
            ));
        } else {
            bank.add(new GeneratedQuestion(
                    "Describe your approach to designing resilient RESTful microservices that gracefully handle network latency and cascading failures.",
                    List.of("Circuit Breaker", "Timeouts", "Retries with Backoff", "Idempotency", "Bulkhead Pattern")
            ));
            bank.add(new GeneratedQuestion(
                    "How do you ensure database data consistency across distributed transactions without introducing long-lived locks?",
                    List.of("Saga Pattern", "Eventual Consistency", "Outbox Pattern", "Compensating Transactions", "Two-Phase Commit")
            ));
            bank.add(new GeneratedQuestion(
                    "Explain how you design, implement, and monitor a CI/CD pipeline with automated testing, linting, and zero-downtime deployment.",
                    List.of("Blue-Green Deployment", "Canary Release", "Automated Smoke Tests", "Docker", "Rollback Strategy")
            ));
            bank.add(new GeneratedQuestion(
                    "How do you secure modern web applications against OWASP Top 10 vulnerabilities like SQL injection, XSS, and CSRF?",
                    List.of("Parameterized Queries", "CSP Headers", "SameSite Cookies", "CORS Configuration", "Input Sanitization")
            ));
            bank.add(new GeneratedQuestion(
                    "Walk through how you profile and troubleshoot high memory usage or CPU spikes in a containerized production service.",
                    List.of("Heap Dumps", "Thread Dumps", "Prometheus Metrics", "APM Profiling", "Resource Limits")
            ));
        }

        // Return up to count questions
        return bank.subList(0, Math.min(count, bank.size()));
    }

    public EvaluationResponseDto generateHeuristicEvaluation(String questionText, List<String> expectedConcepts, String userAnswer) {
        if (!StringUtils.hasText(userAnswer) || userAnswer.trim().length() < 15) {
            return new EvaluationResponseDto(
                    0, 30, 25, 35,
                    "Answer is too brief to demonstrate technical competency. Please provide concrete technical explanations.",
                    "An effective answer defines the core mechanism, provides real-world trade-offs, and mentions key concepts like " +
                            (expectedConcepts != null && !expectedConcepts.isEmpty() ? String.join(", ", expectedConcepts) : "architecture principles") + ".",
                    false, null, null, null
            );
        }

        String lowerAns = userAnswer.toLowerCase();
        int matchedConcepts = 0;
        if (expectedConcepts != null) {
            for (String c : expectedConcepts) {
                String cLower = c.toLowerCase().trim();
                String cClean = cLower.replaceAll("[^a-z0-9]", "");
                if (lowerAns.contains(cLower) || (cClean.length() >= 4 && lowerAns.contains(cClean.substring(0, Math.min(6, cClean.length()))))) {
                    matchedConcepts++;
                }
            }
        }

        int totalConcepts = expectedConcepts != null && !expectedConcepts.isEmpty() ? expectedConcepts.size() : 3;
        int conceptCoveragePct = (int) Math.round(((double) matchedConcepts / totalConcepts) * 100);

        int wordCount = userAnswer.trim().split("\\s+").length;
        int lengthBonus = Math.min(25, wordCount / 3);

        int technicalAccuracy = Math.min(95, Math.max(45, 30 + (conceptCoveragePct * 5 / 10) + lengthBonus));
        int clarityScore = Math.min(95, Math.max(50, 45 + (wordCount > 25 ? 35 : 20)));
        int score = (int) Math.round(technicalAccuracy * 0.70 + clarityScore * 0.30);

        String feedback;
        if (matchedConcepts >= 2) {
            feedback = String.format("Strong technical response addressing %d key concepts (%s). Well articulated with clear understanding.",
                    matchedConcepts, String.join(", ", expectedConcepts.stream().limit(3).toList()));
        } else {
            feedback = "Good baseline attempt, but missed explicitly elaborating on crucial concepts such as: " +
                    (expectedConcepts != null && !expectedConcepts.isEmpty() ? String.join(", ", expectedConcepts) : "key terminology") + ".";
        }

        String modelAnswer = "A top-tier answer covers: " +
                (expectedConcepts != null ? String.join(", ", expectedConcepts) : "core concepts") +
                ", explains why this architectural choice is made, and highlights performance or operational considerations.";

        return new EvaluationResponseDto(
                0, score, technicalAccuracy, clarityScore, feedback, modelAnswer,
                false, null, null, null
        );
    }

    private String cleanJsonString(String raw) {
        if (!StringUtils.hasText(raw)) return "{}";
        String s = raw.trim();
        if (s.startsWith("```json")) s = s.substring(7);
        else if (s.startsWith("```")) s = s.substring(3);
        if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        return s.trim();
    }
}
