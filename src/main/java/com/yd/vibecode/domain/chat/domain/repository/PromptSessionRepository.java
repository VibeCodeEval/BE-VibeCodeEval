package com.yd.vibecode.domain.chat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yd.vibecode.domain.chat.domain.entity.PromptSession;

import java.util.List;
import java.util.Optional;

public interface PromptSessionRepository extends JpaRepository<PromptSession, Long> {

    Optional<PromptSession> findByExamIdAndParticipantId(Long examId, Long participantId);

    List<PromptSession> findByExamId(Long examId);

    List<PromptSession> findByParticipantId(Long participantId);
}
