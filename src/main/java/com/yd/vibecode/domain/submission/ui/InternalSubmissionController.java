package com.yd.vibecode.domain.submission.ui;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.submission.application.dto.request.ScoringResultRequest;
import com.yd.vibecode.domain.submission.application.usecase.ReceiveScoringResultUseCase;
import com.yd.vibecode.global.common.BaseResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Internal API Controller (FastAPI용)
 * - POST /api/internal/submissions/{id}/result: 채점 결과 수신
 * 
 * 주의: 이 API는 내부 네트워크에서만 호출되어야 합니다.
 * 프로덕션에서는 IP 화이트리스트 또는 내부 토큰으로 보호 필요
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/submissions")
public class InternalSubmissionController {

    private final ReceiveScoringResultUseCase receiveScoringResultUseCase;

    @PostMapping("/{id}/result")
    public BaseResponse<Void> receiveScoringResult(
            @PathVariable Long id,
            @Valid @RequestBody ScoringResultRequest request) {
        receiveScoringResultUseCase.execute(id, request);
        return BaseResponse.onSuccess();
    }
}
