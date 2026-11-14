package com.careerpilot.modules.interview.dto;

public class EvaluationResponseDto {

    private int questionIndex;
    private int score;
    private int technicalAccuracy;
    private int clarityScore;
    private String feedback;
    private String modelAnswer;
    private boolean isSessionFinished;
    private Integer nextQuestionIndex;
    private Integer overallScore;
    private String overallFeedback;

    public EvaluationResponseDto() {
    }

    public EvaluationResponseDto(int questionIndex, int score, int technicalAccuracy,
                                 int clarityScore, String feedback, String modelAnswer,
                                 boolean isSessionFinished, Integer nextQuestionIndex,
                                 Integer overallScore, String overallFeedback) {
        this.questionIndex = questionIndex;
        this.score = score;
        this.technicalAccuracy = technicalAccuracy;
        this.clarityScore = clarityScore;
        this.feedback = feedback;
        this.modelAnswer = modelAnswer;
        this.isSessionFinished = isSessionFinished;
        this.nextQuestionIndex = nextQuestionIndex;
        this.overallScore = overallScore;
        this.overallFeedback = overallFeedback;
    }

    public int getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTechnicalAccuracy() {
        return technicalAccuracy;
    }

    public void setTechnicalAccuracy(int technicalAccuracy) {
        this.technicalAccuracy = technicalAccuracy;
    }

    public int getClarityScore() {
        return clarityScore;
    }

    public void setClarityScore(int clarityScore) {
        this.clarityScore = clarityScore;
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

    public boolean isSessionFinished() {
        return isSessionFinished;
    }

    public void setSessionFinished(boolean sessionFinished) {
        isSessionFinished = sessionFinished;
    }

    public Integer getNextQuestionIndex() {
        return nextQuestionIndex;
    }

    public void setNextQuestionIndex(Integer nextQuestionIndex) {
        this.nextQuestionIndex = nextQuestionIndex;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public String getOverallFeedback() {
        return overallFeedback;
    }

    public void setOverallFeedback(String overallFeedback) {
        this.overallFeedback = overallFeedback;
    }
}

