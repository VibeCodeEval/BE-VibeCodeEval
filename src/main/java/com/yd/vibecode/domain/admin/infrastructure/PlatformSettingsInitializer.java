package com.yd.vibecode.domain.admin.infrastructure;

import java.sql.SQLException;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.NestedExceptionUtils;
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

    /** PostgreSQL: undefined_table */
    private static final String SQLSTATE_UNDEFINED_TABLE = "42P01";

    private final PlatformSettingsService platformSettingsService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            platformSettingsService.getOrCreate();
            log.debug("[PlatformSettingsInitializer] Platform settings ready");
        } catch (DataAccessException e) {
            if (isPlatformSettingsTableMissing(e)) {
                log.warn("[PlatformSettingsInitializer] platform_settings table not yet created. Skipping init.");
            } else {
                log.error("[PlatformSettingsInitializer] Failed to initialize platform settings: {}",
                        e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("[PlatformSettingsInitializer] Unexpected error: {}", e.getMessage(), e);
        }
    }

    private static boolean isPlatformSettingsTableMissing(DataAccessException e) {
        Throwable cause = NestedExceptionUtils.getMostSpecificCause(e);
        if (cause instanceof SQLException sqlException) {
            return SQLSTATE_UNDEFINED_TABLE.equals(sqlException.getSQLState());
        }
        return false;
    }
}
