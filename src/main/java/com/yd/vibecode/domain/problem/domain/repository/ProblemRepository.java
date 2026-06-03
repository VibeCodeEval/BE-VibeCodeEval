package com.yd.vibecode.domain.problem.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yd.vibecode.domain.problem.domain.entity.Problem;
import com.yd.vibecode.domain.problem.domain.entity.ProblemStatus;
import com.yd.vibecode.domain.problem.domain.entity.Difficulty;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findByStatus(ProblemStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Problem p WHERE p.status = :status")
    List<Problem> findAllByStatusForUpdate(@Param("status") ProblemStatus status);

    long countByStatus(ProblemStatus status);
    
    List<Problem> findByDifficulty(Difficulty difficulty);

    Optional<Problem> findByTitle(String title);
}
