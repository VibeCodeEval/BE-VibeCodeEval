package com.yd.vibecode.domain.chat.application.dto.request;

import java.util.Map;

import lombok.Builder;

/**
 * AI 서버로 메시지 전송 요청 DTO
 * 
 * 사용처:
 * - BE → AI 서버: POST /api/chat/messages
 * - AIChatService.sendMessage()에서 사용
 * - SendMessageUseCase에서 AI 서버 호출 시 사용
 */
@Builder
public record AISendMessageRequest(
    Long sessionId,
    Long participantId,        // 추가: 사용자 식별을 위해 필요
    Integer turnId,
    String role,
    String content,
    Map<String, Object> context
) {
}
