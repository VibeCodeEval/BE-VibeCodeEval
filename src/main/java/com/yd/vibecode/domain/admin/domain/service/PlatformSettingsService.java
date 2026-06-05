package com.yd.vibecode.domain.admin.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.admin.domain.entity.PlatformSettings;
import com.yd.vibecode.domain.admin.domain.repository.PlatformSettingsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlatformSettingsService {

    public static final int DEFAULT_TOKEN_LIMIT = 10_000;
    public static final int DEFAULT_LOG_RETENTION_DAYS = 90;
    public static final int DEFAULT_SUBMISSION_RETENTION_DAYS = 90;
    public static final boolean DEFAULT_AUTO_DELETE_EXPIRED_DATA = true;

    private final PlatformSettingsRepository platformSettingsRepository;

    @Transactional
    public PlatformSettings getOrCreate() {
        return platformSettingsRepository.findById(PlatformSettings.SINGLETON_ID)
                .orElseGet(this::createAndSaveDefaults);
    }

    @Transactional
    public PlatformSettings updateRetention(
            int logRetentionDays,
            int submissionRetentionDays,
            boolean autoDeleteExpiredData) {
        PlatformSettings settings = getOrCreate();
        settings.updateRetention(logRetentionDays, submissionRetentionDays, autoDeleteExpiredData);
        return platformSettingsRepository.save(settings);
    }

    @Transactional
    public PlatformSettings createAndSaveDefaults() {
        PlatformSettings defaults = PlatformSettings.builder()
                .id(PlatformSettings.SINGLETON_ID)
                .defaultTokenLimit(DEFAULT_TOKEN_LIMIT)
                .logRetentionDays(DEFAULT_LOG_RETENTION_DAYS)
                .submissionRetentionDays(DEFAULT_SUBMISSION_RETENTION_DAYS)
                .autoDeleteExpiredData(DEFAULT_AUTO_DELETE_EXPIRED_DATA)
                .build();
        return platformSettingsRepository.save(defaults);
    }
}
