package com.yd.vibecode.domain.submission.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yd.vibecode.domain.submission.domain.entity.SubmissionRun;

import java.util.List;

public interface SubmissionRunRepository extends JpaRepository<SubmissionRun, Long> {

    List<SubmissionRun> findBySubmissionId(Long submissionId);
    
    List<SubmissionRun> findBySubmissionIdOrderByCaseIndexAsc(Long submissionId);
}
