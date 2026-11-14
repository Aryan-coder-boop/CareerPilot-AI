package com.careerpilot.modules.jobmatch.service;

import com.careerpilot.common.exception.BadRequestException;
import com.careerpilot.modules.jobmatch.dto.JobMatchReportDto;
import com.careerpilot.modules.jobmatch.dto.SkillMatchItemDto;
import com.careerpilot.modules.resume.entity.Resume;
import com.careerpilot.modules.resume.entity.ResumeSkill;
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
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GeminiJobMatchService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiJobMatchService.class);
    private static final String GEMINI_API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final String apiKey;
    private final String model;
    private final int timeoutSeconds;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GeminiJobMatchService(
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

    public JobMatchReportDto matchResumeWithJob(Resume resume, List<ResumeSkill> resumeSkills,
                                                String jobTitle, String companyName, String jobDescriptionText) {
        if (!StringUtils.hasText(jobDescriptionText)) {
            throw new BadRequestException("Job description text cannot be empty.");
        }

        if (!StringUtils.hasText(apiKey)) {
            logger.info("No Gemini API key configured. Executing offline heuristic job matching.");
            return generateHeuristicMatch(resume, resumeSkills, jobTitle, companyName, jobDescriptionText);
        }

        try {
            return callGeminiApi(resume, resumeSkills, jobTitle, companyName, jobDescriptionText);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Live Gemini API call failed for job matching: {}. Falling back to heuristic match.", e.getMessage());
            return generateHeuristicMatch(resume, resumeSkills, jobTitle, companyName, jobDescriptionText);
        }
    }

    private JobMatchReportDto callGeminiApi(Resume resume, List<ResumeSkill> resumeSkills,
                                            String jobTitle, String companyName, String jobDescriptionText) {
        String prompt = buildPrompt(resume, resumeSkills, jobTitle, companyName, jobDescriptionText);

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
                        String errorBody = new String(resp.getBody().readAllBytes());
                        logger.error("Gemini API Client Error (4xx): {}", errorBody);
                        throw new BadRequestException("AI Job Matching request rejected by Gemini API: " + resp.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                        String errorBody = new String(resp.getBody().readAllBytes());
                        logger.error("Gemini API Server Error (5xx): {}", errorBody);
                        throw new RuntimeException("Gemini API Service Unavailable: " + resp.getStatusCode());
                    })
                    .body(String.class);

            return parseGeminiResponse(rawResponse, resume, jobTitle, companyName);
        } catch (RestClientResponseException e) {
            logger.error("HTTP error invoking Gemini: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Gemini API invocation failed: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            logger.error("Network timeout/access error invoking Gemini API: {}", e.getMessage());
            throw new RuntimeException("Gemini API connection timed out. Using fallback.", e);
        }
    }

    private JobMatchReportDto parseGeminiResponse(String responseJson, Resume resume, String jobTitle, String companyName) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode candidates = root.path("candidates");
            if (candidates.isMissingNode() || candidates.isEmpty()) {
                throw new BadRequestException("Gemini API returned an empty candidate list.");
            }

            JsonNode content = candidates.get(0).path("content");
            JsonNode parts = content.path("parts");
            if (parts.isMissingNode() || parts.isEmpty()) {
                throw new BadRequestException("Gemini response missing content parts.");
            }

            String text = parts.get(0).path("text").asText();
            String cleanedJson = cleanJsonString(text);

            JsonNode parsed = objectMapper.readTree(cleanedJson);

            int skillsScore = parsed.path("skillsScore").asInt(70);
            int experienceScore = parsed.path("experienceScore").asInt(70);
            int matchScore = parsed.has("matchScore") ?
                    parsed.path("matchScore").asInt() :
                    (int) Math.round((skillsScore * 0.60) + (experienceScore * 0.40));

            // Constrain scores between 0 and 100
            skillsScore = Math.max(0, Math.min(100, skillsScore));
            experienceScore = Math.max(0, Math.min(100, experienceScore));
            matchScore = Math.max(0, Math.min(100, matchScore));

            String matchSummary = parsed.path("matchSummary").asText(
                    "Candidate shows moderate alignment with the key requirements of the role."
            );

            List<String> matchedSkills = new ArrayList<>();
            JsonNode matchedNode = parsed.path("matchedSkills");
            if (matchedNode.isArray()) {
                for (JsonNode m : matchedNode) {
                    if (m.isTextual() && StringUtils.hasText(m.asText())) {
                        matchedSkills.add(m.asText().trim());
                    }
                }
            }

            List<SkillMatchItemDto> missingSkills = new ArrayList<>();
            JsonNode missingNode = parsed.path("missingSkills");
            if (missingNode.isArray()) {
                for (JsonNode item : missingNode) {
                    String name = item.path("skillName").asText("").trim();
                    if (!StringUtils.hasText(name)) continue;
                    String category = item.path("category").asText("TECHNICAL");
                    String importance = item.path("importance").asText("REQUIRED");
                    String recommendation = item.path("recommendation").asText("Study core principles and complete a reference project.");
                    missingSkills.add(new SkillMatchItemDto(name, category, importance, "MISSING", recommendation));
                }
            }

            List<String> recommendations = new ArrayList<>();
            JsonNode recNode = parsed.path("recommendations");
            if (recNode.isArray()) {
                for (JsonNode r : recNode) {
                    if (r.isTextual() && StringUtils.hasText(r.asText())) {
                        recommendations.add(r.asText().trim());
                    }
                }
            }

            return new JobMatchReportDto(
                    null,
                    resume.getId(),
                    resume.getFileName(),
                    null,
                    jobTitle,
                    companyName,
                    matchScore,
                    skillsScore,
                    experienceScore,
                    matchSummary,
                    matchedSkills,
                    missingSkills,
                    recommendations,
                    Instant.now()
            );

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse Gemini JSON payload: {}", e.getMessage());
            throw new RuntimeException("AI response could not be parsed as valid JSON.", e);
        }
    }

    private String buildPrompt(Resume resume, List<ResumeSkill> resumeSkills,
                               String jobTitle, String companyName, String jobDescriptionText) {
        String existingSkills = resumeSkills != null && !resumeSkills.isEmpty()
                ? resumeSkills.stream().map(ResumeSkill::getSkillName).collect(Collectors.joining(", "))
                : "No categorized skills extracted";

        String resumeTextSnippet = resume.getRawText();
        if (resumeTextSnippet != null && resumeTextSnippet.length() > 3000) {
            resumeTextSnippet = resumeTextSnippet.substring(0, 3000);
        }

        return String.format("""
            You are CareerPilot AI's Senior Technical Hiring Assessor and Job Matching Engine.
            Evaluate how closely the candidate's resume matches the target job description.

            ### TARGET JOB:
            Title: %s
            Company: %s
            Description:
            <JOB_DESCRIPTION>
            %s
            </JOB_DESCRIPTION>

            ### CANDIDATE RESUME:
            Identified Resume Skills: %s
            Resume Text:
            <RESUME_TEXT>
            %s
            </RESUME_TEXT>

            ### RULES:
            1. Zero Hallucination: Compare strictly the provided job description and candidate resume.
            2. matchedSkills: List technical & professional skills explicitly present in both.
            3. missingSkills: List skills required or preferred by the job description that the resume does NOT possess.
            4. skillsScore (0-100): Weighted calculation based on proportion of required skills matched.
            5. experienceScore (0-100): Alignment of candidate's past projects, domain, and experience level with the job.
            6. matchScore (0-100): Overall match calculated as (skillsScore * 0.6) + (experienceScore * 0.4).
            7. recommendations: 3 to 5 clear, actionable steps for the candidate to address the gaps.

            Return ONLY valid JSON with this exact schema:
            {
              "matchScore": 82,
              "skillsScore": 85,
              "experienceScore": 78,
              "matchSummary": "Candidate demonstrates strong core competencies in backend engineering...",
              "matchedSkills": ["Java", "Spring Boot", "Docker", "PostgreSQL"],
              "missingSkills": [
                {
                  "skillName": "Kubernetes",
                  "category": "DEVOPS",
                  "importance": "REQUIRED",
                  "recommendation": "Deploy a containerized Spring Boot microservice to a local Minikube cluster."
                }
              ],
              "recommendations": [
                "Highlight distributed systems design experience in project descriptions.",
                "Add Kubernetes and Helm deployment manifests to GitHub portfolio."
              ]
            }
            """,
                jobTitle != null ? jobTitle : "Software Engineer",
                companyName != null ? companyName : "Target Company",
                jobDescriptionText,
                existingSkills,
                resumeTextSnippet != null ? resumeTextSnippet : ""
        );
    }

    public JobMatchReportDto generateHeuristicMatch(Resume resume, List<ResumeSkill> resumeSkills,
                                                    String jobTitle, String companyName, String jobDescriptionText) {
        String lowerJd = (jobTitle + " " + jobDescriptionText).toLowerCase();

        Set<String> resumeSkillSet = new HashSet<>();
        if (resumeSkills != null) {
            for (ResumeSkill s : resumeSkills) {
                if (StringUtils.hasText(s.getSkillName())) {
                    resumeSkillSet.add(s.getSkillName().trim().toLowerCase());
                }
            }
        }

        // If resumeSkills table is empty, parse common skills from resume rawText
        if (resumeSkillSet.isEmpty() && StringUtils.hasText(resume.getRawText())) {
            String lowerResume = resume.getRawText().toLowerCase();
            for (String kw : KNOWN_KEYWORDS) {
                if (containsWord(lowerResume, kw.toLowerCase())) {
                    resumeSkillSet.add(kw.toLowerCase());
                }
            }
        }

        List<String> matchedSkills = new ArrayList<>();
        List<SkillMatchItemDto> missingSkills = new ArrayList<>();

        // Scan known tech keywords in JD
        for (String kw : KNOWN_KEYWORDS) {
            String kwLower = kw.toLowerCase();
            if (containsWord(lowerJd, kwLower)) {
                if (resumeSkillSet.contains(kwLower)) {
                    matchedSkills.add(kw);
                } else {
                    boolean isRequired = lowerJd.contains("required") || lowerJd.contains("must have") ||
                            lowerJd.contains("essential") || lowerJd.contains("qualifications");
                    String importance = isRequired ? "REQUIRED" : "PREFERRED";
                    String recommendation = "Build hands-on proficiency in " + kw + " through project-based practice.";
                    missingSkills.add(new SkillMatchItemDto(kw, "TECHNICAL", importance, "MISSING", recommendation));
                }
            }
        }

        int totalIdentified = matchedSkills.size() + missingSkills.size();
        int skillsScore = totalIdentified > 0 ? (int) Math.round(((double) matchedSkills.size() / totalIdentified) * 100) : 60;
        int experienceScore = Math.min(100, Math.max(45, skillsScore - 5));
        int matchScore = (int) Math.round((skillsScore * 0.60) + (experienceScore * 0.40));

        String matchSummary = String.format(
                "Candidate resume demonstrates %d matching skills out of %d key competencies identified in the job description for %s.",
                matchedSkills.size(), totalIdentified, jobTitle
        );

        List<String> recommendations = new ArrayList<>();
        if (!missingSkills.isEmpty()) {
            recommendations.add("Address the top missing required skills (" +
                    missingSkills.stream().limit(3).map(SkillMatchItemDto::getSkillName).collect(Collectors.joining(", ")) +
                    ") in your upcoming technical interview preparation.");
        }
        recommendations.add("Tailor your resume bullet points to emphasize impact metrics aligned with " + jobTitle + " responsibilities.");
        recommendations.add("Ensure your GitHub projects explicitly demonstrate your listed competencies in " +
                (matchedSkills.isEmpty() ? "your core stack" : String.join(", ", matchedSkills.stream().limit(3).toList())) + ".");

        return new JobMatchReportDto(
                null,
                resume.getId(),
                resume.getFileName(),
                null,
                jobTitle,
                companyName,
                matchScore,
                skillsScore,
                experienceScore,
                matchSummary,
                matchedSkills,
                missingSkills,
                recommendations,
                Instant.now()
        );
    }

    private boolean containsWord(String text, String word) {
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(text).find();
    }

    private String cleanJsonString(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "{}";
        }
        String cleaned = raw.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private static final List<String> KNOWN_KEYWORDS = List.of(
            "Java", "Spring Boot", "Spring Security", "Python", "JavaScript", "TypeScript", "React",
            "Node.js", "Docker", "Kubernetes", "PostgreSQL", "MySQL", "MongoDB", "Redis",
            "Kafka", "RabbitMQ", "AWS", "Azure", "GCP", "GraphQL", "REST API", "Microservices",
            "CI/CD", "Git", "GitHub Actions", "Terraform", "Linux", "JUnit", "Mockito", "Tailwind CSS",
            "Elasticsearch", "Next.js", "Go", "Rust", "C++", "C#"
    );
}

