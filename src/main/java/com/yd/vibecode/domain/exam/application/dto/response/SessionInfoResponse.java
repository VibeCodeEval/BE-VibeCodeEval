package com.yd.vibecode.domain.exam.application.dto.response;

/**
 * 세션 정보 응답 DTO
 * - Auth 도메인의 EnterResponse, MeResponse에서 사용
 */
public record SessionInfoResponse(
    Long examParticipantId,
    Integer tokenLimit,
    Integer tokenUsed
) {
    public static SessionInfoResponse of(Long examParticipantId, Integer tokenLimit, Integer tokenUsed) {
        return new SessionInfoResponse(examParticipantId, tokenLimit, tokenUsed);
    }
}
