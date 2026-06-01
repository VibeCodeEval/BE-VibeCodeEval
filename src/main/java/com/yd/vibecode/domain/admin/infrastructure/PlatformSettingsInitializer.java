package com.yd.vibecode.domain.admin.infrastructure;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.yd.vibecode.domain.admin.domain.service.PlatformSettingsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3)
public class PlatformSettingsInitializer implements ApplicationRunner {

    private final PlatformSettingsService platformSettingsService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            platformSettingsService.getOrCreate();
            log.debug("[PlatformSettingsInitializer] Platform settings ready");
        } catch (DataAccessException e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("does not exist")) {
                log.warn("[PlatformSettingsInitializer] platform_settings table not yet created. Skipping init.");
            } else {
                log.error("[PlatformSettingsInitializer] Failed to initialize platform settings: {}", errorMessage, e);
            }
        } catch (Exception e) {
            log.error("[PlatformSettingsInitializer] Unexpected error: {}", e.getMessage(), e);
        }
    }
}
