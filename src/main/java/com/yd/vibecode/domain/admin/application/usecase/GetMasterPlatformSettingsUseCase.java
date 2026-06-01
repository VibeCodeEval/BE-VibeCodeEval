package com.yd.vibecode.domain.admin.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.application.dto.response.MasterPlatformSettingsResponse;
import com.yd.vibecode.domain.admin.domain.service.PlatformSettingsService;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMasterPlatformSettingsUseCase {

    private final AdminService adminService;
    private final PlatformSettingsService platformSettingsService;

    @Transactional
    public MasterPlatformSettingsResponse execute(Long requesterId) {
        Admin requester = adminService.findById(requesterId);
        if (!requester.isMaster()) {
            throw new RestApiException(AuthErrorStatus.MASTER_ONLY);
        }

        return MasterPlatformSettingsResponse.from(platformSettingsService.getOrCreate());
    }
}
