package com.codearena.codearena_backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.codearena.codearena_backend.dto.ProblemRequest;
import com.codearena.codearena_backend.entity.Difficulty;
import com.codearena.codearena_backend.entity.Problem;
import com.codearena.codearena_backend.repository.ProblemRepository;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;

    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Transactional(readOnly = true)
    public List<Problem> getAllProblems(
            String search, Difficulty difficulty) {

        List<Problem> problems;

        if (search != null && !search.isBlank()) {
            problems = problemRepository
                    .findByTitleContainingIgnoreCase(search.trim());
        } else {
            problems = problemRepository
                    .findAllByOrderByCreatedAtDesc();
        }

        if (difficulty != null) {
            problems = problems.stream()
                    .filter(problem ->
                            problem.getDifficulty() == difficulty)
                    .toList();
        }

        return problems;
    }

    @Transactional(readOnly = true)
    public Problem getProblemById(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Problem not found with ID: " + id));
    }

    @Transactional
    public Problem createProblem(ProblemRequest request) {
        if (problemRepository.existsByTitleIgnoreCase(
                request.getTitle().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A problem with this title already exists");
        }

        Problem problem = new Problem();
        copyRequestToProblem(request, problem);

        return problemRepository.save(problem);
    }

    @Transactional
    public Problem updateProblem(
            Long id, ProblemRequest request) {

        Problem problem = getProblemById(id);

        if (problemRepository.existsByTitleIgnoreCaseAndIdNot(
                request.getTitle().trim(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another problem already uses this title");
        }

        copyRequestToProblem(request, problem);

        return problemRepository.save(problem);
    }

    @Transactional
    public void deleteProblem(Long id) {
        Problem problem = getProblemById(id);
        problemRepository.delete(problem);
    }

    private void copyRequestToProblem(
            ProblemRequest request, Problem problem) {

        problem.setTitle(request.getTitle().trim());
        problem.setDescription(request.getDescription().trim());
        problem.setDifficulty(request.getDifficulty());
        problem.setConstraintsText(request.getConstraintsText());
        problem.setSampleInput(request.getSampleInput());
        problem.setSampleOutput(request.getSampleOutput());
        problem.setExplanation(request.getExplanation());
    }
}