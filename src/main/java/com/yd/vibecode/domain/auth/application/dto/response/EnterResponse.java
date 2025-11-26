package com.yd.vibecode.domain.auth.application.dto.response;

public record EnterResponse(
    String accessToken,
    String role,
    ParticipantInfo participant,
    ExamInfo exam,
    SessionInfo session
) {
    public record ParticipantInfo(
        Long id,
        String name,
        String phone
    ) {
    }

    public record ExamInfo(
        Long id,
        String title,
        String state
    ) {
    }

    public record SessionInfo(
        Long examParticipantId,
        Integer tokenLimit,
        Integer tokenUsed
    ) {
    }
}
