package com.yd.vibecode.domain.chat.ui;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.submission.application.dto.request.AISubmissionStatusRequest;
import com.yd.vibecode.domain.submission.application.dto.request.ScoringResultRequest;
import com.yd.vibecode.domain.submission.application.usecase.ReceiveScoringResultUseCase;
import com.yd.vibecode.domain.submission.application.usecase.UpdateSubmissionStatusUseCase;
import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.swagger.AICallbackApi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI Callback Controller
 * - POST /api/callbacks/ai/analysis: 진행/실패 상태만 (RUNNING, FAILED 권장)
 * - POST /api/callbacks/ai/submissions/{submissionId}/result: 채점 결과 (runs, scores, SSE)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/callbacks/ai")
public class AICallbackController implements AICallbackApi {

    private final UpdateSubmissionStatusUseCase updateSubmissionStatusUseCase;
    private final ReceiveScoringResultUseCase receiveScoringResultUseCase;

    @Override
    @PostMapping("/analysis")
    public BaseResponse<Void> receiveAnalysisResult(@Valid @RequestBody AISubmissionStatusRequest request) {
        log.info("[AI Callback] POST /analysis — submissionId={}, status={} (DB status only, no SSE)",
                request.submissionId(), request.status());
        updateSubmissionStatusUseCase.execute(request.submissionId(), request.status());
        return BaseResponse.onSuccess();
    }

    @Override
    @PostMapping("/submissions/{submissionId}/result")
    public BaseResponse<Void> receiveScoringResult(
            @PathVariable Long submissionId,
            @Valid @RequestBody ScoringResultRequest request) {
        int testCaseCount = request.testCases() != null ? request.testCases().size() : 0;
        log.info("[AI Callback] POST /submissions/{}/result — status={}, testCases={}, hasScore={}",
                submissionId, request.status(), testCaseCount, request.score() != null);
        receiveScoringResultUseCase.execute(submissionId, request);
        log.info("[AI Callback] result 처리 완료 — submissionId={}, status={}", submissionId, request.status());
        return BaseResponse.onSuccess();
    }
}
