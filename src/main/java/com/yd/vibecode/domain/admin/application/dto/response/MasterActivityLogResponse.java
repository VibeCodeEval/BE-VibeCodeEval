package com.yd.vibecode.domain.admin.application.dto.response;

import java.time.LocalDateTime;

import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLog;
import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLogType;

public record MasterActivityLogResponse(
        Long id,
        MasterActivityLogType type,
        String title,
        String message,
        LocalDateTime createdAt,
        Long targetAdminId
) {
    public static MasterActivityLogResponse from(MasterActivityLog log) {
        return new MasterActivityLogResponse(
                log.getId(),
                log.getType(),
                log.getTitle(),
                log.getMessage(),
                log.getCreatedAt(),
                log.getTargetAdminId()
        );
    }
}
