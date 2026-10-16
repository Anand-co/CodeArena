package com.codearena.codearena_backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, length = 30)
    private String language;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sourceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    private LocalDateTime submittedAt;

    private Double executionTimeSeconds;
    private Long memoryUsageKb;
    private Integer passedTestCases;
    private Integer totalTestCases;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    public Submission() {}

    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) submittedAt = LocalDateTime.now();
        if (status == null) status = SubmissionStatus.PENDING;
        if (passedTestCases == null) passedTestCases = 0;
        if (totalTestCases == null) totalTestCases = 0;
    }

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }

    public Double getExecutionTimeSeconds() { return executionTimeSeconds; }
    public void setExecutionTimeSeconds(Double value) {
        this.executionTimeSeconds = value;
    }

    public Long getMemoryUsageKb() { return memoryUsageKb; }
    public void setMemoryUsageKb(Long value) {
        this.memoryUsageKb = value;
    }

    public Integer getPassedTestCases() { return passedTestCases; }
    public void setPassedTestCases(Integer value) {
        this.passedTestCases = value;
    }

    public Integer getTotalTestCases() { return totalTestCases; }
    public void setTotalTestCases(Integer value) {
        this.totalTestCases = value;
    }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }
}