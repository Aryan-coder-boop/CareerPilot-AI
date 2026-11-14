package com.careerpilot.modules.roadmap.service;

import com.careerpilot.modules.roadmap.entity.RoadmapMilestone;
import com.careerpilot.modules.roadmap.entity.RoadmapResource;
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

@Service
public class GeminiRoadmapService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiRoadmapService.class);
    private static final String GEMINI_API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final String apiKey;
    private final String model;
    private final int timeoutSeconds;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public record PlannedResource(String title, String url, String resourceType) {}
    public record PlannedMilestone(int weekNumber, String title, String description, List<PlannedResource> resources) {}

    public GeminiRoadmapService(
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

    public List<PlannedMilestone> generateRoadmapPlan(String targetRole, int targetWeeks,
                                                     List<String> missingSkills, String jobTitle) {
        if (!StringUtils.hasText(apiKey)) {
            logger.info("No Gemini API key configured. Executing offline heuristic roadmap generator.");
            return generateHeuristicRoadmap(targetRole, targetWeeks, missingSkills);
        }

        try {
            return callGeminiGenerateRoadmap(targetRole, targetWeeks, missingSkills, jobTitle);
        } catch (Exception e) {
            logger.warn("Live Gemini roadmap generation failed: {}. Using heuristic roadmap.", e.getMessage());
            return generateHeuristicRoadmap(targetRole, targetWeeks, missingSkills);
        }
    }

    private List<PlannedMilestone> callGeminiGenerateRoadmap(String targetRole, int targetWeeks,
                                                            List<String> missingSkills, String jobTitle) {
        String skillsContext = missingSkills != null && !missingSkills.isEmpty()
                ? String.join(", ", missingSkills)
                : "Standard competencies for " + targetRole;

        String prompt = String.format("""
            You are CareerPilot AI's Senior Technical Learning Curriculum Architect.
            Create a highly practical, week-by-week learning roadmap for an engineer aiming to become a %s.
            Duration: %d weeks.
            Key Target Skills & Gaps to Bridge: %s.

            Guidelines:
            - Each week must have a concrete, production-grade learning milestone.
            - Provide 2-3 high-quality, reputable resources (official docs, repos, guides) with realistic URLs.
            - Ensure steady progression from foundation, to distributed systems / cloud, to advanced testing.

            Return ONLY valid JSON matching this schema:
            {
              "milestones": [
                {
                  "weekNumber": 1,
                  "title": "Advanced Concurrency & JVM Internals",
                  "description": "Master virtual threads, memory models, garbage collection tuning, and lock-free primitives.",
                  "resources": [
                    {
                      "title": "Java Platform Concurrency Documentation",
                      "url": "https://docs.oracle.com/en/java/",
                      "resourceType": "DOCUMENTATION"
                    }
                  ]
                }
              ]
            }
            """, targetRole, targetWeeks, skillsContext);

        String rawResponse = invokeGemini(prompt);
        return parseRoadmapResponse(rawResponse, targetRole, targetWeeks, missingSkills);
    }

    private List<PlannedMilestone> parseRoadmapResponse(String responseJson, String targetRole,
                                                       int targetWeeks, List<String> missingSkills) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");
            String cleanedJson = cleanJsonString(textNode.asText());

            JsonNode parsed = objectMapper.readTree(cleanedJson);
            JsonNode mArray = parsed.path("milestones");

            List<PlannedMilestone> result = new ArrayList<>();
            if (mArray.isArray()) {
                for (JsonNode m : mArray) {
                    int week = m.path("weekNumber").asInt(result.size() + 1);
                    String title = m.path("title").asText("Milestone " + week);
                    String description = m.path("description").asText("Study core principles and implement hands-on exercises.");

                    List<PlannedResource> resources = new ArrayList<>();
                    JsonNode rArray = m.path("resources");
                    if (rArray.isArray()) {
                        for (JsonNode r : rArray) {
                            String rTitle = r.path("title").asText("Official Resource");
                            String url = r.path("url").asText("https://developer.mozilla.org");
                            String type = r.path("resourceType").asText("DOCUMENTATION");
                            resources.add(new PlannedResource(rTitle, url, type));
                        }
                    }
                    result.add(new PlannedMilestone(week, title, description, resources));
                }
            }

            if (result.isEmpty()) {
                return generateHeuristicRoadmap(targetRole, targetWeeks, missingSkills);
            }
            return result;
        } catch (Exception e) {
            logger.warn("Failed to parse Gemini roadmap response: {}. Falling back to heuristic.", e.getMessage());
            return generateHeuristicRoadmap(targetRole, targetWeeks, missingSkills);
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
                        throw new RuntimeException("Gemini roadmap API error: " + resp.getStatusCode() + " " + body);
                    })
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Gemini HTTP error: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Gemini timeout: " + e.getMessage(), e);
        }
    }

    public List<PlannedMilestone> generateHeuristicRoadmap(String targetRole, int targetWeeks, List<String> missingSkills) {
        List<PlannedMilestone> milestones = new ArrayList<>();
        String roleLower = targetRole.toLowerCase();

        if (roleLower.contains("java") || roleLower.contains("backend") || roleLower.contains("spring")) {
            milestones.add(new PlannedMilestone(
                    1,
                    "Modern Java 17+ Features & JVM Internals",
                    "Deep dive into records, pattern matching, sealed classes, Virtual Threads (Project Loom), and JVM garbage collection mechanics.",
                    List.of(
                            new PlannedResource("Oracle Java 17+ Documentation", "https://docs.oracle.com/en/java/javase/17/", "DOCUMENTATION"),
                            new PlannedResource("JVM Anatomy Quarks", "https://shipilev.net/jvm/anatomy-quarks/", "ARTICLE")
                    )
            ));
            milestones.add(new PlannedMilestone(
                    2,
                    "Enterprise Spring Boot 3 & Clean Architecture",
                    "Build modular monoliths with Spring Boot 3.3+, Spring Security 6, stateless JWT token rotation, and Flyway schema versioning.",
                    List.of(
                            new PlannedResource("Spring Boot Official Reference", "https://docs.spring.io/spring-boot/docs/current/reference/html/", "DOCUMENTATION"),
                            new PlannedResource("Spring Security Architecture Guide", "https://docs.spring.io/spring-security/reference/servlet/architecture.html", "DOCUMENTATION")
                    )
            ));
            milestones.add(new PlannedMilestone(
                    3,
                    "PostgreSQL Optimization & Distributed Data Caching",
                    "Master query plans with EXPLAIN ANALYZE, B-Tree and GIN indexing, database transactions, and Redis distributed caching.",
                    List.of(
                            new PlannedResource("PostgreSQL Documentation: Indexing", "https://www.postgresql.org/docs/current/indexes.html", "DOCUMENTATION"),
                            new PlannedResource("Use The Index, Luke!", "https://use-the-index-luke.com/", "ARTICLE")
                    )
            ));
            milestones.add(new PlannedMilestone(
                    4,
                    "Event-Driven Architecture with Apache Kafka",
                    "Implement asynchronous decoupled communication, consumer groups, partition keys, idempotent consumers, and dead-letter queues.",
                    List.of(
                            new PlannedResource("Apache Kafka Core Concepts", "https://kafka.apache.org/documentation/", "DOCUMENTATION"),
                            new PlannedResource("Confluent Kafka Tutorials", "https://developer.confluent.io/tutorials/", "ARTICLE")
                    )
            ));
            milestones.add(new PlannedMilestone(
                    5,
                    "Containerization & Cloud Native Deployment",
                    "Containerize Spring Boot with multi-stage Dockerfiles, configure Docker Compose, and orchestrate with Kubernetes deployments and services.",
                    List.of(
                            new PlannedResource("Docker Documentation", "https://docs.docker.com/get-started/", "DOCUMENTATION"),
                            new PlannedResource("Kubernetes Basics", "https://kubernetes.io/docs/tutorials/kubernetes-basics/", "DOCUMENTATION")
                    )
            ));
            milestones.add(new PlannedMilestone(
                    6,
                    "System Design & Mock Technical Interview Readiness",
                    "Practice end-to-end system design scenarios (rate limiters, URL shorteners, payment workflows), resiliency patterns, and behavioral STAR stories.",
                    List.of(
                            new PlannedResource("System Design Primer", "https://github.com/donnemartin/system-design-primer", "REPO"),
                            new PlannedResource("Microservice Resiliency Patterns", "https://microservices.io/patterns/reliability/circuit-breaker.html", "ARTICLE")
                    )
            ));
        } else {
            milestones.add(new PlannedMilestone(
                    1,
                    "Core Fundamentals & Modern Tooling",
                    "Establish deep mastery of language primitives, type systems, modular structure, and automated testing setups.",
                    List.of(
                            new PlannedResource("Developer Roadmaps", "https://roadmap.sh/", "ARTICLE"),
                            new PlannedResource("MDN Web Docs", "https://developer.mozilla.org/", "DOCUMENTATION")
                    )
            ));
            milestones.add(new PlannedMilestone(
                    2,
                    "State Management & Robust API Architecture",
                    "Design clean RESTful and event-driven APIs, input validation, defensive error handling, and authorization flows.",
                    List.of(
                            new PlannedResource("REST API Design Best Practices", "https://restfulapi.net/", "ARTICLE"),
                            new PlannedResource("OWASP API Security Top 10", "https://owasp.org/www-project-api-security/", "DOCUMENTATION")
                    )
            ));
            milestones.add(new PlannedMilestone(
                    3,
                    "Database Engineering & Query Performance",
                    "Schema normalization, indexing strategies, transaction isolation levels, and caching layers.",
                    List.of(
                            new PlannedResource("Database System Concepts", "https://www.postgresql.org/docs/", "DOCUMENTATION")
                    )
            ));
            milestones.add(new PlannedMilestone(
                    4,
                    "DevOps, CI/CD & Cloud Infrastructure",
                    "Automate testing, build reproducible containers, write deployment pipelines, and configure health monitoring.",
                    List.of(
                            new PlannedResource("GitHub Actions Documentation", "https://docs.github.com/en/actions", "DOCUMENTATION")
                    )
            ));
        }

        // Return up to targetWeeks
        return milestones.subList(0, Math.min(targetWeeks, milestones.size()));
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

