package com.yd.vibecode.domain.chat.ui;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.submission.application.dto.request.AISubmissionStatusRequest;
import com.yd.vibecode.domain.submission.application.usecase.UpdateSubmissionStatusUseCase;
import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.swagger.AICallbackApi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * AI Callback Controller
 * - AI 서버로부터 제출 상태 업데이트 수신
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/callbacks/ai")
public class AICallbackController implements AICallbackApi {

    private final UpdateSubmissionStatusUseCase updateSubmissionStatusUseCase;

    @Override
    @PostMapping("/analysis")
    public BaseResponse<Void> receiveAnalysisResult(@Valid @RequestBody AISubmissionStatusRequest request) {
        updateSubmissionStatusUseCase.execute(request.submissionId(), request.status());
        return BaseResponse.onSuccess();
    }
}
