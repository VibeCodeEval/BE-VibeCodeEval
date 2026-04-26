package com.yd.vibecode.domain.exam.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.exam.application.dto.response.ExamStateResponse;
import com.yd.vibecode.domain.exam.application.dto.response.ParticipantSessionResponse;
import com.yd.vibecode.domain.exam.application.usecase.GetExamStateUseCase;
import com.yd.vibecode.domain.exam.application.usecase.GetParticipantSessionUseCase;
import com.yd.vibecode.global.annotation.CurrentUser;
import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.swagger.ExamApi;

import lombok.RequiredArgsConstructor;

/**
 * 시험 관련 USER Controller
 * - GET /api/exams/{examId}/state: 시험 상태 조회 (타이머 동기화용)
 * - GET /api/exams/{examId}/participants/me: 현재 참가자 세션 정보 조회
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exams")
public class ExamController implements ExamApi {

    private final GetExamStateUseCase getExamStateUseCase;
    private final GetParticipantSessionUseCase getParticipantSessionUseCase;

    @GetMapping("/{examId}/state")
    @Override
    public BaseResponse<ExamStateResponse> getExamState(@PathVariable Long examId) {
        ExamStateResponse response = getExamStateUseCase.execute(examId);
        return BaseResponse.onSuccess(response);
    }

    @GetMapping("/{examId}/participants/me")
    public BaseResponse<ParticipantSessionResponse> getMySession(
            @PathVariable Long examId,
            @CurrentUser String userId) {
        ParticipantSessionResponse response = getParticipantSessionUseCase.execute(examId, Long.parseLong(userId));
        return BaseResponse.onSuccess(response);
    }

}
