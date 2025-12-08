package com.yd.vibecode.domain.chat.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 토큰 사용량 업데이트 요청 DTO
 * 
 * 사용처:
 * - FE → BE: POST /api/chat/tokens/update
 * - ChatController.updateTokenUsage()에서 사용
 * - UpdateTokenUsageUseCase.execute()에서 사용
 * - AI 콜백에서 토큰 사용량 업데이트 시 사용
 */
public record UpdateTokenUsageRequest(
    @Schema(description = "시험 ID", example = "1", required = true)
    @NotNull(message = "examId는 필수입니다")
    Long examId,

    @Schema(description = "참가자 ID", example = "1", required = true)
    @NotNull(message = "participantId는 필수입니다")
    Long participantId,

    @Schema(description = "토큰 사용량", example = "1500", required = true)
    @NotNull(message = "tokens는 필수입니다")
    Integer tokens
) {
}
