package com.yd.vibecode.domain.admin.application.dto.response;

import java.time.LocalDateTime;

import com.yd.vibecode.domain.admin.domain.entity.PlatformSettings;

public record MasterPlatformSettingsResponse(
        int logRetentionDays,
        int submissionRetentionDays,
        boolean autoDeleteExpiredData,
        LocalDateTime updatedAt
) {
    public static MasterPlatformSettingsResponse from(PlatformSettings settings) {
        return new MasterPlatformSettingsResponse(
                settings.getLogRetentionDays(),
                settings.getSubmissionRetentionDays(),
                Boolean.TRUE.equals(settings.getAutoDeleteExpiredData()),
                settings.getUpdatedAt() != null ? settings.getUpdatedAt() : settings.getCreatedAt()
        );
    }
}
