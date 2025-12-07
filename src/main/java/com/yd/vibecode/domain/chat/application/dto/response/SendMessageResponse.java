package com.yd.vibecode.domain.chat.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI 서버로부터 메시지 전송 응답 DTO
 * 
 * 사용처:
 * - AI 서버 → BE: POST /api/chat/messages 응답
 * - AIChatService.sendMessage()에서 AI 서버 응답 수신 시 사용
 * - SendMessageUseCase.execute()에서 AI 응답 반환 시 사용
 */
@Schema(description = "대화 메시지 전송 응답")
public record SendMessageResponse(
    @Schema(description = "세션 ID", example = "100")
    @JsonProperty("sessionId")
    Long sessionId,

    @Schema(description = "턴 번호", example = "2")
    @JsonProperty("turn")  // AI 서버는 "turn"으로 보내지만, BE에서는 turnId로 사용
    Integer turnId,

    @Schema(description = "역할", example = "AI")
    @JsonProperty("role")
    String role,

    @Schema(description = "AI 응답 내용", example = "DP는 점화식을 세워서...")
    @JsonProperty("content")
    String content,

    @Schema(description = "AI 응답 토큰 수 (completion_tokens)", example = "200")
    @JsonProperty("tokenCount")
    Integer tokenCount,

    @Schema(description = "전체 토큰 수 (사용자 질문 토큰 + AI 응답 토큰)", example = "350")
    @JsonProperty("totalCount")
    Integer totalCount
) {
}
