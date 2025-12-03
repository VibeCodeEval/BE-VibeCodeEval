package com.yd.vibecode.domain.submission.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yd.vibecode.domain.submission.domain.entity.Score;

import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    Optional<Score> findBySubmissionId(Long submissionId);
}
