package com.yd.vibecode.domain.chat.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.chat.domain.entity.PromptSession;
import com.yd.vibecode.domain.chat.domain.repository.PromptSessionRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PromptSessionService {

    private final PromptSessionRepository promptSessionRepository;

    @Transactional
    public PromptSession create(Long examId, Long participantId, Long specId) {
        PromptSession session = PromptSession.builder()
                .examId(examId)
                .participantId(participantId)
                .specId(specId)
                .totalTokens(0)
                .startedAt(LocalDateTime.now())
                .build();
        return promptSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public PromptSession findById(Long id) {
        return promptSessionRepository.findById(id)
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus._NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PromptSession findByExamIdAndParticipantId(Long examId, Long participantId) {
        return promptSessionRepository.findByExamIdAndParticipantId(examId, participantId)
                .orElse(null);
    }

    @Transactional
    public PromptSession getOrCreateSession(Long examId, Long participantId, Long specId) {
        PromptSession existing = findByExamIdAndParticipantId(examId, participantId);
        if (existing != null) {
            return existing;
        }
        return create(examId, participantId, specId);
    }

    @Transactional
    public void addTokens(Long sessionId, Integer tokens) {
        PromptSession session = findById(sessionId);
        session.addTokens(tokens);
    }

    @Transactional
    public void endSession(Long sessionId) {
        PromptSession session = findById(sessionId);
        session.endSession();
    }
}
