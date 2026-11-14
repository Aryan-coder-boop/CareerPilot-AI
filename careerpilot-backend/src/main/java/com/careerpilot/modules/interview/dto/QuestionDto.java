package com.careerpilot.modules.interview.dto;

import java.util.List;
import java.util.UUID;

public class QuestionDto {

    private UUID id;
    private UUID sessionId;
    private int questionIndex;
    private int totalQuestions;
    private String questionText;
    private List<String> expectedConcepts;
    private String userAnswer;
    private Integer score;
    private String feedback;
    private String modelAnswer;
    private boolean answered;

    public QuestionDto() {
    }

    public QuestionDto(UUID id, UUID sessionId, int questionIndex, int totalQuestions,
                       String questionText, List<String> expectedConcepts, String userAnswer,
                       Integer score, String feedback, String modelAnswer, boolean answered) {
        this.id = id;
        this.sessionId = sessionId;
        this.questionIndex = questionIndex;
        this.totalQuestions = totalQuestions;
        this.questionText = questionText;
        this.expectedConcepts = expectedConcepts;
        this.userAnswer = userAnswer;
        this.score = score;
        this.feedback = feedback;
        this.modelAnswer = modelAnswer;
        this.answered = answered;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public int getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getExpectedConcepts() {
        return expectedConcepts;
    }

    public void setExpectedConcepts(List<String> expectedConcepts) {
        this.expectedConcepts = expectedConcepts;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getModelAnswer() {
        return modelAnswer;
    }

    public void setModelAnswer(String modelAnswer) {
        this.modelAnswer = modelAnswer;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }
}

