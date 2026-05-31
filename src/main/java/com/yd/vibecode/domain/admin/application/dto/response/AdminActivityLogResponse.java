package com.yd.vibecode.domain.admin.application.dto.response;

import java.time.LocalDateTime;

import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLog;
import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLogType;

public record AdminActivityLogResponse(
        Long id,
        AdminActivityLogType type,
        String title,
        String message,
        LocalDateTime createdAt,
        Long examId,
        Long participantId
) {
    public static AdminActivityLogResponse from(AdminActivityLog log) {
        return new AdminActivityLogResponse(
                log.getId(),
                log.getType(),
                log.getTitle(),
                log.getMessage(),
                log.getCreatedAt(),
                log.getExamId(),
                log.getParticipantId()
        );
    }
}
