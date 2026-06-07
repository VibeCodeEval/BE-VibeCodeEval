package com.yd.vibecode.domain.admin.infrastructure;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.yd.vibecode.domain.admin.application.usecase.PurgeExpiredPlatformDataUseCase;
import com.yd.vibecode.domain.admin.domain.entity.PlatformSettings;
import com.yd.vibecode.domain.admin.domain.service.PlatformSettingsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 플랫폼 전역 데이터 보관 정책에 따라 만료 데이터를 주기적으로 삭제한다.
 * {@code autoDeleteExpiredData=false}이면 아무 데이터도 삭제하지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataRetentionScheduler {

    private static final long PURGE_INTERVAL_MS = 86_400_000L;

    private final PlatformSettingsService platformSettingsService;
    private final PurgeExpiredPlatformDataUseCase purgeExpiredPlatformDataUseCase;

    @Scheduled(fixedDelay = PURGE_INTERVAL_MS)
    public void purgeExpiredData() {
        PlatformSettings settings = platformSettingsService.getOrCreate();
        if (!Boolean.TRUE.equals(settings.getAutoDeleteExpiredData())) {
            log.debug("[DataRetention] autoDeleteExpiredData=false; skipping purge");
            return;
        }

        try {
            purgeExpiredPlatformDataUseCase.execute(settings);
        } catch (Exception e) {
            log.error("[DataRetention] Failed to purge expired platform data", e);
        }
    }
}
