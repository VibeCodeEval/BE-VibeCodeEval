package com.yd.vibecode.domain.submission.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * AI 제출 상태 콜백 요청 DTO
 * 
 * 사용처:
 * - AI 서버 → BE: POST /api/callbacks/ai/analysis
 * - AICallbackController.receiveAnalysisResult()에서 사용
 * - 제출 평가 완료 시 상태만 전달
 */
@Schema(description = "AI 제출 상태 콜백 요청")
public record AISubmissionStatusRequest(
    @Schema(description = "제출 ID", example = "88001", required = true)
    @NotNull(message = "submissionId는 필수입니다")
    Long submissionId,

    @Schema(description = "제출 상태 (DONE, FAILED 등)", example = "DONE", required = true)
    @NotNull(message = "status는 필수입니다")
    String status
) {
}

