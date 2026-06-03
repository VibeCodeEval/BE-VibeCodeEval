package com.yd.vibecode.domain.chat.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.chat.application.dto.response.SendMessageResponse;
import com.yd.vibecode.domain.chat.domain.service.PromptMessageService;
import com.yd.vibecode.domain.chat.domain.service.PromptSessionService;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;

import lombok.RequiredArgsConstructor;

/**
 * AI 응답 사후 저장 전용 컴포넌트.
 * AI HTTP 호출(무 트랜잭션) 이후의 DB 쓰기를 하나의 트랜잭션으로 묶어 원자성을 보장한다.
 */
@Component
@RequiredArgsConstructor
public class ChatResponsePersister {

    private final PromptMessageService promptMessageService;
    private final PromptSessionService promptSessionService;
    private final ExamParticipantService examParticipantService;

    @Transactional
    public void persistAiResponse(Long sessionId, Long examId, Long participantId,
                                  SendMessageResponse aiResponse) {
        Integer tokenCount = aiResponse.tokenCount();

        promptMessageService.create(
                sessionId,
                aiResponse.turnId(),
                aiResponse.role() != null ? aiResponse.role() : "AI",
                aiResponse.content() != null ? aiResponse.content() : "",
                tokenCount != null ? tokenCount : 0,
                null);

        if (tokenCount != null && tokenCount > 0) {
            promptSessionService.addTokens(sessionId, tokenCount);
            examParticipantService.addTokenUsed(examId, participantId, tokenCount);
        }
    }
}
