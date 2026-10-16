package com.codearena.codearena_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codearena.codearena_backend.dto.SubmissionRequest;
import com.codearena.codearena_backend.dto.SubmissionResponse;
import com.codearena.codearena_backend.service.SubmissionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<SubmissionResponse> createSubmission(
            Authentication authentication,
            @Valid @RequestBody SubmissionRequest request) {

        SubmissionResponse response =
                submissionService.createSubmission(
                        authentication.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(
            Authentication authentication) {

        return ResponseEntity.ok(
                submissionService.getMySubmissions(
                        authentication.getName()));
    }

    @GetMapping("/my/{id}")
    public ResponseEntity<SubmissionResponse> getMySubmission(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                submissionService.getMySubmission(
                        id, authentication.getName()));
    }

    @GetMapping("/my/problem/{problemId}")
    public ResponseEntity<List<SubmissionResponse>>
            getMySubmissionsForProblem(
                    @PathVariable Long problemId,
                    Authentication authentication) {

        return ResponseEntity.ok(
                submissionService.getMySubmissionsForProblem(
                        problemId, authentication.getName()));
    }
}