package com.yd.vibecode.domain.exam.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.exam.application.dto.response.ExamStateResponse;
import com.yd.vibecode.domain.exam.application.usecase.GetExamStateUseCase;
import com.yd.vibecode.global.common.BaseResponse;

import lombok.RequiredArgsConstructor;

/**
 * 시험 관련 USER Controller
 * - GET /api/exams/{examId}/state: 시험 상태 조회 (타이머 동기화용)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exams")
public class ExamController {

    private final GetExamStateUseCase getExamStateUseCase;

    @GetMapping("/{examId}/state")
    public BaseResponse<ExamStateResponse> getExamState(@PathVariable Long examId) {
        ExamStateResponse response = getExamStateUseCase.execute(examId);
        return BaseResponse.onSuccess(response);
    }
}
