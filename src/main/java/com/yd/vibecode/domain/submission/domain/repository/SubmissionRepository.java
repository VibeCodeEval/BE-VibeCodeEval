package com.yd.vibecode.domain.submission.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yd.vibecode.domain.submission.domain.entity.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByExamIdAndParticipantId(Long examId, Long participantId);
    
    List<Submission> findByExamId(Long examId);
    
    List<Submission> findByParticipantId(Long participantId);
}
