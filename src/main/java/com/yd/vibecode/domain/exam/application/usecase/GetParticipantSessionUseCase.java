package com.yd.vibecode.domain.exam.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.exam.application.dto.response.ParticipantSessionResponse;
import com.yd.vibecode.domain.exam.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.exam.domain.service.ExamParticipantService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.ExamErrorStatus;

import lombok.RequiredArgsConstructor;

/**
 * 현재 로그인한 참가자의 세션 정보 조회 UseCase
 * GET /api/exams/{examId}/participants/me
 */
@Service
@RequiredArgsConstructor
public class GetParticipantSessionUseCase {

    private final ExamParticipantService examParticipantService;

    @Transactional(readOnly = true)
    public ParticipantSessionResponse execute(Long examId, Long participantId) {
        ExamParticipant participant = examParticipantService.findByExamIdAndParticipantId(examId, participantId);
        if (participant == null) {
            throw new RestApiException(ExamErrorStatus.PARTICIPANT_NOT_FOUND);
        }
        return ParticipantSessionResponse.from(participant);
    }
}
