package com.yd.vibecode.domain.submission.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AI 서버 제출 평가 요청 DTO
 * - BE → AI 서버: /api/session/submit
 */
@Schema(description = "AI 서버 제출 평가 요청")
public record AISubmitEvaluationRequest(
    @Schema(description = "참가자 ID", example = "9001", required = true)
    @NotNull(message = "participantId는 필수입니다")
    Long participantId,

    @Schema(description = "문제 ID", example = "1", required = true)
    @NotNull(message = "problemId는 필수입니다")
    Long problemId,

    @Schema(description = "스펙 버전 ID", example = "1", required = true)
    @NotNull(message = "specVersion은 필수입니다")
    Long specVersion,

    @Schema(description = "최종 제출 코드", example = "def solution(): ...", required = true)
    @NotBlank(message = "finalCode는 필수입니다")
    String finalCode,

    @Schema(description = "프로그래밍 언어", example = "python", required = true)
    @NotBlank(message = "language는 필수입니다")
    String language,

    @Schema(description = "제출 ID", example = "88001", required = true)
    @NotNull(message = "submissionId는 필수입니다")
    Long submissionId
) {
}

