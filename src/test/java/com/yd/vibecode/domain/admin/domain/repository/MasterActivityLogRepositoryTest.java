package com.yd.vibecode.domain.admin.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLog;
import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLogType;
import com.yd.vibecode.global.config.JpaConfig;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.default_schema=",
        "spring.config.import="
})
class MasterActivityLogRepositoryTest {

    @Autowired
    private MasterActivityLogRepository masterActivityLogRepository;

    @Test
    @DisplayName("type·keyword로 로그를 조회한다")
    void search_filtersByTypeAndKeyword() {
        saveLog(MasterActivityLogType.ADMIN_SIGNUP_CODE_ISSUED,
                "관리자 가입 번호가 발급되었습니다",
                "새 관리자가 가입할 수 있는 가입 번호가 발급되었습니다.");
        saveLog(MasterActivityLogType.ADMIN_ACCOUNT_DELETED,
                "관리자 계정이 삭제되었습니다",
                "'홍길동' 관리자 계정이 삭제되었습니다.");

        Page<MasterActivityLog> page = masterActivityLogRepository.search(
                MasterActivityLogType.ADMIN_ACCOUNT_DELETED,
                "삭제",
                PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getType()).isEqualTo(MasterActivityLogType.ADMIN_ACCOUNT_DELETED);
    }

    @Test
    @DisplayName("keyword 없이 전체 로그를 조회한다")
    void search_withoutKeyword() {
        saveLog(MasterActivityLogType.ADMIN_PASSWORD_RESET,
                "관리자 비밀번호가 재설정되었습니다",
                "'김관리' 관리자 비밀번호가 재설정되었습니다.");

        Page<MasterActivityLog> page = masterActivityLogRepository.search(null, null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    private void saveLog(MasterActivityLogType type, String title, String message) {
        masterActivityLogRepository.save(MasterActivityLog.builder()
                .masterId(1L)
                .type(type)
                .title(title)
                .message(message)
                .build());
    }
}
