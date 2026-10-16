package com.codearena.codearena_backend.dto;

public class AdminTestCaseResponse {

    private Long id;
    private Long problemId;
    private String input;
    private String expectedOutput;
    private boolean hidden;

    public AdminTestCaseResponse() {
    }

    public AdminTestCaseResponse(
            Long id,
            Long problemId,
            String input,
            String expectedOutput,
            boolean hidden) {
        this.id = id;
        this.problemId = problemId;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.hidden = hidden;
    }

    public Long getId() {
        return id;
    }

    public Long getProblemId() {
        return problemId;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public boolean isHidden() {
        return hidden;
    }
}
