package com.yd.vibecode.domain.admin.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.admin.domain.entity.PlatformSettings;
import com.yd.vibecode.domain.admin.domain.repository.PlatformSettingsRepository;

@ExtendWith(MockitoExtension.class)
class PlatformSettingsServiceTest {

    @InjectMocks
    private PlatformSettingsService platformSettingsService;

    @Mock
    private PlatformSettingsRepository platformSettingsRepository;

    @Test
    @DisplayName("설정 row가 없으면 기본값으로 생성한다")
    void getOrCreate_createsDefaultsWhenMissing() {
        given(platformSettingsRepository.findById(PlatformSettings.SINGLETON_ID)).willReturn(Optional.empty());
        given(platformSettingsRepository.save(any(PlatformSettings.class))).willAnswer(inv -> inv.getArgument(0));

        PlatformSettings result = platformSettingsService.getOrCreate();

        assertThat(result.getDefaultTokenLimit()).isEqualTo(PlatformSettingsService.DEFAULT_TOKEN_LIMIT);
        assertThat(result.getLogRetentionDays()).isEqualTo(PlatformSettingsService.DEFAULT_LOG_RETENTION_DAYS);
        assertThat(result.getSubmissionRetentionDays()).isEqualTo(PlatformSettingsService.DEFAULT_SUBMISSION_RETENTION_DAYS);
        assertThat(result.getAutoDeleteExpiredData()).isTrue();
        verify(platformSettingsRepository).save(any(PlatformSettings.class));
    }

    @Test
    @DisplayName("설정을 업데이트한다")
    void update_persistsNewValues() {
        PlatformSettings existing = PlatformSettings.builder()
                .defaultTokenLimit(10000)
                .logRetentionDays(90)
                .submissionRetentionDays(90)
                .autoDeleteExpiredData(true)
                .build();

        given(platformSettingsRepository.findById(PlatformSettings.SINGLETON_ID)).willReturn(Optional.of(existing));
        given(platformSettingsRepository.save(existing)).willReturn(existing);

        PlatformSettings updated = platformSettingsService.updateRetention(30, 180, false);

        assertThat(updated.getDefaultTokenLimit()).isEqualTo(10000);
        assertThat(updated.getLogRetentionDays()).isEqualTo(30);
        assertThat(updated.getSubmissionRetentionDays()).isEqualTo(180);
        assertThat(updated.getAutoDeleteExpiredData()).isFalse();
    }
}
