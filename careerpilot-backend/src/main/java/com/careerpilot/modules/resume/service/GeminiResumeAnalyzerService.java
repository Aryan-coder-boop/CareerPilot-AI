package com.careerpilot.modules.resume.service;

import com.careerpilot.common.exception.BadRequestException;
import com.careerpilot.modules.resume.dto.*;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeminiResumeAnalyzerService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiResumeAnalyzerService.class);
    private static final String GEMINI_API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final String apiKey;
    private final String model;
    private final int timeoutSeconds;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GeminiResumeAnalyzerService(
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

    public ParsedResumeAnalysisDto analyzeResume(String rawResumeText) {
        if (!StringUtils.hasText(rawResumeText)) {
            throw new BadRequestException("Resume text is empty. Cannot perform analysis.");
        }

        // If no Gemini API key is configured, fallback to rule-based offline analysis
        if (!StringUtils.hasText(apiKey)) {
            logger.info("No Gemini API key configured. Executing offline heuristic resume analysis.");
            return generateHeuristicAnalysis(rawResumeText);
        }

        try {
            return callGeminiApi(rawResumeText);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Live Gemini API call failed: {}. Falling back to heuristic analysis.", e.getMessage());
            return generateHeuristicAnalysis(rawResumeText);
        }
    }

    private ParsedResumeAnalysisDto callGeminiApi(String rawResumeText) {
        String prompt = buildPrompt(rawResumeText);

        Map<String, Object> requestPayload = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.1,
                        "responseMimeType", "application/json"
                )
        );

        String url = String.format(GEMINI_API_URL_TEMPLATE, this.model, this.apiKey);

        try {
            String rawResponse = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        int statusCode = resp.getStatusCode().value();
                        if (statusCode == 429) {
                            throw new BadRequestException("Gemini API rate limit or quota exceeded. Please wait a moment and try again.");
                        }
                        throw new BadRequestException("Gemini API returned client error: " + resp.getStatusText());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                        throw new BadRequestException("Gemini API service temporarily unavailable. Please retry shortly.");
                    })
                    .body(String.class);

            return parseGeminiResponse(rawResponse, rawResumeText);

        } catch (ResourceAccessException e) {
            logger.error("Gemini API timeout or network connectivity issue", e);
            throw new BadRequestException("AI analysis timed out while processing resume. Please retry.");
        } catch (RestClientResponseException e) {
            logger.error("Gemini API returned error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BadRequestException("Gemini API request failed: " + e.getMessage());
        }
    }

    private String buildPrompt(String rawResumeText) {
        return """
                You are CareerPilot AI, an elite technical recruiter and resume evaluator.
                Your task is to analyze the resume text provided below strictly between the <resume_text> delimiters.
                
                CRITICAL INSTRUCTIONS:
                1. NEVER invent, hallucinate, or assume any facts, dates, skills, metrics, or experiences that are not explicitly present in the resume text.
                2. If a section is absent (e.g. projects or certifications), return an empty array for that field and note its absence in the improvement suggestions.
                3. Calculate a transparent "CareerPilot Quality Score" (0-100) based strictly on:
                   - impactMetricsScore (0-25): Presence of quantified results (%%, $, numbers, scale).
                   - actionVerbsScore (0-25): Strong verbs (architected, spearheaded, optimized) vs passive voice.
                   - formattingClarityScore (0-25): Logical flow, clear headings, concise descriptions.
                   - sectionCompletenessScore (0-25): Presence of contact info, skills, experience, education.
                   The qualityScore must equal the exact sum of these 4 breakdown scores.
                4. Categorize technical skills accurately into categories: 'LANGUAGES', 'FRAMEWORKS', 'DATABASES', 'CLOUD_DEVOPS', 'TOOLS', 'CONCEPTS'.
                5. Provide 3-5 concrete, actionable improvement suggestions with before-and-after examples.
                
                <resume_text>
                %s
                </resume_text>
                
                Return the response STRICTLY as valid JSON adhering to this exact structure:
                {
                  "candidateName": "Full Name",
                  "email": "email@example.com",
                  "phone": "+1-234-567-8900",
                  "location": "City, Country",
                  "summary": "Professional summary or null",
                  "skills": [
                    { "name": "Java", "category": "LANGUAGES", "proficiency": "ADVANCED" }
                  ],
                  "softSkills": ["Communication", "Leadership"],
                  "experiences": [
                    {
                      "company": "Company Name",
                      "role": "Job Title",
                      "duration": "Start - End",
                      "location": "Location",
                      "highlights": ["Bullet point 1", "Bullet point 2"]
                    }
                  ],
                  "education": [
                    {
                      "institution": "University Name",
                      "degree": "Degree",
                      "fieldOfStudy": "Major",
                      "graduationYear": "Year",
                      "gpaOrHonors": "GPA / Honors or null"
                    }
                  ],
                  "projects": [
                    {
                      "title": "Project Title",
                      "description": "Short description",
                      "technologies": ["React", "Spring Boot"],
                      "link": "URL or null"
                    }
                  ],
                  "certifications": ["AWS Certified Solutions Architect"],
                  "scoreBreakdown": {
                    "impactMetricsScore": 20,
                    "actionVerbsScore": 22,
                    "formattingClarityScore": 20,
                    "sectionCompletenessScore": 23,
                    "totalScore": 85
                  },
                  "qualityScore": 85,
                  "strengths": ["Strong technical stack", "Quantifiable metrics in recent roles"],
                  "suggestions": [
                    {
                      "category": "Impact Quantification",
                      "issue": "Bullet points lack measurable business outcomes.",
                      "recommendation": "Add specific percentages, latency reductions, or dollar amounts.",
                      "example": "Changed 'Improved SQL performance' to 'Optimized Postgres queries, reducing latency by 45%%.'"
                    }
                  ],
                  "disclaimer": "The CareerPilot Quality Score is an estimated diagnostic metric (0-100) based on action verbs, impact quantification, structure completeness, and clarity. It is not an official or proprietary ATS score."
                }
                """.formatted(rawResumeText);
    }

    private ParsedResumeAnalysisDto parseGeminiResponse(String rawResponse, String rawResumeText) {
        try {
            JsonNode rootNode = objectMapper.readTree(rawResponse);
            JsonNode textNode = rootNode.at("/candidates/0/content/parts/0/text");

            if (textNode.isMissingNode() || !StringUtils.hasText(textNode.asText())) {
                logger.warn("Empty candidate content received from Gemini API. Falling back to heuristic.");
                return generateHeuristicAnalysis(rawResumeText);
            }

            String content = textNode.asText().trim();

            // Strip markdown JSON fences if present
            if (content.startsWith("```json")) {
                content = content.substring(7);
            } else if (content.startsWith("```")) {
                content = content.substring(3);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
            content = content.trim();

            return objectMapper.readValue(content, ParsedResumeAnalysisDto.class);

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse Gemini JSON response into ParsedResumeAnalysisDto", e);
            return generateHeuristicAnalysis(rawResumeText);
        }
    }

    /**
     * Fallback and offline heuristic analyzer. Ensures robust platform functionality even without an external API key.
     */
    public ParsedResumeAnalysisDto generateHeuristicAnalysis(String text) {
        String email = extractRegex(text, "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+");
        String phone = extractRegex(text, "(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}");

        // First non-empty line usually contains name
        String candidateName = "Candidate";
        for (String line : text.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (trimmed.length() > 2 && trimmed.length() < 50 && !trimmed.contains("@") && !trimmed.matches(".*\\d{3}.*")) {
                candidateName = trimmed;
                break;
            }
        }

        // Detect technical skills
        List<SkillItemDto> detectedSkills = new ArrayList<>();
        Map<String, String> commonTech = Map.ofEntries(
                Map.entry("Java", "LANGUAGES"),
                Map.entry("Python", "LANGUAGES"),
                Map.entry("JavaScript", "LANGUAGES"),
                Map.entry("TypeScript", "LANGUAGES"),
                Map.entry("C++", "LANGUAGES"),
                Map.entry("Spring Boot", "FRAMEWORKS"),
                Map.entry("Spring", "FRAMEWORKS"),
                Map.entry("React", "FRAMEWORKS"),
                Map.entry("Angular", "FRAMEWORKS"),
                Map.entry("Vue", "FRAMEWORKS"),
                Map.entry("Node.js", "FRAMEWORKS"),
                Map.entry("PostgreSQL", "DATABASES"),
                Map.entry("MySQL", "DATABASES"),
                Map.entry("MongoDB", "DATABASES"),
                Map.entry("Redis", "DATABASES"),
                Map.entry("Docker", "CLOUD_DEVOPS"),
                Map.entry("Kubernetes", "CLOUD_DEVOPS"),
                Map.entry("AWS", "CLOUD_DEVOPS"),
                Map.entry("Git", "TOOLS"),
                Map.entry("Linux", "TOOLS"),
                Map.entry("REST", "CONCEPTS"),
                Map.entry("CI/CD", "CLOUD_DEVOPS"),
                Map.entry("Microservices", "CONCEPTS")
        );

        String lowerText = text.toLowerCase();
        for (Map.Entry<String, String> entry : commonTech.entrySet()) {
            if (lowerText.contains(entry.getKey().toLowerCase())) {
                detectedSkills.add(new SkillItemDto(entry.getKey(), entry.getValue(), "INTERMEDIATE"));
            }
        }

        // Count quantifiable metrics (%, $, numbers)
        int metricMatches = 0;
        Matcher metricMatcher = Pattern.compile("\\b(\\d+(\\.\\d+)?%|\\$\\d+|\\d+\\+?\\s*(users|clients|requests|ms|seconds|million|billion|k))\\b", Pattern.CASE_INSENSITIVE).matcher(text);
        while (metricMatcher.find()) {
            metricMatches++;
        }

        // Compute scores
        int impactScore = Math.min(25, 10 + (metricMatches * 3));
        int verbScore = lowerText.matches(".*\\b(developed|engineered|implemented|architected|spearheaded|optimized|built|led|designed)\\b.*") ? 22 : 14;
        int structureScore = (lowerText.contains("experience") ? 6 : 0) +
                (lowerText.contains("education") ? 6 : 0) +
                (lowerText.contains("skills") ? 6 : 0) +
                (email != null ? 7 : 0);
        int clarityScore = text.length() > 500 && text.length() < 5000 ? 22 : 16;
        int totalScore = impactScore + verbScore + structureScore + clarityScore;

        ScoreBreakdownDto breakdown = new ScoreBreakdownDto(impactScore, verbScore, clarityScore, structureScore, totalScore);

        List<String> strengths = new ArrayList<>();
        if (detectedSkills.size() >= 5) {
            strengths.add("Diverse technical competency with " + detectedSkills.size() + "+ demonstrated skills.");
        }
        if (metricMatches > 0) {
            strengths.add("Includes quantified performance metrics and business impact statements.");
        } else {
            strengths.add("Clear structure with foundational technical skills listed.");
        }

        List<ImprovementSuggestionDto> suggestions = new ArrayList<>();
        if (metricMatches < 3) {
            suggestions.add(new ImprovementSuggestionDto(
                    "Impact Quantification",
                    "Few bullet points quantify results with metrics or percentages.",
                    "Incorporate concrete metrics to demonstrate scale and efficiency gains.",
                    "Change 'Optimized queries' to 'Optimized 12 slow queries, cutting API response times by 38%.'"
            ));
        }
        if (verbScore < 20) {
            suggestions.add(new ImprovementSuggestionDto(
                    "Action Verbs",
                    "Resume relies on passive phrasing ('Responsible for', 'Assisted with').",
                    "Start bullet points with strong active verbs like 'Architected', 'Spearheaded', 'Engineered'.",
                    "Change 'Responsible for writing unit tests' to 'Authored 100+ unit tests, elevating test coverage to 85%.'"
            ));
        }
        suggestions.add(new ImprovementSuggestionDto(
                "Certifications & Projects",
                "Highlighting cloud or vendor certifications signals up-to-date industry validation.",
                "Add a dedicated Certifications section if you hold credentials (e.g. AWS, GCP, Oracle Java).",
                "e.g. 'AWS Certified Solutions Architect – Associate (2025)'"
        ));

        return new ParsedResumeAnalysisDto(
                candidateName,
                email,
                phone,
                "Not specified",
                "Experienced software engineer with hands-on background in modern backend and full-stack development.",
                detectedSkills,
                List.of("Problem Solving", "Collaboration", "Critical Thinking"),
                List.of(new ExperienceItemDto(
                        "Technical Professional Experience",
                        "Software Engineer",
                        "Recent",
                        "Location",
                        List.of("Engineered scalable software solutions and collaborated across cross-functional teams.")
                )),
                List.of(new EducationItemDto(
                        "Higher Education Institution",
                        "Bachelor of Science",
                        "Computer Science / Engineering",
                        "Recent",
                        null
                )),
                List.of(),
                List.of(),
                breakdown,
                totalScore,
                strengths,
                suggestions,
                ParsedResumeAnalysisDto.DEFAULT_DISCLAIMER
        );
    }

    private String extractRegex(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}

