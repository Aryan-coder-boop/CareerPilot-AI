package com.careerpilot.roadmap;

import com.careerpilot.modules.auth.entity.Role;
import com.careerpilot.modules.auth.entity.User;
import com.careerpilot.modules.auth.repository.UserRepository;
import com.careerpilot.modules.jobmatch.repository.JobDescriptionRepository;
import com.careerpilot.modules.jobmatch.repository.JobMatchReportRepository;
import com.careerpilot.modules.roadmap.dto.*;
import com.careerpilot.modules.roadmap.entity.LearningRoadmap;
import com.careerpilot.modules.roadmap.entity.RoadmapMilestone;
import com.careerpilot.modules.roadmap.repository.LearningRoadmapRepository;
import com.careerpilot.modules.roadmap.repository.RoadmapMilestoneRepository;
import com.careerpilot.modules.roadmap.repository.RoadmapResourceRepository;
import com.careerpilot.modules.roadmap.service.GeminiRoadmapService;
import com.careerpilot.modules.roadmap.service.RoadmapService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoadmapServiceTest {

    @Mock
    private LearningRoadmapRepository roadmapRepository;

    @Mock
    private RoadmapMilestoneRepository milestoneRepository;

    @Mock
    private RoadmapResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobDescriptionRepository jobDescriptionRepository;

    @Mock
    private JobMatchReportRepository jobMatchReportRepository;

    @Mock
    private GeminiRoadmapService geminiRoadmapService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RoadmapService roadmapService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User("candidate@example.com", "hash", "Bob Smith", Role.ROLE_USER);
        user.setId(userId);
    }

    @Test
    @DisplayName("Should successfully generate personalized roadmap")
    void testGenerateRoadmapSuccess() {
        RoadmapGenerateRequestDto request = new RoadmapGenerateRequestDto("Full Stack Engineer", null, null, 4);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        List<GeminiRoadmapService.PlannedMilestone> planned = List.of(
                new GeminiRoadmapService.PlannedMilestone(1, "React 18", "Learn concurrent React", Collections.emptyList()),
                new GeminiRoadmapService.PlannedMilestone(2, "Spring Boot", "Build REST APIs", Collections.emptyList())
        );
        when(geminiRoadmapService.generateRoadmapPlan(eq("Full Stack Engineer"), eq(4), any(), any()))
                .thenReturn(planned);

        LearningRoadmap saved = new LearningRoadmap(user, null, "Full Stack Engineer", 2);
        saved.setId(UUID.randomUUID());
        when(roadmapRepository.save(any(LearningRoadmap.class))).thenReturn(saved);

        RoadmapDetailDto result = roadmapService.generateRoadmap(userId, request);

        assertNotNull(result);
        assertEquals("Full Stack Engineer", result.getTargetRole());
        verify(roadmapRepository, atLeastOnce()).save(any(LearningRoadmap.class));
    }

    @Test
    @DisplayName("Should update milestone status and mark roadmap completed when all milestones are done")
    void testUpdateMilestoneStatus() {
        UUID roadmapId = UUID.randomUUID();
        LearningRoadmap roadmap = new LearningRoadmap(user, null, "Lead Dev", 1);
        roadmap.setId(roadmapId);

        UUID milestoneId = UUID.randomUUID();
        RoadmapMilestone milestone = new RoadmapMilestone(roadmap, 1, "Week 1", "Tasks", 1);
        milestone.setId(milestoneId);

        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(milestone));
        when(milestoneRepository.save(any(RoadmapMilestone.class))).thenReturn(milestone);
        when(milestoneRepository.findByRoadmapIdOrderByWeekNumberAscOrderIndexAsc(roadmapId)).thenReturn(List.of(milestone));

        RoadmapMilestoneDto updated = roadmapService.updateMilestoneStatus(milestoneId, userId, "COMPLETED");

        assertNotNull(updated);
        assertEquals("COMPLETED", milestone.getStatus());
        assertEquals("COMPLETED", roadmap.getStatus());
        verify(roadmapRepository, times(1)).save(roadmap);
    }
}

