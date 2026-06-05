package com.yd.vibecode.domain.admin.application.usecase;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.application.dto.request.UpdateMasterPlatformSettingsRequest;
import com.yd.vibecode.domain.admin.application.dto.response.MasterPlatformSettingsResponse;
import com.yd.vibecode.domain.admin.domain.entity.PlatformSettings;
import com.yd.vibecode.domain.admin.domain.service.MasterActivityLogService;
import com.yd.vibecode.domain.admin.domain.service.PlatformSettingsService;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateMasterPlatformSettingsUseCase {

    private final AdminService adminService;
    private final PlatformSettingsService platformSettingsService;
    private final MasterActivityLogService masterActivityLogService;

    @Transactional
    public MasterPlatformSettingsResponse execute(Long requesterId, UpdateMasterPlatformSettingsRequest request) {
        Admin requester = adminService.findById(requesterId);
        if (!requester.isMaster()) {
            throw new RestApiException(AuthErrorStatus.MASTER_ONLY);
        }

        PlatformSettings before = platformSettingsService.getOrCreate();
        boolean changed = hasChanges(before, request);

        PlatformSettings updated = platformSettingsService.updateRetention(
                request.logRetentionDays(),
                request.submissionRetentionDays(),
                request.autoDeleteExpiredData());

        if (changed) {
            masterActivityLogService.logPlatformSettingsUpdated(requesterId);
        }

        return MasterPlatformSettingsResponse.from(updated);
    }

    private static boolean hasChanges(PlatformSettings before, UpdateMasterPlatformSettingsRequest request) {
        boolean beforeAutoDelete = Boolean.TRUE.equals(before.getAutoDeleteExpiredData());
        return !Objects.equals(before.getLogRetentionDays(), request.logRetentionDays())
                || !Objects.equals(before.getSubmissionRetentionDays(), request.submissionRetentionDays())
                || beforeAutoDelete != request.autoDeleteExpiredData();
    }
}
