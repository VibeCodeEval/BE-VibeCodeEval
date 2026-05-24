package com.yd.vibecode.domain.submission.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.submission.application.dto.request.SubmitRequest;
import com.yd.vibecode.domain.submission.application.dto.response.SubmitResponse;
import com.yd.vibecode.domain.submission.domain.service.ParticipantSubmitOrchestrationService;

import lombok.RequiredArgsConstructor;

/**
 * 제출 UseCase — 사용자 직접 제출 (공통 오케스트레이션 위임)
 */
@Service
@RequiredArgsConstructor
public class SubmitUseCase {

    private final ParticipantSubmitOrchestrationService participantSubmitOrchestrationService;

    @Transactional
    public SubmitResponse execute(Long examId, Long userId, SubmitRequest request) {
        return participantSubmitOrchestrationService.submitOrThrow(examId, userId, request);
    }
}
