package com.yd.vibecode.domain.chat.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 채팅 메시지 전송 요청 DTO
 * 
 * 사용처:
 * - FE → BE: POST /api/exams/{examId}/chat/messages
 * - ExamController.sendChatMessage()에서 사용
 * - SendMessageUseCase.execute()에서 사용
 * 
 * 규칙:
 * - examId: path variable과 일치해야 함 (컨트롤러에서 검증)
 * - participantId: JWT에서 추출한 값과 일치해야 함 (컨트롤러에서 검증)
 * - content: 사용자가 입력한 메시지 내용
 */
@Schema(description = "채팅 메시지 전송 요청")
public record SendChatMessageRequest(
    @Schema(description = "시험 ID", example = "1", required = true)
    @NotNull(message = "examId는 필수입니다")
    Long examId,

    @Schema(description = "참가자 ID", example = "1", required = true)
    @NotNull(message = "participantId는 필수입니다")
    Long participantId,

    @Schema(description = "메시지 내용", example = "DP로 푸는 법을 알려주세요", required = true)
    @NotBlank(message = "content는 필수입니다")
    String content
) {
}

