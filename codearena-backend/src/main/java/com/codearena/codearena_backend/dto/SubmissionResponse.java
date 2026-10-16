package com.codearena.codearena_backend.dto;

import java.time.LocalDateTime;

import com.codearena.codearena_backend.entity.SubmissionStatus;

public class SubmissionResponse {

    private Long id;
    private Long problemId;
    private String problemTitle;
    private String language;
    private SubmissionStatus status;
    private LocalDateTime submittedAt;

    private Double executionTimeSeconds;
    private Long memoryUsageKb;
    private Integer passedTestCases;
    private Integer totalTestCases;
    private String errorMessage;

    public SubmissionResponse() {}

    public SubmissionResponse(
            Long id,
            Long problemId,
            String problemTitle,
            String language,
            SubmissionStatus status,
            LocalDateTime submittedAt,
            Double executionTimeSeconds,
            Long memoryUsageKb,
            Integer passedTestCases,
            Integer totalTestCases,
            String errorMessage) {

        this.id = id;
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.language = language;
        this.status = status;
        this.submittedAt = submittedAt;
        this.executionTimeSeconds = executionTimeSeconds;
        this.memoryUsageKb = memoryUsageKb;
        this.passedTestCases = passedTestCases;
        this.totalTestCases = totalTestCases;
        this.errorMessage = errorMessage;
    }

    public Long getId() { return id; }
    public Long getProblemId() { return problemId; }
    public String getProblemTitle() { return problemTitle; }
    public String getLanguage() { return language; }
    public SubmissionStatus getStatus() { return status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }

    public Double getExecutionTimeSeconds() {
        return executionTimeSeconds;
    }

    public Long getMemoryUsageKb() { return memoryUsageKb; }
    public Integer getPassedTestCases() { return passedTestCases; }
    public Integer getTotalTestCases() { return totalTestCases; }
    public String getErrorMessage() { return errorMessage; }
}