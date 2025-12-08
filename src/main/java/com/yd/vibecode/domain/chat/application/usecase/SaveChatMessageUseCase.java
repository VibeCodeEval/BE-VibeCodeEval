package com.yd.vibecode.domain.chat.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.chat.application.dto.request.SaveChatMessageRequest;
import com.yd.vibecode.domain.chat.domain.entity.PromptSession;
import com.yd.vibecode.domain.chat.domain.service.PromptMessageService;
import com.yd.vibecode.domain.chat.domain.service.PromptSessionService;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaveChatMessageUseCase {

    private final PromptSessionService promptSessionService;
    private final PromptMessageService promptMessageService;
    private final ExamParticipantService examParticipantService;

    @Transactional
    public void execute(SaveChatMessageRequest request) {
        // 1. ExamParticipant 조회하여 specId 가져오기
        ExamParticipant examParticipant = examParticipantService.findByExamIdAndParticipantId(
                request.examId(), request.participantId());

        if (examParticipant == null) {
            throw new IllegalArgumentException("ExamParticipant not found");
        }

        // 2. 세션 가져오기 또는 생성
        PromptSession session = promptSessionService.getOrCreateSession(
                request.examId(), request.participantId(), examParticipant.getSpecId());

        // 3. 메시지 저장
        promptMessageService.create(
                session.getId(),
                request.turn(),
                request.role(),
                request.content(),
                request.tokenCount(),
                request.meta()
        );

        // 4. 토큰 카운트가 있으면 세션에 누적
        if (request.tokenCount() != null && request.tokenCount() > 0) {
            promptSessionService.addTokens(session.getId(), request.tokenCount());
        }
    }
}
