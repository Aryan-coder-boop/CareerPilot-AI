package com.careerpilot.modules.jobmatch.service;

import com.careerpilot.common.exception.BadRequestException;
import com.careerpilot.common.exception.ResourceNotFoundException;
import com.careerpilot.common.exception.UnauthorizedException;
import com.careerpilot.modules.auth.entity.User;
import com.careerpilot.modules.auth.repository.UserRepository;
import com.careerpilot.modules.jobmatch.dto.*;
import com.careerpilot.modules.jobmatch.entity.JobDescription;
import com.careerpilot.modules.jobmatch.entity.JobMatchReport;
import com.careerpilot.modules.jobmatch.repository.JobDescriptionRepository;
import com.careerpilot.modules.jobmatch.repository.JobMatchReportRepository;
import com.careerpilot.modules.resume.entity.Resume;
import com.careerpilot.modules.resume.entity.ResumeSkill;
import com.careerpilot.modules.resume.repository.ResumeRepository;
import com.careerpilot.modules.resume.repository.ResumeSkillRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JobMatchService {

    private static final Logger logger = LoggerFactory.getLogger(JobMatchService.class);

    private final JobDescriptionRepository jobDescriptionRepository;
    private final JobMatchReportRepository jobMatchReportRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final UserRepository userRepository;
    private final GeminiJobMatchService geminiJobMatchService;
    private final ObjectMapper objectMapper;

    public JobMatchService(
            JobDescriptionRepository jobDescriptionRepository,
            JobMatchReportRepository jobMatchReportRepository,
            ResumeRepository resumeRepository,
            ResumeSkillRepository resumeSkillRepository,
            UserRepository userRepository,
            GeminiJobMatchService geminiJobMatchService,
            ObjectMapper objectMapper) {
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.jobMatchReportRepository = jobMatchReportRepository;
        this.resumeRepository = resumeRepository;
        this.resumeSkillRepository = resumeSkillRepository;
        this.userRepository = userRepository;
        this.geminiJobMatchService = geminiJobMatchService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public JobMatchReportDto matchJobWithResume(UUID userId, JobMatchRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated or not found"));

        Resume resume = resumeRepository.findById(requestDto.getResumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with ID: " + requestDto.getResumeId()));

        if (!resume.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to access this resume.");
        }

        // 1. Save Job Description
        JobDescription jobDescription = new JobDescription(
                user,
                requestDto.getJobTitle().trim(),
                requestDto.getCompanyName() != null ? requestDto.getCompanyName().trim() : null,
                requestDto.getJobDescriptionText().trim()
        );
        jobDescription = jobDescriptionRepository.save(jobDescription);

        // 2. Fetch associated skills
        List<ResumeSkill> resumeSkills = resumeSkillRepository.findByResumeId(resume.getId());

        // 3. Perform AI Matching Analysis
        JobMatchReportDto analysisResult = geminiJobMatchService.matchResumeWithJob(
                resume,
                resumeSkills,
                jobDescription.getJobTitle(),
                jobDescription.getCompanyName(),
                jobDescription.getRawText()
        );

        // 4. Serialize JSON fields for persistent storage
        String matchedSkillsJson = serializeToJson(analysisResult.getMatchedSkills());
        String missingSkillsJson = serializeToJson(analysisResult.getMissingSkills());
        String recommendationsJson = serializeToJson(analysisResult.getRecommendations());

        // 5. Save JobMatchReport
        JobMatchReport report = new JobMatchReport(
                user,
                resume,
                jobDescription,
                analysisResult.getMatchScore(),
                analysisResult.getSkillsScore(),
                analysisResult.getExperienceScore(),
                analysisResult.getMatchSummary(),
                matchedSkillsJson,
                missingSkillsJson,
                recommendationsJson
        );
        report = jobMatchReportRepository.save(report);

        // 6. Return comprehensive DTO with generated IDs
        analysisResult.setId(report.getId());
        analysisResult.setJobId(jobDescription.getId());
        analysisResult.setResumeId(resume.getId());
        analysisResult.setResumeFileName(resume.getFileName());
        analysisResult.setCreatedAt(report.getCreatedAt());

        return analysisResult;
    }

    @Transactional(readOnly = true)
    public List<JobMatchSummaryDto> getMatchesByUser(UUID userId) {
        List<JobMatchReport> reports = jobMatchReportRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return reports.stream().map(this::toSummaryDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobMatchReportDto getMatchReportById(UUID reportId, UUID userId) {
        JobMatchReport report = jobMatchReportRepository.findByIdAndUserId(reportId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Job match report not found with ID: " + reportId));

        return toFullDto(report);
    }

    @Transactional(readOnly = true)
    public List<JobMatchSummaryDto> getMatchesByResume(UUID resumeId, UUID userId) {
        List<JobMatchReport> reports = jobMatchReportRepository.findByResumeIdAndUserId(resumeId, userId);
        return reports.stream().map(this::toSummaryDto).collect(Collectors.toList());
    }

    private JobMatchSummaryDto toSummaryDto(JobMatchReport report) {
        return new JobMatchSummaryDto(
                report.getId(),
                report.getJobDescription().getId(),
                report.getJobDescription().getJobTitle(),
                report.getJobDescription().getCompanyName(),
                report.getResume().getId(),
                report.getResume().getFileName(),
                report.getMatchScore(),
                report.getCreatedAt()
        );
    }

    private JobMatchReportDto toFullDto(JobMatchReport report) {
        List<String> matchedSkills = deserializeList(report.getMatchedSkills(), new TypeReference<List<String>>() {});
        List<SkillMatchItemDto> missingSkills = deserializeList(report.getMissingSkills(), new TypeReference<List<SkillMatchItemDto>>() {});
        List<String> recommendations = deserializeList(report.getRecommendations(), new TypeReference<List<String>>() {});

        return new JobMatchReportDto(
                report.getId(),
                report.getResume().getId(),
                report.getResume().getFileName(),
                report.getJobDescription().getId(),
                report.getJobDescription().getJobTitle(),
                report.getJobDescription().getCompanyName(),
                report.getMatchScore(),
                report.getSkillsScore(),
                report.getExperienceScore(),
                report.getMatchSummary(),
                matchedSkills,
                missingSkills,
                recommendations,
                report.getCreatedAt()
        );
    }

    private String serializeToJson(Object obj) {
        if (obj == null) return "[]";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize to JSON: {}", e.getMessage());
            return "[]";
        }
    }

    private <T> List<T> deserializeList(String json, TypeReference<List<T>> typeRef) {
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            logger.warn("Failed to deserialize JSON: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}

