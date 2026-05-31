package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.yd.vibecode.domain.admin.application.dto.response.AdminActivityLogPageResponse;
import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLog;
import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLogType;
import com.yd.vibecode.domain.admin.domain.repository.AdminActivityLogRepository;

@ExtendWith(MockitoExtension.class)
class GetAdminActivityLogsUseCaseTest {

    @InjectMocks
    private GetAdminActivityLogsUseCase getAdminActivityLogsUseCase;

    @Mock
    private AdminActivityLogRepository adminActivityLogRepository;

    @Test
    @DisplayName("현재 관리자 adminId 기준으로 로그를 조회한다")
    void execute_filtersByAdminId() {
        Long adminId = 5L;
        AdminActivityLog log = AdminActivityLog.builder()
                .adminId(adminId)
                .examId(1L)
                .type(AdminActivityLogType.EXAM_STARTED)
                .title("시험 세션이 시작되었습니다.")
                .message("시험이 시작되었습니다.")
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(log, "createdAt", LocalDateTime.now());

        Page<AdminActivityLog> page = new PageImpl<>(List.of(log));
        given(adminActivityLogRepository.searchByAdmin(
                eq(adminId), isNull(), isNull(), org.mockito.ArgumentMatchers.any(Pageable.class)))
                .willReturn(page);

        AdminActivityLogPageResponse response = getAdminActivityLogsUseCase.execute(adminId, null, null, 0, 20);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).type()).isEqualTo(AdminActivityLogType.EXAM_STARTED);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(20);
    }

    @Test
    @DisplayName("size는 최대 100으로 제한된다")
    void execute_capsPageSize() {
        Page<AdminActivityLog> page = new PageImpl<>(List.of());
        given(adminActivityLogRepository.searchByAdmin(
                eq(1L), isNull(), isNull(), org.mockito.ArgumentMatchers.argThat(p -> p.getPageSize() == 100)))
                .willReturn(page);

        AdminActivityLogPageResponse response = getAdminActivityLogsUseCase.execute(1L, null, null, 0, 500);

        assertThat(response.size()).isEqualTo(100);
    }
}
