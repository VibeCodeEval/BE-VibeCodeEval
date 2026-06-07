package com.yd.vibecode.domain.admin.infrastructure;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.admin.application.usecase.PurgeExpiredPlatformDataUseCase;
import com.yd.vibecode.domain.admin.domain.entity.PlatformSettings;
import com.yd.vibecode.domain.admin.domain.service.PlatformSettingsService;

@ExtendWith(MockitoExtension.class)
class DataRetentionSchedulerTest {

    @InjectMocks
    private DataRetentionScheduler scheduler;

    @Mock
    private PlatformSettingsService platformSettingsService;

    @Mock
    private PurgeExpiredPlatformDataUseCase purgeExpiredPlatformDataUseCase;

    @Test
    @DisplayName("autoDeleteExpiredData=false이면 PurgeExpiredPlatformDataUseCase를 호출하지 않는다")
    void purgeExpiredData_skipsWhenAutoDeleteDisabled() {
        PlatformSettings settings = PlatformSettings.builder()
                .defaultTokenLimit(10000)
                .logRetentionDays(90)
                .submissionRetentionDays(90)
                .autoDeleteExpiredData(false)
                .build();
        given(platformSettingsService.getOrCreate()).willReturn(settings);

        scheduler.purgeExpiredData();

        verifyNoInteractions(purgeExpiredPlatformDataUseCase);
    }

    @Test
    @DisplayName("autoDeleteExpiredData=true이면 PurgeExpiredPlatformDataUseCase를 호출한다")
    void purgeExpiredData_invokesUseCaseWhenAutoDeleteEnabled() {
        PlatformSettings settings = PlatformSettings.builder()
                .defaultTokenLimit(10000)
                .logRetentionDays(30)
                .submissionRetentionDays(60)
                .autoDeleteExpiredData(true)
                .build();
        given(platformSettingsService.getOrCreate()).willReturn(settings);

        scheduler.purgeExpiredData();

        verify(purgeExpiredPlatformDataUseCase).execute(settings);
    }

    @Test
    @DisplayName("UseCase 예외가 발생해도 scheduler 메서드는 예외를 던지지 않는다")
    void purgeExpiredData_swallowsUseCaseException() {
        PlatformSettings settings = PlatformSettings.builder()
                .defaultTokenLimit(10000)
                .logRetentionDays(90)
                .submissionRetentionDays(90)
                .autoDeleteExpiredData(true)
                .build();
        given(platformSettingsService.getOrCreate()).willReturn(settings);
        org.mockito.BDDMockito.willThrow(new RuntimeException("purge failed"))
                .given(purgeExpiredPlatformDataUseCase)
                .execute(settings);

        scheduler.purgeExpiredData();

        verify(purgeExpiredPlatformDataUseCase).execute(settings);
    }
}
