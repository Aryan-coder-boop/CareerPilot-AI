package com.careerpilot.resume;

import com.careerpilot.common.exception.BadRequestException;
import com.careerpilot.common.exception.ResourceNotFoundException;
import com.careerpilot.modules.auth.entity.Role;
import com.careerpilot.modules.auth.entity.User;
import com.careerpilot.modules.auth.repository.UserRepository;
import com.careerpilot.modules.resume.dto.ParsedResumeAnalysisDto;
import com.careerpilot.modules.resume.dto.ResumeDetailResponseDto;
import com.careerpilot.modules.resume.dto.ResumeSummaryDto;
import com.careerpilot.modules.resume.dto.ScoreBreakdownDto;
import com.careerpilot.modules.resume.entity.ProcessingStatus;
import com.careerpilot.modules.resume.entity.Resume;
import com.careerpilot.modules.resume.repository.ResumeRepository;
import com.careerpilot.modules.resume.repository.ResumeSkillRepository;
import com.careerpilot.modules.resume.service.FileStorageService;
import com.careerpilot.modules.resume.service.GeminiResumeAnalyzerService;
import com.careerpilot.modules.resume.service.PdfTextExtractorService;
import com.careerpilot.modules.resume.service.ResumeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ResumeSkillRepository resumeSkillRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PdfTextExtractorService pdfTextExtractorService;

    @Mock
    private GeminiResumeAnalyzerService geminiResumeAnalyzerService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private ResumeService resumeService;

    private User sampleUser;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        resumeService = new ResumeService(
                resumeRepository,
                resumeSkillRepository,
                userRepository,
                fileStorageService,
                pdfTextExtractorService,
                geminiResumeAnalyzerService,
                objectMapper,
                5, // max 5 per window
                10
        );

        sampleUser = new User("dev@careerpilot.ai", "hash", "Test Developer", Role.ROLE_USER);
        sampleUser.setId(userId);
    }

    @Test
    @DisplayName("Should reject upload when user exceeds rate limit window")
    void testUploadRateLimitExceeded() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D});

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(resumeRepository.countByUserIdAndCreatedAtAfter(eq(userId), any(Instant.class))).thenReturn(5L);

        assertThrows(BadRequestException.class, () -> resumeService.uploadAndAnalyzeResume(file, userId));
        verify(fileStorageService, never()).storeFile(any(), any());
    }

    @Test
    @DisplayName("Should successfully upload, extract, analyze and persist resume")
    void testUploadAndAnalyzeSuccess() {
        byte[] pdfBytes = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};
        MockMultipartFile file = new MockMultipartFile("file", "cv.pdf", "application/pdf", pdfBytes);

        ParsedResumeAnalysisDto mockAnalysis = new ParsedResumeAnalysisDto(
                "Jane Dev", "jane@careerpilot.ai", "555-0199", "Seattle", "Summary",
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                new ScoreBreakdownDto(20, 20, 20, 20, 80), 80, List.of("Strong stack"), List.of(),
                ParsedResumeAnalysisDto.DEFAULT_DISCLAIMER
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(resumeRepository.countByUserIdAndCreatedAtAfter(eq(userId), any(Instant.class))).thenReturn(0L);
        when(pdfTextExtractorService.extractText(file)).thenReturn("Extracted resume text with plenty of words for testing.");
        when(fileStorageService.storeFile(eq(file), any(UUID.class))).thenReturn("/uploads/resume.pdf");
        when(geminiResumeAnalyzerService.analyzeResume(anyString())).thenReturn(mockAnalysis);
        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResumeDetailResponseDto response = resumeService.uploadAndAnalyzeResume(file, userId);

        assertNotNull(response);
        assertEquals("cv.pdf", response.fileName());
        assertEquals(80, response.qualityScore());
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    @DisplayName("Should reject access when requesting a resume belonging to another user")
    void testUserIsolationOnGetResumeDetail() {
        UUID resumeId = UUID.randomUUID();
        when(resumeRepository.findByIdAndUserId(resumeId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> resumeService.getResumeDetail(resumeId, userId));
    }

    @Test
    @DisplayName("Should delete both disk file and database record on resume deletion")
    void testDeleteResumeSuccess() {
        UUID resumeId = UUID.randomUUID();
        Resume resume = new Resume(sampleUser, "cv.pdf", "/uploads/resume_123.pdf");
        resume.setId(resumeId);

        when(resumeRepository.findByIdAndUserId(resumeId, userId)).thenReturn(Optional.of(resume));

        resumeService.deleteResume(resumeId, userId);

        verify(fileStorageService).deleteFile("/uploads/resume_123.pdf");
        verify(resumeRepository).delete(resume);
    }
}

