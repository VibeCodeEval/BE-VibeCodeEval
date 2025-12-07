package com.yd.vibecode.domain.problem.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.entity.ProblemStatus;
import com.yd.vibecode.domain.problem.domain.entity.Difficulty;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findByStatus(ProblemStatus status);
    
    List<Problem> findByDifficulty(Difficulty difficulty);

    Optional<Problem> findByTitle(String title);
}
