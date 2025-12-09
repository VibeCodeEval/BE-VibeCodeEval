package com.yd.vibecode.domain.chat.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 채팅 메시지 저장 요청 DTO
 * 
 * 사용처:
 * - FE → BE: POST /api/chat/messages
 * - ChatController.saveChatMessage()에서 사용
 * - SaveChatMessageUseCase.execute()에서 사용
 * 
 * 참고:
 * - sessionId가 제공되면 해당 세션 사용, 없으면 examId와 participantId로 세션 조회/생성
 */
public record SaveChatMessageRequest(
    @Schema(description = "세션 ID (선택)", example = "1")
    Long sessionId,

    @Schema(description = "시험 ID", example = "1", required = true)
    @NotNull(message = "examId는 필수입니다")
    Long examId,

    @Schema(description = "참가자 ID", example = "1", required = true)
    @NotNull(message = "participantId는 필수입니다")
    Long participantId,

    @Schema(description = "턴 번호", example = "1", required = true)
    @NotNull(message = "turn은 필수입니다")
    Integer turn,

    @Schema(description = "역할 (user, assistant)", example = "user", required = true)
    @NotBlank(message = "role은 필수입니다")
    String role,

    @Schema(description = "메시지 내용", example = "안녕하세요", required = true)
    @NotBlank(message = "content는 필수입니다")
    String content,

    @Schema(description = "토큰 사용량", example = "100")
    Integer tokenCount,

    @Schema(description = "메타데이터 (JSON 문자열)", example = "{\"key\": \"value\"}")
    String meta
) {
}
