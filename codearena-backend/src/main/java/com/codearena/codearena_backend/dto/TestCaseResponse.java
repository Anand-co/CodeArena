package com.codearena.codearena_backend.dto;

public class TestCaseResponse {

    private Long id;
    private Long problemId;
    private String input;

    public TestCaseResponse() {
    }

    public TestCaseResponse(
            Long id, Long problemId, String input) {
        this.id = id;
        this.problemId = problemId;
        this.input = input;
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
}
