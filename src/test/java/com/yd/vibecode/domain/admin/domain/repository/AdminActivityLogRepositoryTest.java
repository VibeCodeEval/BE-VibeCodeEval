package com.yd.vibecode.domain.admin.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLog;
import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLogType;
import com.yd.vibecode.global.config.JpaConfig;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.default_schema=",
        "spring.config.import="
})
class AdminActivityLogRepositoryTest {

    @Autowired
    private AdminActivityLogRepository adminActivityLogRepository;

    @Test
    @DisplayName("adminId·type·keyword로 본인 로그만 조회된다")
    void searchByAdmin_filtersByAdminTypeKeyword() {
        saveLog(1L, 10L, AdminActivityLogType.ROOM_CREATED, "시험 방 생성됨", "새 입장 코드가 생성되었습니다.");
        saveLog(1L, 11L, AdminActivityLogType.EXAM_STARTED, "시험 세션이 시작되었습니다.", "시험이 시작되었습니다.");
        saveLog(2L, 12L, AdminActivityLogType.EXAM_ENDED, "시험이 종료되었습니다.", "시험이 종료되었습니다.");

        Page<AdminActivityLog> page = adminActivityLogRepository.searchByAdmin(
                1L,
                AdminActivityLogType.EXAM_STARTED,
                "시작",
                PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getType()).isEqualTo(AdminActivityLogType.EXAM_STARTED);
    }

    @Test
    @DisplayName("다른 adminId 로그는 조회되지 않는다")
    void searchByAdmin_excludesOtherAdmins() {
        saveLog(99L, 10L, AdminActivityLogType.ROOM_CREATED, "시험 방 생성됨", "새 입장 코드가 생성되었습니다.");

        Page<AdminActivityLog> page = adminActivityLogRepository.searchByAdmin(
                1L, null, null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isZero();
    }

    private void saveLog(
            Long adminId,
            Long examId,
            AdminActivityLogType type,
            String title,
            String message) {
        adminActivityLogRepository.save(AdminActivityLog.builder()
                .adminId(adminId)
                .examId(examId)
                .type(type)
                .title(title)
                .message(message)
                .build());
    }
}
