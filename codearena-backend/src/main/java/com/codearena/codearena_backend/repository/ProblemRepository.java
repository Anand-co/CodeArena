package com.codearena.codearena_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codearena.codearena_backend.entity.Difficulty;
import com.codearena.codearena_backend.entity.Problem;

public interface ProblemRepository
        extends JpaRepository<Problem, Long> {

    // Get all problems, newest first
    List<Problem> findAllByOrderByCreatedAtDesc();

    // Search problems by title
    List<Problem> findByTitleContainingIgnoreCase(String title);

    // Filter problems by difficulty
    List<Problem> findByDifficulty(Difficulty difficulty);

    // Check for duplicate problem titles
    boolean existsByTitleIgnoreCase(String title);

    // Check for duplicate titles when updating
    boolean existsByTitleIgnoreCaseAndIdNot(
            String title, Long id);
}