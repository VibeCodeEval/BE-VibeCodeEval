package com.yd.vibecode.domain.auth.application.dto.response;

import com.yd.vibecode.domain.exam.application.dto.response.ExamInfoResponse;
import com.yd.vibecode.domain.exam.application.dto.response.SessionInfoResponse;

public record EnterResponse(
    String accessToken,
    String role,
    ParticipantInfo participant,
    ExamInfoResponse exam,
    SessionInfoResponse session
) {
    public record ParticipantInfo(
        Long id,
        String name,
        String phone
    ) {
    }
}
