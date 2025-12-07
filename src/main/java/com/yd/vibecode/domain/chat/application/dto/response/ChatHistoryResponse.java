package com.yd.vibecode.domain.chat.application.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 채팅 히스토리 응답 DTO
 * 
 * 사용처:
 * - BE → FE: GET /api/chat/history 응답
 * - ChatController.getChatHistory()에서 사용
 * - GetChatHistoryUseCase.execute()에서 사용
 */
@Schema(description = "채팅 히스토리 응답")
public record ChatHistoryResponse(
    @Schema(description = "세션 ID", example = "1")
    Long sessionId,
    
    @Schema(description = "시험 ID", example = "1")
    Long examId,
    
    @Schema(description = "참가자 ID", example = "1")
    Long participantId,
    
    @Schema(description = "총 토큰 사용량", example = "5000")
    Integer totalTokens,
    
    @Schema(description = "메시지 목록")
    List<MessageInfo> messages
) {
    @Schema(description = "메시지 정보")
    public record MessageInfo(
        @Schema(description = "메시지 ID", example = "1")
        Long id,
        
        @Schema(description = "턴 번호", example = "1")
        Integer turn,
        
        @Schema(description = "역할 (user, assistant)", example = "user")
        String role,
        
        @Schema(description = "메시지 내용", example = "안녕하세요")
        String content,
        
        @Schema(description = "토큰 사용량", example = "100")
        Integer tokenCount
    ) {
    }
}
