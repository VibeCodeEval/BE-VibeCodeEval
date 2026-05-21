package com.yd.vibecode.domain.auth.application.dto.response;

import com.yd.vibecode.domain.exam.application.dto.response.ExamInfoResponse;
import com.yd.vibecode.domain.exam.application.dto.response.SessionInfoResponse;

public record MeResponse(
    String role,
    ParticipantInfo participant,
    ExamInfoResponse exam,
    SessionInfoResponse session
) {
    public record ParticipantInfo(
        Long id,
        /** 표시용 이름 (displayName 없으면 adminNumber 등 BE fallback) */
        String name,
        String phone,
        /** ADMIN 전용. USER는 null */
        String adminNumber,
        /** ADMIN 전용 원본 display_name (nullable). USER는 null */
        String displayName
    ) {
    }
}
