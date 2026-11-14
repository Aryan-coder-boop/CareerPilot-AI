package com.careerpilot.jobmatch;

import com.careerpilot.common.exception.ResourceNotFoundException;
import com.careerpilot.common.exception.UnauthorizedException;
import com.careerpilot.modules.auth.entity.Role;
import com.careerpilot.modules.auth.entity.User;
import com.careerpilot.modules.auth.repository.UserRepository;
import com.careerpilot.modules.jobmatch.dto.JobMatchReportDto;
import com.careerpilot.modules.jobmatch.dto.JobMatchRequestDto;
import com.careerpilot.modules.jobmatch.dto.JobMatchSummaryDto;
import com.careerpilot.modules.jobmatch.entity.JobDescription;
import com.careerpilot.modules.jobmatch.entity.JobMatchReport;
import com.careerpilot.modules.jobmatch.repository.JobDescriptionRepository;
import com.careerpilot.modules.jobmatch.repository.JobMatchReportRepository;
import com.careerpilot.modules.jobmatch.service.GeminiJobMatchService;
import com.careerpilot.modules.jobmatch.service.JobMatchService;
import com.careerpilot.modules.resume.entity.Resume;
import com.careerpilot.modules.resume.entity.ResumeSkill;
import com.careerpilot.modules.resume.repository.ResumeRepository;
import com.careerpilot.modules.resume.repository.ResumeSkillRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobMatchServiceTest {

    @Mock
    private JobDescriptionRepository jobDescriptionRepository;

    @Mock
    private JobMatchReportRepository jobMatchReportRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ResumeSkillRepository resumeSkillRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GeminiJobMatchService geminiJobMatchService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private JobMatchService jobMatchService;

    private User user;
    private Resume resume;
    private UUID userId;
    private UUID resumeId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        resumeId = UUID.randomUUID();

        user = new User("jane@example.com", "hash", "Jane Doe", Role.ROLE_USER);
        user.setId(userId);

        resume = new Resume(user, "resume.pdf", "/uploads/resume.pdf");
        resume.setId(resumeId);
    }

    @Test
    @DisplayName("Should successfully match resume with job description")
    void testMatchJobWithResumeSuccess() {
        JobMatchRequestDto request = new JobMatchRequestDto(
                resumeId,
                "Backend Engineer",
                "Google",
                "Looking for a backend engineer with Java and Cloud experience."
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));
        when(resumeSkillRepository.findByResumeId(resumeId)).thenReturn(Collections.emptyList());

        JobDescription savedJob = new JobDescription(user, request.getJobTitle(), request.getCompanyName(), request.getJobDescriptionText());
        savedJob.setId(UUID.randomUUID());
        when(jobDescriptionRepository.save(any(JobDescription.class))).thenReturn(savedJob);

        JobMatchReportDto aiDto = new JobMatchReportDto(
                null, resumeId, "resume.pdf", savedJob.getId(),
                "Backend Engineer", "Google", 85, 90, 80,
                "Strong candidate fit", List.of("Java"), Collections.emptyList(),
                List.of("Apply now"), Instant.now()
        );
        when(geminiJobMatchService.matchResumeWithJob(any(), any(), any(), any(), any())).thenReturn(aiDto);

        JobMatchReport savedReport = new JobMatchReport(
                user, resume, savedJob, 85, 90, 80,
                "Strong candidate fit", "[\"Java\"]", "[]", "[\"Apply now\"]"
        );
        savedReport.setId(UUID.randomUUID());
        when(jobMatchReportRepository.save(any(JobMatchReport.class))).thenReturn(savedReport);

        JobMatchReportDto result = jobMatchService.matchJobWithResume(userId, request);

        assertNotNull(result);
        assertEquals(85, result.getMatchScore());
        assertEquals("Backend Engineer", result.getJobTitle());
        assertEquals("resume.pdf", result.getResumeFileName());
        verify(jobMatchReportRepository, times(1)).save(any(JobMatchReport.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when resume does not belong to user")
    void testMatchJobUnauthorizedResume() {
        UUID otherUserId = UUID.randomUUID();
        User otherUser = new User("other@example.com", "hash", "Other", Role.ROLE_USER);
        otherUser.setId(otherUserId);
        resume.setUser(otherUser);

        JobMatchRequestDto request = new JobMatchRequestDto(
                resumeId, "Title", "Company", "Description text must be long enough."
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(resumeRepository.findById(resumeId)).thenReturn(Optional.of(resume));

        assertThrows(UnauthorizedException.class, () -> jobMatchService.matchJobWithResume(userId, request));
        verify(jobDescriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve matches by user")
    void testGetMatchesByUser() {
        JobDescription job = new JobDescription(user, "Java Lead", "Meta", "Role description");
        job.setId(UUID.randomUUID());

        JobMatchReport report = new JobMatchReport(
                user, resume, job, 90, 95, 85,
                "Excellent match", "[]", "[]", "[]"
        );
        report.setId(UUID.randomUUID());

        when(jobMatchReportRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(report));

        List<JobMatchSummaryDto> results = jobMatchService.getMatchesByUser(userId);

        assertEquals(1, results.size());
        assertEquals("Java Lead", results.get(0).getJobTitle());
        assertEquals(90, results.get(0).getMatchScore());
    }
}
