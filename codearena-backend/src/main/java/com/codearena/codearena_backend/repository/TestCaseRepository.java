package com.codearena.codearena_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codearena.codearena_backend.entity.TestCase;

public interface TestCaseRepository
        extends JpaRepository<TestCase, Long> {

    List<TestCase> findByProblemIdOrderByIdAsc(Long problemId);

    List<TestCase> findByProblemIdAndHiddenFalseOrderByIdAsc(
            Long problemId);

    boolean existsByProblemId(Long problemId);
}