package com.yd.vibecode.domain.auth.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yd.vibecode.domain.auth.domain.entity.ExamParticipant;

public interface ExamParticipantRepository extends JpaRepository<ExamParticipant, Long> {

    Optional<ExamParticipant> findByExamIdAndParticipantId(Long examId, Long participantId);

    List<ExamParticipant> findByExamId(Long examId);

    List<ExamParticipant> findByParticipantId(Long participantId);

    List<ExamParticipant> findByParticipantIdOrderByJoinedAtDesc(Long participantId);

    boolean existsByExamIdAndParticipantId(Long examId, Long participantId);
}

