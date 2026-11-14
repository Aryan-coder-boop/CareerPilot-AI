package com.careerpilot.interview;

import com.careerpilot.common.exception.BadRequestException;
import com.careerpilot.modules.auth.entity.Role;
import com.careerpilot.modules.auth.entity.User;
import com.careerpilot.modules.auth.repository.UserRepository;
import com.careerpilot.modules.interview.dto.*;
import com.careerpilot.modules.interview.entity.InterviewExchange;
import com.careerpilot.modules.interview.entity.InterviewSession;
import com.careerpilot.modules.interview.repository.InterviewExchangeRepository;
import com.careerpilot.modules.interview.repository.InterviewSessionRepository;
import com.careerpilot.modules.interview.service.GeminiInterviewService;
import com.careerpilot.modules.interview.service.InterviewService;
import com.careerpilot.modules.jobmatch.repository.JobDescriptionRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private InterviewSessionRepository sessionRepository;

    @Mock
    private InterviewExchangeRepository exchangeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobDescriptionRepository jobDescriptionRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ResumeSkillRepository resumeSkillRepository;

    @Mock
    private GeminiInterviewService geminiInterviewService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private InterviewService interviewService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User("developer@example.com", "hash", "Alice Dev", Role.ROLE_USER);
        user.setId(userId);
    }

    @Test
    @DisplayName("Should successfully start mock interview session and populate generated questions")
    void testStartInterviewSession() {
        StartInterviewRequestDto request = new StartInterviewRequestDto("Cloud Architect", "SENIOR", null, null, 3);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        List<GeminiInterviewService.GeneratedQuestion> genQuestions = List.of(
                new GeminiInterviewService.GeneratedQuestion("Q1: Explain VPC peering", List.of("VPC", "Routing")),
                new GeminiInterviewService.GeneratedQuestion("Q2: Multi-region failover", List.of("Route53", "Replication")),
                new GeminiInterviewService.GeneratedQuestion("Q3: IAM best practices", List.of("Least Privilege", "MFA"))
        );
        when(geminiInterviewService.generateQuestions(eq("Cloud Architect"), eq("SENIOR"), eq(3), any(), any()))
                .thenReturn(genQuestions);

        InterviewSession savedSession = new InterviewSession(user, null, "Cloud Architect", "SENIOR", 3);
        savedSession.setId(UUID.randomUUID());
        when(sessionRepository.save(any(InterviewSession.class))).thenReturn(savedSession);

        InterviewSessionDetailDto result = interviewService.startSession(userId, request);

        assertNotNull(result);
        assertEquals("Cloud Architect", result.getTargetRole());
        assertEquals(3, result.getTotalQuestions());
        verify(sessionRepository, atLeastOnce()).save(any(InterviewSession.class));
    }

    @Test
    @DisplayName("Should submit answer and evaluate performance")
    void testSubmitAnswer() {
        UUID sessionId = UUID.randomUUID();
        InterviewSession session = new InterviewSession(user, null, "Java Dev", "MID", 2);
        session.setId(sessionId);

        InterviewExchange exchange = new InterviewExchange(session, 1, "What is Spring IoC?", "[\"IoC\",\"Bean\"]");
        exchange.setId(UUID.randomUUID());

        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));
        when(exchangeRepository.findBySessionIdAndQuestionIndex(sessionId, 1)).thenReturn(Optional.of(exchange));

        EvaluationResponseDto evalDto = new EvaluationResponseDto(
                1, 85, 88, 80, "Great answer", "Model answer", false, 2, null, null
        );
        when(geminiInterviewService.evaluateAnswer(any(), any(), any(), any(), any())).thenReturn(evalDto);
        when(exchangeRepository.findBySessionIdOrderByQuestionIndexAsc(sessionId)).thenReturn(List.of(exchange));

        AnswerSubmissionDto submission = new AnswerSubmissionDto(1, "IoC is Inversion of Control where Spring manages beans.");
        EvaluationResponseDto response = interviewService.submitAnswer(sessionId, userId, submission);

        assertNotNull(response);
        assertEquals(85, response.getScore());
        assertEquals("Great answer", response.getFeedback());
        verify(exchangeRepository, times(1)).save(exchange);
    }
}

