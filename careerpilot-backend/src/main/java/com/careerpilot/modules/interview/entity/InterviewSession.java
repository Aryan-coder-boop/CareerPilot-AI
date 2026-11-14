package com.careerpilot.modules.interview.entity;

import com.careerpilot.modules.auth.entity.User;
import com.careerpilot.modules.jobmatch.entity.JobDescription;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "interview_sessions")
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private JobDescription jobDescription;

    @Column(name = "target_role", nullable = false, length = 150)
    private String targetRole;

    @Column(name = "difficulty", nullable = false, length = 30)
    private String difficulty; // JUNIOR, MID, SENIOR, LEAD

    @Column(name = "status", nullable = false, length = 30)
    private String status = "IN_PROGRESS"; // IN_PROGRESS, COMPLETED, ABANDONED

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions = 5;

    @Column(name = "overall_score")
    private Integer overallScore;

    @Column(name = "overall_feedback", columnDefinition = "TEXT")
    private String overallFeedback;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("questionIndex ASC")
    private List<InterviewExchange> exchanges = new ArrayList<>();

    public InterviewSession() {
    }

    public InterviewSession(User user, JobDescription jobDescription, String targetRole, String difficulty, int totalQuestions) {
        this.user = user;
        this.jobDescription = jobDescription;
        this.targetRole = targetRole;
        this.difficulty = difficulty != null ? difficulty.toUpperCase() : "MID";
        this.totalQuestions = totalQuestions > 0 ? totalQuestions : 5;
        this.status = "IN_PROGRESS";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public JobDescription getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(JobDescription jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public List<InterviewExchange> getExchanges() {
        return exchanges;
    }

    public void setExchanges(List<InterviewExchange> exchanges) {
        this.exchanges = exchanges;
    }

    public void addExchange(InterviewExchange exchange) {
        exchanges.add(exchange);
        exchange.setSession(this);
    }
}

