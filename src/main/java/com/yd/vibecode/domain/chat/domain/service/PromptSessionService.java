package com.yd.vibecode.domain.chat.domain.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.chat.domain.entity.PromptSession;
import com.yd.vibecode.domain.chat.domain.repository.PromptSessionRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;

import lombok.RequiredArgsConstructor;

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
        PromptSession session = create(examId, participantId, specId);
        // flush를 호출하여 즉시 DB에 반영 (다른 트랜잭션에서도 조회 가능하도록)
        promptSessionRepository.flush();
        return session;
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

    /**
     * 세션 조회/생성 (별도 트랜잭션으로 즉시 커밋)
     * AI 서버에서 세션을 조회할 수 있도록 별도 트랜잭션으로 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PromptSession getOrCreateSessionWithNewTransaction(Long examId, Long participantId, Long specId) {
        // 별도 트랜잭션 내에서 직접 repository 호출하여 조회/생성
        PromptSession existing = promptSessionRepository.findByExamIdAndParticipantId(examId, participantId)
                .orElse(null);
        if (existing != null) {
            return existing;
        }
        PromptSession session = PromptSession.builder()
                .examId(examId)
                .participantId(participantId)
                .specId(specId)
                .totalTokens(0)
                .startedAt(LocalDateTime.now())
                .build();
        return promptSessionRepository.save(session);
    }

    /**
     * 세션 조회 (별도 트랜잭션으로 즉시 커밋)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public PromptSession findByIdWithNewTransaction(Long id) {
        return promptSessionRepository.findById(id)
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus._NOT_FOUND));
    }
}
