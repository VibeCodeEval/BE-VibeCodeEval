package com.yd.vibecode.domain.problem.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yd.vibecode.domain.problem.domain.entity.Problem;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findByStatus(com.yd.vibecode.domain.problem.domain.entity.ProblemStatus status);
    
    List<Problem> findByDifficulty(com.yd.vibecode.domain.problem.domain.entity.Difficulty difficulty);
}
