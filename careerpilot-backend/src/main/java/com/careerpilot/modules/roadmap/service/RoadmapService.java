package com.careerpilot.modules.roadmap.service;

import com.careerpilot.common.exception.ResourceNotFoundException;
import com.careerpilot.common.exception.UnauthorizedException;
import com.careerpilot.modules.auth.entity.User;
import com.careerpilot.modules.auth.repository.UserRepository;
import com.careerpilot.modules.jobmatch.dto.SkillMatchItemDto;
import com.careerpilot.modules.jobmatch.entity.JobDescription;
import com.careerpilot.modules.jobmatch.entity.JobMatchReport;
import com.careerpilot.modules.jobmatch.repository.JobDescriptionRepository;
import com.careerpilot.modules.jobmatch.repository.JobMatchReportRepository;
import com.careerpilot.modules.roadmap.dto.*;
import com.careerpilot.modules.roadmap.entity.LearningRoadmap;
import com.careerpilot.modules.roadmap.entity.RoadmapMilestone;
import com.careerpilot.modules.roadmap.entity.RoadmapResource;
import com.careerpilot.modules.roadmap.repository.LearningRoadmapRepository;
import com.careerpilot.modules.roadmap.repository.RoadmapMilestoneRepository;
import com.careerpilot.modules.roadmap.repository.RoadmapResourceRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoadmapService {

    private static final Logger logger = LoggerFactory.getLogger(RoadmapService.class);

    private final LearningRoadmapRepository roadmapRepository;
    private final RoadmapMilestoneRepository milestoneRepository;
    private final RoadmapResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final JobMatchReportRepository jobMatchReportRepository;
    private final GeminiRoadmapService geminiRoadmapService;
    private final ObjectMapper objectMapper;

    public RoadmapService(
            LearningRoadmapRepository roadmapRepository,
            RoadmapMilestoneRepository milestoneRepository,
            RoadmapResourceRepository resourceRepository,
            UserRepository userRepository,
            JobDescriptionRepository jobDescriptionRepository,
            JobMatchReportRepository jobMatchReportRepository,
            GeminiRoadmapService geminiRoadmapService,
            ObjectMapper objectMapper) {
        this.roadmapRepository = roadmapRepository;
        this.milestoneRepository = milestoneRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.jobMatchReportRepository = jobMatchReportRepository;
        this.geminiRoadmapService = geminiRoadmapService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RoadmapDetailDto generateRoadmap(UUID userId, RoadmapGenerateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        JobDescription job = null;
        if (request.getJobId() != null) {
            job = jobDescriptionRepository.findByIdAndUserId(request.getJobId(), userId).orElse(null);
        }

        List<String> missingSkills = new ArrayList<>();
        if (request.getMatchReportId() != null) {
            JobMatchReport report = jobMatchReportRepository.findByIdAndUserId(request.getMatchReportId(), userId).orElse(null);
            if (report != null && report.getMissingSkills() != null) {
                try {
                    List<SkillMatchItemDto> items = objectMapper.readValue(report.getMissingSkills(), new TypeReference<>() {});
                    missingSkills = items.stream().map(SkillMatchItemDto::getSkillName).toList();
                } catch (Exception ignored) {}
            }
        }

        int weeks = request.getTargetWeeks() > 0 ? request.getTargetWeeks() : 6;
        String jobTitle = job != null ? job.getJobTitle() : request.getTargetRole();

        List<GeminiRoadmapService.PlannedMilestone> planned =
                geminiRoadmapService.generateRoadmapPlan(request.getTargetRole(), weeks, missingSkills, jobTitle);

        LearningRoadmap roadmap = new LearningRoadmap(user, job, request.getTargetRole(), planned.size());
        roadmap = roadmapRepository.save(roadmap);

        for (int i = 0; i < planned.size(); i++) {
            GeminiRoadmapService.PlannedMilestone pm = planned.get(i);
            RoadmapMilestone milestone = new RoadmapMilestone(
                    roadmap, pm.weekNumber(), pm.title(), pm.description(), i + 1
            );
            roadmap.addMilestone(milestone);

            if (pm.resources() != null) {
                for (GeminiRoadmapService.PlannedResource pr : pm.resources()) {
                    RoadmapResource resource = new RoadmapResource(milestone, pr.title(), pr.url(), pr.resourceType());
                    milestone.addResource(resource);
                }
            }
        }

        roadmap = roadmapRepository.save(roadmap);
        return toDetailDto(roadmap);
    }

    @Transactional(readOnly = true)
    public List<RoadmapSummaryDto> getUserRoadmaps(UUID userId) {
        List<LearningRoadmap> roadmaps = roadmapRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return roadmaps.stream().map(this::toSummaryDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoadmapDetailDto getRoadmapDetail(UUID roadmapId, UUID userId) {
        LearningRoadmap roadmap = roadmapRepository.findByIdAndUserId(roadmapId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning roadmap not found with ID: " + roadmapId));
        return toDetailDto(roadmap);
    }

    @Transactional
    public RoadmapMilestoneDto updateMilestoneStatus(UUID milestoneId, UUID userId, String newStatus) {
        RoadmapMilestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with ID: " + milestoneId));

        if (!milestone.getRoadmap().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized to modify this roadmap milestone");
        }

        milestone.setStatus(newStatus.toUpperCase());
        milestone = milestoneRepository.save(milestone);

        // Check if all milestones completed
        LearningRoadmap roadmap = milestone.getRoadmap();
        List<RoadmapMilestone> allMilestones = milestoneRepository.findByRoadmapIdOrderByWeekNumberAscOrderIndexAsc(roadmap.getId());
        boolean allDone = allMilestones.stream().allMatch(m -> "COMPLETED".equalsIgnoreCase(m.getStatus()));
        if (allDone) {
            roadmap.setStatus("COMPLETED");
            roadmapRepository.save(roadmap);
        } else if ("COMPLETED".equalsIgnoreCase(roadmap.getStatus())) {
            roadmap.setStatus("ACTIVE");
            roadmapRepository.save(roadmap);
        }

        return toMilestoneDto(milestone);
    }

    private RoadmapSummaryDto toSummaryDto(LearningRoadmap roadmap) {
        List<RoadmapMilestone> milestones = milestoneRepository.findByRoadmapIdOrderByWeekNumberAscOrderIndexAsc(roadmap.getId());
        long completed = milestones.stream().filter(m -> "COMPLETED".equalsIgnoreCase(m.getStatus())).count();
        int progress = milestones.isEmpty() ? 0 : (int) Math.round(((double) completed / milestones.size()) * 100);

        return new RoadmapSummaryDto(
                roadmap.getId(),
                roadmap.getTargetRole(),
                roadmap.getEstimatedWeeks(),
                roadmap.getStatus(),
                milestones.size(),
                (int) completed,
                progress,
                roadmap.getCreatedAt()
        );
    }

    private RoadmapDetailDto toDetailDto(LearningRoadmap roadmap) {
        List<RoadmapMilestone> milestones = milestoneRepository.findByRoadmapIdOrderByWeekNumberAscOrderIndexAsc(roadmap.getId());
        long completed = milestones.stream().filter(m -> "COMPLETED".equalsIgnoreCase(m.getStatus())).count();
        int progress = milestones.isEmpty() ? 0 : (int) Math.round(((double) completed / milestones.size()) * 100);

        List<RoadmapMilestoneDto> dtos = milestones.stream().map(this::toMilestoneDto).collect(Collectors.toList());

        return new RoadmapDetailDto(
                roadmap.getId(),
                roadmap.getTargetRole(),
                roadmap.getEstimatedWeeks(),
                roadmap.getStatus(),
                progress,
                roadmap.getCreatedAt(),
                dtos
        );
    }

    private RoadmapMilestoneDto toMilestoneDto(RoadmapMilestone m) {
        List<RoadmapResource> resources = resourceRepository.findByMilestoneId(m.getId());
        List<RoadmapResourceDto> resDtos = resources.stream()
                .map(r -> new RoadmapResourceDto(r.getId(), r.getTitle(), r.getUrl(), r.getResourceType()))
                .collect(Collectors.toList());

        return new RoadmapMilestoneDto(
                m.getId(),
                m.getWeekNumber(),
                m.getTitle(),
                m.getDescription(),
                m.getStatus(),
                m.getOrderIndex(),
                resDtos
        );
    }
}

