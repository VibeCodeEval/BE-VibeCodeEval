package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.admin.application.dto.response.MasterPlatformSettingsResponse;
import com.yd.vibecode.domain.admin.domain.entity.PlatformSettings;
import com.yd.vibecode.domain.admin.domain.service.PlatformSettingsService;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

@ExtendWith(MockitoExtension.class)
class GetMasterPlatformSettingsUseCaseTest {

    @InjectMocks
    private GetMasterPlatformSettingsUseCase getMasterPlatformSettingsUseCase;

    @Mock
    private AdminService adminService;

    @Mock
    private PlatformSettingsService platformSettingsService;

    @Test
    @DisplayName("MASTER는 플랫폼 설정을 조회할 수 있다")
    void execute_master_success() {
        Long masterId = 1L;
        given(adminService.findById(masterId)).willReturn(Admin.builder().role(AdminRole.MASTER).build());
        given(platformSettingsService.getOrCreate()).willReturn(PlatformSettings.builder()
                .defaultTokenLimit(10000)
                .logRetentionDays(90)
                .submissionRetentionDays(90)
                .autoDeleteExpiredData(true)
                .build());

        MasterPlatformSettingsResponse response = getMasterPlatformSettingsUseCase.execute(masterId);

        assertThat(response.logRetentionDays()).isEqualTo(90);
        assertThat(response.autoDeleteExpiredData()).isTrue();
    }

    @Test
    @DisplayName("일반 ADMIN은 MASTER_ONLY 예외")
    void execute_admin_forbidden() {
        given(adminService.findById(2L)).willReturn(Admin.builder().role(AdminRole.ADMIN).build());

        assertThatThrownBy(() -> getMasterPlatformSettingsUseCase.execute(2L))
                .isInstanceOf(RestApiException.class)
                .extracting(ex -> ((RestApiException) ex).getErrorCode().getCode())
                .isEqualTo(AuthErrorStatus.MASTER_ONLY.getCode().getCode());
    }
}
