package com.yd.vibecode.infrastructure.database;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PostgreSQL ENUM 타입 초기화
 * Hibernate는 ENUM 타입을 자동 생성하지 않으므로, ApplicationRunner로 직접 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(1) // 다른 Initializer보다 먼저 실행되도록 설정
public class EnumTypeInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("[EnumTypeInitializer] ENUM 타입 초기화 시작");

        // 스키마 생성 (이미 존재하면 무시)
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS ai_vibe_coding_test");

        // ENUM 타입 생성 (이미 존재하면 무시)
        createEnumIfNotExists("ai_vibe_coding_test.difficulty_enum", 
                "'EASY', 'MEDIUM', 'HARD'");
        createEnumIfNotExists("ai_vibe_coding_test.problem_status_enum", 
                "'DRAFT', 'REVIEW', 'PUBLISHED', 'ARCHIVED'");
        createEnumIfNotExists("ai_vibe_coding_test.exam_state_enum", 
                "'WAITING', 'RUNNING', 'ENDED'");
        createEnumIfNotExists("ai_vibe_coding_test.prompt_role_enum", 
                "'USER', 'AI'");
        createEnumIfNotExists("ai_vibe_coding_test.submission_status_enum", 
                "'QUEUED', 'RUNNING', 'DONE', 'FAILED'");
        createEnumIfNotExists("ai_vibe_coding_test.run_grp_enum", 
                "'SAMPLE', 'PUBLIC', 'PRIVATE'");
        createEnumIfNotExists("ai_vibe_coding_test.verdict_enum", 
                "'AC', 'WA', 'TLE', 'MLE', 'RE'");
        createEnumIfNotExists("ai_vibe_coding_test.admin_role_enum", 
                "'ADMIN', 'MASTER'");
        createEnumIfNotExists("ai_vibe_coding_test.evaluation_type_enum", 
                "'TURN_EVAL', 'HOLISTIC_FLOW'");

        log.info("[EnumTypeInitializer] ENUM 타입 초기화 완료");
    }

    /**
     * ENUM 타입이 존재하지 않으면 생성
     */
    private void createEnumIfNotExists(String enumName, String values) {
        try {
            // ENUM 타입 존재 여부 확인
            String enumTypeName = enumName.split("\\.")[1];
            String checkSql = "SELECT 1 FROM pg_type WHERE typname = ? AND typnamespace = " +
                    "(SELECT oid FROM pg_namespace WHERE nspname = 'ai_vibe_coding_test')";

            try {
                Integer exists = jdbcTemplate.queryForObject(checkSql, Integer.class, enumTypeName);
                if (exists != null && exists == 1) {
                    log.debug("[EnumTypeInitializer] ENUM 타입 이미 존재: {}", enumName);
                    return;
                }
            } catch (EmptyResultDataAccessException e) {
                // 결과가 없으면 타입이 존재하지 않음 - 계속 진행
                log.debug("[EnumTypeInitializer] ENUM 타입 존재하지 않음, 생성 진행: {}", enumName);
            }
            
            // ENUM 타입 생성
            String createSql = String.format(
                    "CREATE TYPE %s AS ENUM (%s)",
                    enumName, values
            );
            jdbcTemplate.execute(createSql);
            log.info("[EnumTypeInitializer] ENUM 타입 생성 완료: {}", enumName);
            
        } catch (DataAccessException e) {
            // 타입이 이미 존재하는 경우 무시
            String errorMessage = e.getMessage();
            if (errorMessage != null && 
                (errorMessage.contains("already exists") || 
                 errorMessage.contains("duplicate key value"))) {
                log.debug("[EnumTypeInitializer] ENUM 타입 이미 존재 (무시): {}", enumName);
            } else {
                log.warn("[EnumTypeInitializer] ENUM 타입 생성 중 오류 발생: {} - {}", 
                        enumName, errorMessage);
            }
        } catch (Exception e) {
            log.warn("[EnumTypeInitializer] ENUM 타입 생성 중 예상치 못한 오류 발생: {} - {}", 
                    enumName, e.getMessage());
        }
    }
}

