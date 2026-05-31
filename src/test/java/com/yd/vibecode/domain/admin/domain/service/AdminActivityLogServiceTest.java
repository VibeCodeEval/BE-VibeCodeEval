package com.yd.vibecode.domain.admin.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLog;
import com.yd.vibecode.domain.admin.domain.entity.AdminActivityLogType;
import com.yd.vibecode.domain.admin.domain.repository.AdminActivityLogRepository;

@ExtendWith(MockitoExtension.class)
class AdminActivityLogServiceTest {

    @InjectMocks
    private AdminActivityLogService adminActivityLogService;

    @Mock
    private AdminActivityLogRepository adminActivityLogRepository;

    @Test
    @DisplayName("ROOM_CREATED 로그 저장")
    void logRoomCreated() {
        adminActivityLogService.logRoomCreated(10L, 100L);

        ArgumentCaptor<AdminActivityLog> captor = ArgumentCaptor.forClass(AdminActivityLog.class);
        verify(adminActivityLogRepository).save(captor.capture());

        AdminActivityLog saved = captor.getValue();
        assertThat(saved.getAdminId()).isEqualTo(10L);
        assertThat(saved.getExamId()).isEqualTo(100L);
        assertThat(saved.getParticipantId()).isNull();
        assertThat(saved.getType()).isEqualTo(AdminActivityLogType.ROOM_CREATED);
        assertThat(saved.getTitle()).isEqualTo("시험 방 생성됨");
        assertThat(saved.getMessage()).isEqualTo("새 입장 코드가 생성되었습니다.");
    }

    @Test
    @DisplayName("EXAM_STARTED 로그 저장")
    void logExamStarted() {
        adminActivityLogService.logExamStarted(10L, 100L);

        ArgumentCaptor<AdminActivityLog> captor = ArgumentCaptor.forClass(AdminActivityLog.class);
        verify(adminActivityLogRepository).save(captor.capture());

        AdminActivityLog saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(AdminActivityLogType.EXAM_STARTED);
        assertThat(saved.getTitle()).isEqualTo("시험 세션이 시작되었습니다.");
        assertThat(saved.getMessage()).isEqualTo("시험이 시작되었습니다.");
    }

    @Test
    @DisplayName("EVALUATION_COMPLETED 로그 저장")
    void logEvaluationCompleted() {
        adminActivityLogService.logEvaluationCompleted(10L, 100L, 200L);

        ArgumentCaptor<AdminActivityLog> captor = ArgumentCaptor.forClass(AdminActivityLog.class);
        verify(adminActivityLogRepository).save(captor.capture());

        AdminActivityLog saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(AdminActivityLogType.EVALUATION_COMPLETED);
        assertThat(saved.getParticipantId()).isEqualTo(200L);
        assertThat(saved.getTitle()).isEqualTo("채점 과정이 성공적으로 완료되었습니다.");
        assertThat(saved.getMessage()).doesNotContain("token");
    }

    @Test
    @DisplayName("EXAM_ENDED 로그 저장")
    void logExamEnded() {
        adminActivityLogService.logExamEnded(10L, 100L);

        verify(adminActivityLogRepository).save(any(AdminActivityLog.class));
    }
}
