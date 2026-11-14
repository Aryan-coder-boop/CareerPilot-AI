package com.careerpilot.modules.resume.service;

import com.careerpilot.common.exception.BadRequestException;
import com.careerpilot.common.exception.ResourceNotFoundException;
import com.careerpilot.common.utils.PdfValidator;
import com.careerpilot.modules.auth.entity.User;
import com.careerpilot.modules.auth.repository.UserRepository;
import com.careerpilot.modules.resume.dto.ParsedResumeAnalysisDto;
import com.careerpilot.modules.resume.dto.ResumeDetailResponseDto;
import com.careerpilot.modules.resume.dto.ResumeSummaryDto;
import com.careerpilot.modules.resume.dto.SkillItemDto;
import com.careerpilot.modules.resume.entity.ProcessingStatus;
import com.careerpilot.modules.resume.entity.Resume;
import com.careerpilot.modules.resume.entity.ResumeSkill;
import com.careerpilot.modules.resume.repository.ResumeRepository;
import com.careerpilot.modules.resume.repository.ResumeSkillRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ResumeService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeService.class);

    private final ResumeRepository resumeRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PdfTextExtractorService pdfTextExtractorService;
    private final GeminiResumeAnalyzerService geminiResumeAnalyzerService;
    private final ObjectMapper objectMapper;

    private final int maxResumesPerWindow;
    private final int windowMinutes;

    public ResumeService(
            ResumeRepository resumeRepository,
            ResumeSkillRepository resumeSkillRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService,
            PdfTextExtractorService pdfTextExtractorService,
            GeminiResumeAnalyzerService geminiResumeAnalyzerService,
            ObjectMapper objectMapper,
            @Value("${careerpilot.rate-limit.max-resumes-per-window:5}") int maxResumesPerWindow,
            @Value("${careerpilot.rate-limit.window-minutes:10}") int windowMinutes) {
        this.resumeRepository = resumeRepository;
        this.resumeSkillRepository = resumeSkillRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.pdfTextExtractorService = pdfTextExtractorService;
        this.geminiResumeAnalyzerService = geminiResumeAnalyzerService;
        this.objectMapper = objectMapper;
        this.maxResumesPerWindow = maxResumesPerWindow;
        this.windowMinutes = windowMinutes;
    }

    @Transactional
    public ResumeDetailResponseDto uploadAndAnalyzeResume(MultipartFile file, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 1. Rate Limiting Check
        Instant windowStart = Instant.now().minus(Duration.ofMinutes(this.windowMinutes));
        long recentUploadCount = resumeRepository.countByUserIdAndCreatedAtAfter(userId, windowStart);
        if (recentUploadCount >= this.maxResumesPerWindow) {
            throw new BadRequestException(
                    String.format("Rate limit exceeded: You have analyzed %d resumes in the last %d minutes. Please wait before uploading another.",
                            this.maxResumesPerWindow, this.windowMinutes)
            );
        }

        // 2. Validate PDF format and integrity
        PdfValidator.validatePdf(file);

        // 3. Extract Text via Apache PDFBox
        String extractedText = pdfTextExtractorService.extractText(file);

        // 4. Save file to isolated local disk storage
        UUID resumeId = UUID.randomUUID();
        String storedFilePath = fileStorageService.storeFile(file, resumeId);

        // 5. Analyze via Gemini AI (or heuristic fallback)
        ParsedResumeAnalysisDto analysisDto = geminiResumeAnalyzerService.analyzeResume(extractedText);

        // 6. Persist Resume entity
        Resume resume = new Resume(user, file.getOriginalFilename(), storedFilePath);
        resume.setId(resumeId);
        resume.setRawText(extractedText);
        resume.setQualityScore(analysisDto.qualityScore());
        resume.setProcessingStatus(ProcessingStatus.PROCESSED);

        try {
            resume.setParsedData(objectMapper.writeValueAsString(analysisDto));
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize ParsedResumeAnalysisDto", e);
            resume.setParsedData("{}");
        }

        Resume savedResume = resumeRepository.save(resume);

        // 7. Persist individual extracted skills
        if (analysisDto.skills() != null) {
            for (SkillItemDto skillDto : analysisDto.skills()) {
                ResumeSkill resumeSkill = new ResumeSkill(
                        savedResume,
                        skillDto.name(),
                        skillDto.category(),
                        skillDto.proficiency()
                );
                resumeSkillRepository.save(resumeSkill);
            }
        }

        String rawPreview = extractedText.length() > 300 ? extractedText.substring(0, 300) + "..." : extractedText;

        return new ResumeDetailResponseDto(
                savedResume.getId(),
                savedResume.getFileName(),
                savedResume.getQualityScore(),
                savedResume.getProcessingStatus().name(),
                savedResume.getCreatedAt(),
                analysisDto,
                rawPreview
        );
    }

    @Transactional(readOnly = true)
    public List<ResumeSummaryDto> getUserResumes(UUID userId) {
        List<Resume> resumes = resumeRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return resumes.stream()
                .map(r -> new ResumeSummaryDto(
                        r.getId(),
                        r.getFileName(),
                        r.getQualityScore(),
                        r.getProcessingStatus().name(),
                        r.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumeDetailResponseDto getResumeDetail(UUID resumeId, UUID userId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        ParsedResumeAnalysisDto analysisDto = null;
        if (resume.getParsedData() != null) {
            try {
                analysisDto = objectMapper.readValue(resume.getParsedData(), ParsedResumeAnalysisDto.class);
            } catch (JsonProcessingException e) {
                logger.error("Failed to deserialize parsedData for resume {}", resumeId, e);
            }
        }

        String rawPreview = resume.getRawText() != null
                ? (resume.getRawText().length() > 300 ? resume.getRawText().substring(0, 300) + "..." : resume.getRawText())
                : "";

        return new ResumeDetailResponseDto(
                resume.getId(),
                resume.getFileName(),
                resume.getQualityScore(),
                resume.getProcessingStatus().name(),
                resume.getCreatedAt(),
                analysisDto,
                rawPreview
        );
    }

    @Transactional
    public void deleteResume(UUID resumeId, UUID userId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        // Delete from local disk
        fileStorageService.deleteFile(resume.getFilePath());

        // Delete from database
        resumeRepository.delete(resume);
    }
}

