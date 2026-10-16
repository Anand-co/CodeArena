package com.codearena.codearena_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codearena.codearena_backend.entity.Submission;
import com.codearena.codearena_backend.entity.User;

public interface SubmissionRepository
        extends JpaRepository<Submission, Long> {

    // Get all submissions for a particular user
    List<Submission> findByUserOrderBySubmittedAtDesc(User user);

    // Get all submissions for a particular problem
    List<Submission> findByProblemIdOrderBySubmittedAtDesc(
            Long problemId);

    // Get submissions for a particular user and problem
    List<Submission> findByUserAndProblemIdOrderBySubmittedAtDesc(
            User user, Long problemId);
}