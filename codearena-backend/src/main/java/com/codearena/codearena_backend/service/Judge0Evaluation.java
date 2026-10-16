package com.codearena.codearena_backend.service;

import com.codearena.codearena_backend.entity.SubmissionStatus;

public class Judge0Evaluation {

    private SubmissionStatus status;
    private Double executionTimeSeconds;
    private Long memoryUsageKb;
    private Integer passedTestCases;
    private Integer totalTestCases;
    private String errorMessage;

    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }

    public Double getExecutionTimeSeconds() {
        return executionTimeSeconds;
    }
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