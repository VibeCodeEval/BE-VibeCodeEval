package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.admin.application.dto.request.UpdateMasterPlatformSettingsRequest;
import com.yd.vibecode.domain.admin.domain.entity.PlatformSettings;
import com.yd.vibecode.domain.admin.domain.service.MasterActivityLogService;
import com.yd.vibecode.domain.admin.domain.service.PlatformSettingsService;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;
import com.yd.vibecode.domain.auth.domain.service.AdminService;

@ExtendWith(MockitoExtension.class)
class UpdateMasterPlatformSettingsUseCaseTest {

    @InjectMocks
    private UpdateMasterPlatformSettingsUseCase updateMasterPlatformSettingsUseCase;

    @Mock
    private AdminService adminService;

    @Mock
    private PlatformSettingsService platformSettingsService;

    @Mock
    private MasterActivityLogService masterActivityLogService;

    @Test
    @DisplayName("설정 변경 시 PLATFORM_SETTINGS_UPDATED 로그를 기록한다")
    void execute_logsWhenChanged() {
        Long masterId = 1L;
        UpdateMasterPlatformSettingsRequest request = new UpdateMasterPlatformSettingsRequest(
                30, 180, false);

        PlatformSettings before = PlatformSettings.builder()
                .defaultTokenLimit(10000)
                .logRetentionDays(90)
                .submissionRetentionDays(90)
                .autoDeleteExpiredData(true)
                .build();
        PlatformSettings after = PlatformSettings.builder()
                .defaultTokenLimit(10000)
                .logRetentionDays(30)
                .submissionRetentionDays(180)
                .autoDeleteExpiredData(false)
                .build();

        given(adminService.findById(masterId)).willReturn(Admin.builder().role(AdminRole.MASTER).build());
        given(platformSettingsService.getOrCreate()).willReturn(before);
        given(platformSettingsService.updateRetention(30, 180, false)).willReturn(after);

        var response = updateMasterPlatformSettingsUseCase.execute(masterId, request);

        assertThat(response.logRetentionDays()).isEqualTo(30);
        verify(masterActivityLogService).logPlatformSettingsUpdated(masterId);
    }

    @Test
    @DisplayName("설정 값이 동일하면 로그를 기록하지 않는다")
    void execute_noLogWhenUnchanged() {
        Long masterId = 1L;
        UpdateMasterPlatformSettingsRequest request = new UpdateMasterPlatformSettingsRequest(
                90, 90, true);

        PlatformSettings same = PlatformSettings.builder()
                .defaultTokenLimit(10000)
                .logRetentionDays(90)
                .submissionRetentionDays(90)
                .autoDeleteExpiredData(true)
                .build();

        given(adminService.findById(masterId)).willReturn(Admin.builder().role(AdminRole.MASTER).build());
        given(platformSettingsService.getOrCreate()).willReturn(same);
        given(platformSettingsService.updateRetention(90, 90, true)).willReturn(same);

        updateMasterPlatformSettingsUseCase.execute(masterId, request);

        verify(masterActivityLogService, never()).logPlatformSettingsUpdated(masterId);
    }
}
