package com.yd.vibecode.domain.exam.ui;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.exam.application.dto.request.ExtendExamRequest;
import com.yd.vibecode.domain.exam.application.usecase.EndExamUseCase;
import com.yd.vibecode.domain.exam.application.usecase.ExtendExamUseCase;
import com.yd.vibecode.domain.exam.application.usecase.StartExamUseCase;
import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.swagger.AdminExamApi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 시험 관리 ADMIN Controller
 * - POST /api/admin/exams/{id}/start: 시험 시작
 * - POST /api/admin/exams/{id}/end: 시험 종료
 * - POST /api/admin/exams/{id}/extend: 시험 시간 연장
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/exams")
public class AdminExamController implements AdminExamApi {

    private final StartExamUseCase startExamUseCase;
    private final EndExamUseCase endExamUseCase;
    private final ExtendExamUseCase extendExamUseCase;

    @PostMapping("/{id}/start")
    public BaseResponse<Void> startExam(@PathVariable Long id) {
        startExamUseCase.execute(id);
        return BaseResponse.onSuccess();
    }

    @PostMapping("/{id}/end")
    public BaseResponse<Void> endExam(@PathVariable Long id) {
        endExamUseCase.execute(id);
        return BaseResponse.onSuccess();
    }

    @PostMapping("/{id}/extend")
    public BaseResponse<Void> extendExam(@PathVariable Long id, 
                                         @Valid @RequestBody ExtendExamRequest request) {
        extendExamUseCase.execute(id, request.minutes());
        return BaseResponse.onSuccess();
    }
}
