package com.codearena.codearena_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.codearena.codearena_backend.dto.AdminTestCaseResponse;
import com.codearena.codearena_backend.dto.TestCaseRequest;
import com.codearena.codearena_backend.dto.TestCaseResponse;
import com.codearena.codearena_backend.service.TestCaseService;

import jakarta.validation.Valid;

@RestController
public class TestCaseController {

    private final TestCaseService testCaseService;

    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    // Authenticated users see only public test cases.
    @GetMapping("/api/problems/{problemId}/test-cases")
    public ResponseEntity<List<TestCaseResponse>> getPublicTestCases(
            @PathVariable Long problemId) {

        return ResponseEntity.ok(
                testCaseService.getPublicTestCases(problemId));
    }

    // Admin: view all test cases, including hidden expected outputs.
    @GetMapping("/api/admin/problems/{problemId}/test-cases")
    public ResponseEntity<List<AdminTestCaseResponse>> getAllTestCases(
            @PathVariable Long problemId) {

        return ResponseEntity.ok(
                testCaseService.getAllTestCases(problemId));
    }

    @PostMapping("/api/admin/problems/{problemId}/test-cases")
    public ResponseEntity<AdminTestCaseResponse> createTestCase(
            @PathVariable Long problemId,
            @Valid @RequestBody TestCaseRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(testCaseService.createTestCase(
                        problemId, request));
    }

    @PutMapping("/api/admin/test-cases/{id}")
    public ResponseEntity<AdminTestCaseResponse> updateTestCase(
            @PathVariable Long id,
            @Valid @RequestBody TestCaseRequest request) {

        return ResponseEntity.ok(
                testCaseService.updateTestCase(id, request));
    }

    @DeleteMapping("/api/admin/test-cases/{id}")
    public ResponseEntity<Void> deleteTestCase(
            @PathVariable Long id) {

        testCaseService.deleteTestCase(id);
        return ResponseEntity.noContent().build();
    }
}
