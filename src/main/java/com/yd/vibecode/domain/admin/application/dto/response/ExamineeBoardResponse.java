package com.yd.vibecode.domain.admin.application.dto.response;

import com.yd.vibecode.domain.auth.domain.entity.ExamParticipant;
import com.yd.vibecode.domain.auth.domain.entity.User;

public record ExamineeBoardResponse(
    Long examParticipantId,
    String name,
    String phoneMasked,
    String state,
    Integer tokenLimit,
    Long tokenUsed,
    Boolean submitted
) {
    public static ExamineeBoardResponse of(ExamParticipant ep, User p, Boolean submitted) {
        return new ExamineeBoardResponse(
            ep.getId(),
            p.getName(),
            maskPhone(p.getPhone()),
            ep.getState(),
            ep.getTokenLimit(),
            ep.getTokenUsed().longValue(),
            submitted
        );
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 9) return phone;
        // 010-1234-5678 -> 010-****-5678
        String[] parts = phone.split("-");
        if (parts.length == 3) {
            return parts[0] + "-****-" + parts[2];
        }
        return phone;
    }
}
