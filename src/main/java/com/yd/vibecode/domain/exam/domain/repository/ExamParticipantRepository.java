package com.yd.vibecode.domain.exam.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;

public interface ExamParticipantRepository extends JpaRepository<ExamParticipant, Long> {

    Optional<ExamParticipant> findByExamIdAndParticipantId(Long examId, Long participantId);

    List<ExamParticipant> findByExamId(Long examId);

    List<ExamParticipant> findAllByExamId(Long examId);

    List<ExamParticipant> findByParticipantId(Long participantId);

    List<ExamParticipant> findByParticipantIdOrderByJoinedAtDesc(Long participantId);

    boolean existsByExamIdAndParticipantId(Long examId, Long participantId);

    long countByExamId(Long examId);

    @Query("SELECT ep.examId, COUNT(ep) FROM ExamParticipant ep WHERE ep.examId IN :examIds GROUP BY ep.examId")
    List<Object[]> countGroupByExamIdIn(@Param("examIds") List<Long> examIds);
}
