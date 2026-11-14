package com.careerpilot.modules.interview.service;

import com.careerpilot.common.exception.BadRequestException;
import com.careerpilot.common.exception.ResourceNotFoundException;
import com.careerpilot.common.exception.UnauthorizedException;
import com.careerpilot.modules.auth.entity.User;
import com.careerpilot.modules.auth.repository.UserRepository;
import com.careerpilot.modules.interview.dto.*;
import com.careerpilot.modules.interview.entity.InterviewExchange;
import com.careerpilot.modules.interview.entity.InterviewSession;
import com.careerpilot.modules.interview.repository.InterviewExchangeRepository;
import com.careerpilot.modules.interview.repository.InterviewSessionRepository;
import com.careerpilot.modules.jobmatch.entity.JobDescription;
import com.careerpilot.modules.jobmatch.repository.JobDescriptionRepository;
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

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    private static final Logger logger = LoggerFactory.getLogger(InterviewService.class);

    private final InterviewSessionRepository sessionRepository;
    private final InterviewExchangeRepository exchangeRepository;
    private final UserRepository userRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final GeminiInterviewService geminiInterviewService;
    private final ObjectMapper objectMapper;

    public InterviewService(
            InterviewSessionRepository sessionRepository,
            InterviewExchangeRepository exchangeRepository,
            UserRepository userRepository,
            JobDescriptionRepository jobDescriptionRepository,
            ResumeRepository resumeRepository,
            ResumeSkillRepository resumeSkillRepository,
            GeminiInterviewService geminiInterviewService,
            ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.exchangeRepository = exchangeRepository;
        this.userRepository = userRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.resumeRepository = resumeRepository;
        this.resumeSkillRepository = resumeSkillRepository;
        this.geminiInterviewService = geminiInterviewService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public InterviewSessionDetailDto startSession(UUID userId, StartInterviewRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        JobDescription job = null;
        String jobContext = "";
        if (request.getJobId() != null) {
            job = jobDescriptionRepository.findByIdAndUserId(request.getJobId(), userId).orElse(null);
            if (job != null) {
                jobContext = job.getJobTitle() + ": " + job.getRawText();
            }
        }

        String candidateSkills = "";
        if (request.getResumeId() != null) {
            Resume resume = resumeRepository.findById(request.getResumeId()).orElse(null);
            if (resume != null && resume.getUser().getId().equals(userId)) {
                List<ResumeSkill> skills = resumeSkillRepository.findByResumeId(resume.getId());
                candidateSkills = skills.stream().map(ResumeSkill::getSkillName).collect(Collectors.joining(", "));
            }
        }

        int count = request.getTotalQuestions() > 0 ? Math.min(10, request.getTotalQuestions()) : 5;
        String difficulty = request.getDifficulty() != null ? request.getDifficulty().toUpperCase() : "MID";

        List<GeminiInterviewService.GeneratedQuestion> generatedQuestions =
                geminiInterviewService.generateQuestions(request.getTargetRole(), difficulty, count, candidateSkills, jobContext);

        InterviewSession session = new InterviewSession(user, job, request.getTargetRole(), difficulty, generatedQuestions.size());
        session = sessionRepository.save(session);

        for (int i = 0; i < generatedQuestions.size(); i++) {
            GeminiInterviewService.GeneratedQuestion q = generatedQuestions.get(i);
            String conceptsJson = serializeConcepts(q.expectedConcepts());
            InterviewExchange exchange = new InterviewExchange(session, i + 1, q.questionText(), conceptsJson);
            session.addExchange(exchange);
        }

        session = sessionRepository.save(session);
        return toDetailDto(session);
    }

    @Transactional(readOnly = true)
    public QuestionDto getCurrentQuestion(UUID sessionId, UUID userId) {
        InterviewSession session = getSessionEntity(sessionId, userId);

        List<InterviewExchange> exchanges = exchangeRepository.findBySessionIdOrderByQuestionIndexAsc(sessionId);
        for (InterviewExchange ex : exchanges) {
            if (ex.getUserAnswer() == null || ex.getUserAnswer().trim().isEmpty()) {
                return toQuestionDto(ex, session.getTotalQuestions());
            }
        }

        // All questions answered, return the last one
        if (!exchanges.isEmpty()) {
            return toQuestionDto(exchanges.get(exchanges.size() - 1), session.getTotalQuestions());
        }
        throw new ResourceNotFoundException("No questions found for session: " + sessionId);
    }

    @Transactional
    public EvaluationResponseDto submitAnswer(UUID sessionId, UUID userId, AnswerSubmissionDto submission) {
        InterviewSession session = getSessionEntity(sessionId, userId);

        if ("COMPLETED".equalsIgnoreCase(session.getStatus())) {
            throw new BadRequestException("Interview session is already completed.");
        }

        InterviewExchange exchange = exchangeRepository.findBySessionIdAndQuestionIndex(sessionId, submission.getQuestionIndex())
                .orElseThrow(() -> new ResourceNotFoundException("Question #" + submission.getQuestionIndex() + " not found in session"));

        List<String> expectedConcepts = deserializeConcepts(exchange.getExpectedConcepts());

        EvaluationResponseDto evaluation = geminiInterviewService.evaluateAnswer(
                exchange.getQuestionText(),
                expectedConcepts,
                submission.getAnswerText(),
                session.getTargetRole(),
                session.getDifficulty()
        );

        exchange.setUserAnswer(submission.getAnswerText().trim());
        exchange.setScore(evaluation.getScore());
        exchange.setTechnicalAccuracy(evaluation.getTechnicalAccuracy());
        exchange.setClarityScore(evaluation.getClarityScore());
        exchange.setFeedback(evaluation.getFeedback());
        exchange.setModelAnswer(evaluation.getModelAnswer());
        exchange.setAnsweredAt(Instant.now());
        exchangeRepository.save(exchange);

        evaluation.setQuestionIndex(submission.getQuestionIndex());

        // Check if all questions are now answered
        List<InterviewExchange> allExchanges = exchangeRepository.findBySessionIdOrderByQuestionIndexAsc(sessionId);
        long answeredCount = allExchanges.stream().filter(e -> e.getUserAnswer() != null && !e.getUserAnswer().trim().isEmpty()).count();

        if (answeredCount >= session.getTotalQuestions()) {
            // Finalize session
            int totalScore = allExchanges.stream().mapToInt(e -> e.getScore() != null ? e.getScore() : 0).sum();
            int avgScore = (int) Math.round((double) totalScore / session.getTotalQuestions());
            String overallFeedback = geminiInterviewService.generateOverallFeedback(
                    session.getTargetRole(), session.getDifficulty(), avgScore, allExchanges
            );

            session.setOverallScore(avgScore);
            session.setOverallFeedback(overallFeedback);
            session.setStatus("COMPLETED");
            session.setCompletedAt(Instant.now());
            sessionRepository.save(session);

            evaluation.setSessionFinished(true);
            evaluation.setNextQuestionIndex(null);
            evaluation.setOverallScore(avgScore);
            evaluation.setOverallFeedback(overallFeedback);
        } else {
            evaluation.setSessionFinished(false);
            evaluation.setNextQuestionIndex(submission.getQuestionIndex() + 1);
        }

        return evaluation;
    }

    @Transactional
    public InterviewSessionDetailDto finishSession(UUID sessionId, UUID userId) {
        InterviewSession session = getSessionEntity(sessionId, userId);

        if (!"COMPLETED".equalsIgnoreCase(session.getStatus())) {
            List<InterviewExchange> exchanges = exchangeRepository.findBySessionIdOrderByQuestionIndexAsc(sessionId);
            List<InterviewExchange> answered = exchanges.stream().filter(e -> e.getScore() != null).toList();

            int overallScore = 0;
            if (!answered.isEmpty()) {
                overallScore = (int) Math.round((double) answered.stream().mapToInt(InterviewExchange::getScore).sum() / answered.size());
            }

            String feedback = geminiInterviewService.generateOverallFeedback(
                    session.getTargetRole(), session.getDifficulty(), overallScore, answered
            );

            session.setOverallScore(overallScore);
            session.setOverallFeedback(feedback);
            session.setStatus("COMPLETED");
            session.setCompletedAt(Instant.now());
            session = sessionRepository.save(session);
        }

        return toDetailDto(session);
    }

    @Transactional(readOnly = true)
    public List<InterviewSessionSummaryDto> getUserSessions(UUID userId) {
        List<InterviewSession> sessions = sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return sessions.stream().map(s -> {
            long completedQuestions = exchangeRepository.findBySessionIdOrderByQuestionIndexAsc(s.getId())
                    .stream().filter(e -> e.getUserAnswer() != null && !e.getUserAnswer().isEmpty()).count();
            return new InterviewSessionSummaryDto(
                    s.getId(),
                    s.getTargetRole(),
                    s.getDifficulty(),
                    s.getStatus(),
                    s.getTotalQuestions(),
                    (int) completedQuestions,
                    s.getOverallScore(),
                    s.getCreatedAt(),
                    s.getCompletedAt()
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InterviewSessionDetailDto getSessionDetail(UUID sessionId, UUID userId) {
        InterviewSession session = getSessionEntity(sessionId, userId);
        return toDetailDto(session);
    }

    private InterviewSession getSessionEntity(UUID sessionId, UUID userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found with ID: " + sessionId));
    }

    private InterviewSessionDetailDto toDetailDto(InterviewSession session) {
        List<InterviewExchange> exchanges = exchangeRepository.findBySessionIdOrderByQuestionIndexAsc(session.getId());
        List<QuestionDto> questions = exchanges.stream()
                .map(e -> toQuestionDto(e, session.getTotalQuestions()))
                .collect(Collectors.toList());

        return new InterviewSessionDetailDto(
                session.getId(),
                session.getTargetRole(),
                session.getDifficulty(),
                session.getStatus(),
                session.getTotalQuestions(),
                session.getOverallScore(),
                session.getOverallFeedback(),
                session.getCreatedAt(),
                session.getCompletedAt(),
                questions
        );
    }

    private QuestionDto toQuestionDto(InterviewExchange exchange, int totalQuestions) {
        List<String> concepts = deserializeConcepts(exchange.getExpectedConcepts());
        boolean answered = exchange.getUserAnswer() != null && !exchange.getUserAnswer().trim().isEmpty();

        return new QuestionDto(
                exchange.getId(),
                exchange.getSession().getId(),
                exchange.getQuestionIndex(),
                totalQuestions,
                exchange.getQuestionText(),
                concepts,
                exchange.getUserAnswer(),
                exchange.getScore(),
                exchange.getFeedback(),
                exchange.getModelAnswer(),
                answered
        );
    }

    private String serializeConcepts(List<String> concepts) {
        if (concepts == null || concepts.isEmpty()) return "[]";
        try {
            return objectMapper.writeValueAsString(concepts);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<String> deserializeConcepts(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}

