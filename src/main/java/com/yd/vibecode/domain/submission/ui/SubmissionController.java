package com.yd.vibecode.domain.submission.ui;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.submission.application.dto.request.SubmitRequest;
import com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse;
import com.yd.vibecode.domain.submission.application.dto.response.SubmitResponse;
import com.yd.vibecode.domain.submission.application.usecase.GetSubmissionDetailUseCase;
import com.yd.vibecode.domain.submission.application.usecase.SubmitUseCase;
import com.yd.vibecode.global.annotation.CurrentUser;
import com.yd.vibecode.global.common.BaseResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Submission Controller
 * - POST /api/exams/{examId}/submissions: 코드 제출
 * - GET /api/submissions/{submissionId}: 제출 상세 조회
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SubmissionController {

    private final SubmitUseCase submitUseCase;
    private final GetSubmissionDetailUseCase getSubmissionDetailUseCase;

    @PostMapping("/exams/{examId}/submissions")
    @ResponseStatus(HttpStatus.ACCEPTED)  // 202 Accepted
    public BaseResponse<SubmitResponse> submit(
            @PathVariable Long examId,
            @CurrentUser Long userId,
            @Valid @RequestBody SubmitRequest request) {
        SubmitResponse response = submitUseCase.execute(examId, userId, request);
        return BaseResponse.onSuccess(response);
    }

    @GetMapping("/submissions/{submissionId}")
    public BaseResponse<SubmissionDetailResponse> getSubmissionDetail(
            @PathVariable Long submissionId) {
        SubmissionDetailResponse response = getSubmissionDetailUseCase.execute(submissionId);
        return BaseResponse.onSuccess(response);
    }
}
