package com.yd.vibecode.domain.chat.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.chat.application.dto.request.UpdateTokenUsageRequest;
import com.yd.vibecode.domain.chat.domain.entity.PromptSession;
import com.yd.vibecode.domain.chat.domain.service.PromptSessionService;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;

import lombok.RequiredArgsConstructor;

/**
 * AI usage 콜백을 받아 토큰 사용량 업데이트
 * - PromptSession에 토큰 누적
 * - ExamParticipant.tokenUsed 업데이트
 */
@Service
@RequiredArgsConstructor
public class UpdateTokenUsageUseCase {

    private final PromptSessionService promptSessionService;
    private final ExamParticipantService examParticipantService;

    @Transactional
    public void execute(UpdateTokenUsageRequest request) {
        // 1. 세션 조회
        PromptSession session = promptSessionService.findByExamIdAndParticipantId(
                request.examId(), request.participantId());

        if (session != null) {
            // 2. 세션에 토큰 누적
            promptSessionService.addTokens(session.getId(), request.tokens());
        }

        // 3. ExamParticipant의 tokenUsed 업데이트 (토큰 한도 체크용)
        examParticipantService.addTokenUsed(request.examId(), request.participantId(), request.tokens());
    }
}
