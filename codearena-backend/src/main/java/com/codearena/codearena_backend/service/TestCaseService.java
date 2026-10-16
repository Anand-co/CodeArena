package com.codearena.codearena_backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.codearena.codearena_backend.dto.AdminTestCaseResponse;
import com.codearena.codearena_backend.dto.TestCaseRequest;
import com.codearena.codearena_backend.dto.TestCaseResponse;
import com.codearena.codearena_backend.entity.Problem;
import com.codearena.codearena_backend.entity.TestCase;
import com.codearena.codearena_backend.repository.ProblemRepository;
import com.codearena.codearena_backend.repository.TestCaseRepository;

@Service
public class TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;

    public TestCaseService(
            TestCaseRepository testCaseRepository,
            ProblemRepository problemRepository) {
        this.testCaseRepository = testCaseRepository;
        this.problemRepository = problemRepository;
    }

    @Transactional(readOnly = true)
    public List<TestCaseResponse> getPublicTestCases(
            Long problemId) {

        ensureProblemExists(problemId);

        return testCaseRepository
                .findByProblemIdAndHiddenFalseOrderByIdAsc(problemId)
                .stream()
                .map(testCase -> new TestCaseResponse(
                        testCase.getId(),
                        testCase.getProblem().getId(),
                        testCase.getInput()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminTestCaseResponse> getAllTestCases(
            Long problemId) {

        ensureProblemExists(problemId);

        return testCaseRepository
                .findByProblemIdOrderByIdAsc(problemId)
                .stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Transactional
    public AdminTestCaseResponse createTestCase(
            Long problemId, TestCaseRequest request) {

        Problem problem = findProblem(problemId);

        TestCase testCase = new TestCase();
        testCase.setProblem(problem);
        copyRequest(request, testCase);

        return toAdminResponse(
                testCaseRepository.save(testCase));
    }

    @Transactional
    public AdminTestCaseResponse updateTestCase(
            Long testCaseId, TestCaseRequest request) {

        TestCase testCase = findTestCase(testCaseId);
        copyRequest(request, testCase);

        return toAdminResponse(
                testCaseRepository.save(testCase));
    }

    @Transactional
    public void deleteTestCase(Long testCaseId) {
        TestCase testCase = findTestCase(testCaseId);
        testCaseRepository.delete(testCase);
    }

    private void copyRequest(
            TestCaseRequest request, TestCase testCase) {

        testCase.setInput(request.getInput());
        testCase.setExpectedOutput(request.getExpectedOutput());
        testCase.setHidden(request.getHidden());
    }

    private Problem findProblem(Long problemId) {
        return problemRepository.findById(problemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Problem not found: " + problemId));
    }

    private void ensureProblemExists(Long problemId) {
        if (!problemRepository.existsById(problemId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Problem not found: " + problemId);
        }
    }

    private TestCase findTestCase(Long id) {
        return testCaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Test case not found: " + id));
    }

    private AdminTestCaseResponse toAdminResponse(
            TestCase testCase) {

        return new AdminTestCaseResponse(
                testCase.getId(),
                testCase.getProblem().getId(),
                testCase.getInput(),
                testCase.getExpectedOutput(),
                testCase.isHidden());
    }
}
