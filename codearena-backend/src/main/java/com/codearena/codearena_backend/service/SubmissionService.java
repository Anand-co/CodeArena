package com.codearena.codearena_backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.codearena.codearena_backend.dto.SubmissionRequest;
import com.codearena.codearena_backend.dto.SubmissionResponse;
import com.codearena.codearena_backend.entity.Problem;
import com.codearena.codearena_backend.entity.Submission;
import com.codearena.codearena_backend.entity.SubmissionStatus;
import com.codearena.codearena_backend.entity.User;
import com.codearena.codearena_backend.repository.ProblemRepository;
import com.codearena.codearena_backend.repository.SubmissionRepository;
import com.codearena.codearena_backend.repository.UserRepository;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final Judge0Service judge0Service;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            ProblemRepository problemRepository,
            UserRepository userRepository,
            Judge0Service judge0Service) {

        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
        this.judge0Service = judge0Service;
    }

    public SubmissionResponse createSubmission(
            String authenticatedEmail,
            SubmissionRequest request) {

        User user = findUser(authenticatedEmail);

        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Problem not found"));

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setProblem(problem);
        submission.setLanguage(request.getLanguage().trim());
        submission.setSourceCode(request.getSourceCode());
        submission.setStatus(SubmissionStatus.PENDING);

        Submission saved = submissionRepository.save(submission);

        try {
            Judge0Evaluation result = judge0Service.evaluate(
                    problem.getId(),
                    saved.getLanguage(),
                    saved.getSourceCode());

            saved.setStatus(result.getStatus());
            saved.setExecutionTimeSeconds(result.getExecutionTimeSeconds());
            saved.setMemoryUsageKb(result.getMemoryUsageKb());
            saved.setPassedTestCases(result.getPassedTestCases());
            saved.setTotalTestCases(result.getTotalTestCases());
            saved.setErrorMessage(result.getErrorMessage());

        } catch (Exception ex) {
            saved.setStatus(SubmissionStatus.JUDGE_ERROR);
            saved.setErrorMessage("Execution service failed. Please try again.");
        }

        return toResponse(submissionRepository.save(saved));
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getMySubmissions(String email) {
        User user = findUser(email);

        return submissionRepository
                .findByUserOrderBySubmittedAtDesc(user)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getMySubmission(Long id, String email) {
        User user = findUser(email);

        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Submission not found"));

        if (!submission.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Submission not found");
        }

        return toResponse(submission);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getMySubmissionsForProblem(
            Long problemId, String email) {

        User user = findUser(email);

        if (!problemRepository.existsById(problemId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Problem not found");
        }

        return submissionRepository
                .findByUserAndProblemIdOrderBySubmittedAtDesc(user, problemId)
                .stream().map(this::toResponse).toList();
    }

    private User findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"));
    }

    private SubmissionResponse toResponse(Submission s) {
        return new SubmissionResponse(
                s.getId(),
                s.getProblem().getId(),
                s.getProblem().getTitle(),
                s.getLanguage(),
                s.getStatus(),
                s.getSubmittedAt(),
                s.getExecutionTimeSeconds(),
                s.getMemoryUsageKb(),
                s.getPassedTestCases(),
                s.getTotalTestCases(),
                s.getErrorMessage());
    }
}