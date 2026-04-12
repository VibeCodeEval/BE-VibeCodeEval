package com.yd.vibecode.domain.submission.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yd.vibecode.domain.submission.domain.entity.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByExamIdAndParticipantId(Long examId, Long participantId);
    
    List<Submission> findByExamId(Long examId);
    
    List<Submission> findByParticipantId(Long participantId);

    long countByExamId(Long examId);

    @Query("SELECT s.examId, COUNT(s) FROM Submission s WHERE s.examId IN :examIds GROUP BY s.examId")
    List<Object[]> countGroupByExamIdIn(@Param("examIds") List<Long> examIds);
}
