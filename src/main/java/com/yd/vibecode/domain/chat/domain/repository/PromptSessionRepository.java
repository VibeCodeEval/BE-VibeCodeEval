package com.yd.vibecode.domain.chat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yd.vibecode.domain.chat.domain.entity.PromptSession;
import com.yd.vibecode.domain.exam.domain.entity.ExamState;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PromptSessionRepository extends JpaRepository<PromptSession, Long> {

    Optional<PromptSession> findByExamIdAndParticipantId(Long examId, Long participantId);

    List<PromptSession> findByExamId(Long examId);

    List<PromptSession> findByParticipantId(Long participantId);

    @Query("""
            SELECT ps.id FROM PromptSession ps, Exam e
            WHERE ps.examId = e.id
              AND e.state = :examState
              AND ps.createdAt < :cutoff
            """)
    List<Long> findIdsByExamStateAndCreatedAtBefore(
            @Param("examState") ExamState examState,
            @Param("cutoff") LocalDateTime cutoff);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PromptSession ps WHERE ps.id IN :ids")
    int deleteByIdIn(@Param("ids") Collection<Long> ids);
}
