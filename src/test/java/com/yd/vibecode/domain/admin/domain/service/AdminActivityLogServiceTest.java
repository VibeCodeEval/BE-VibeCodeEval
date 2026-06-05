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
    @DisplayName("ROOM_CREATED 로그에 시험 제목을 포함한다")
    void logRoomCreated_includesExamTitle() {
        adminActivityLogService.logRoomCreated(10L, 100L, "2026 AI 코딩 테스트");

        ArgumentCaptor<AdminActivityLog> captor = ArgumentCaptor.forClass(AdminActivityLog.class);
        verify(adminActivityLogRepository).save(captor.capture());

        AdminActivityLog saved = captor.getValue();
        assertThat(saved.getAdminId()).isEqualTo(10L);
        assertThat(saved.getExamId()).isEqualTo(100L);
        assertThat(saved.getParticipantId()).isNull();
        assertThat(saved.getType()).isEqualTo(AdminActivityLogType.ROOM_CREATED);
        assertThat(saved.getTitle()).isEqualTo("시험 방 생성됨");
        assertThat(saved.getMessage()).isEqualTo("'2026 AI 코딩 테스트' 시험 방이 생성되었습니다.");
    }

    @Test
    @DisplayName("EXAM_STARTED 로그에 시험 제목을 포함한다")
    void logExamStarted_includesExamTitle() {
        adminActivityLogService.logExamStarted(10L, 100L, "중간고사 A");

        ArgumentCaptor<AdminActivityLog> captor = ArgumentCaptor.forClass(AdminActivityLog.class);
        verify(adminActivityLogRepository).save(captor.capture());

        AdminActivityLog saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(AdminActivityLogType.EXAM_STARTED);
        assertThat(saved.getTitle()).isEqualTo("시험 세션이 시작되었습니다.");
        assertThat(saved.getMessage()).isEqualTo("'중간고사 A' 시험이 시작되었습니다.");
    }

    @Test
    @DisplayName("EVALUATION_COMPLETED 로그에 참가자 표시명과 시험 제목을 포함한다")
    void logEvaluationCompleted_includesParticipantAndExamTitle() {
        adminActivityLogService.logEvaluationCompleted(10L, 100L, 200L, "기말 시험", "김민준");

        ArgumentCaptor<AdminActivityLog> captor = ArgumentCaptor.forClass(AdminActivityLog.class);
        verify(adminActivityLogRepository).save(captor.capture());

        AdminActivityLog saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(AdminActivityLogType.EVALUATION_COMPLETED);
        assertThat(saved.getParticipantId()).isEqualTo(200L);
        assertThat(saved.getTitle()).isEqualTo("채점 과정이 성공적으로 완료되었습니다.");
        assertThat(saved.getMessage()).isEqualTo("참가자 김민준님의 '기말 시험' 채점이 완료되었습니다.");
        assertThat(saved.getMessage()).doesNotContain("token");
    }

    @Test
    @DisplayName("EVALUATION_COMPLETED 참가자명 없으면 시험 제목만 포함한다")
    void logEvaluationCompleted_withoutParticipantName() {
        adminActivityLogService.logEvaluationCompleted(10L, 100L, 200L, "기말 시험", null);

        ArgumentCaptor<AdminActivityLog> captor = ArgumentCaptor.forClass(AdminActivityLog.class);
        verify(adminActivityLogRepository).save(captor.capture());

        assertThat(captor.getValue().getMessage())
                .isEqualTo("'기말 시험' 시험의 참가자 채점이 완료되었습니다.");
    }

    @Test
    @DisplayName("EXAM_ENDED 로그에 시험 제목을 포함한다")
    void logExamEnded_includesExamTitle() {
        adminActivityLogService.logExamEnded(10L, 100L, "중간고사 A");

        ArgumentCaptor<AdminActivityLog> captor = ArgumentCaptor.forClass(AdminActivityLog.class);
        verify(adminActivityLogRepository).save(captor.capture());

        assertThat(captor.getValue().getMessage()).isEqualTo("'중간고사 A' 시험이 종료되었습니다.");
    }

    @Test
    @DisplayName("시험 제목이 비어 있으면 examId fallback을 사용한다")
    void resolveExamLabel_blankTitleUsesExamId() {
        assertThat(AdminActivityLogService.resolveExamLabel(null, 42L)).isEqualTo("시험 ID 42");
        assertThat(AdminActivityLogService.resolveExamLabel("  ", 42L)).isEqualTo("시험 ID 42");
        assertThat(AdminActivityLogService.formatMessage("'%s' 시험이 시작되었습니다.", null, 42L))
                .isEqualTo("'시험 ID 42' 시험이 시작되었습니다.");
    }

    @Test
    @DisplayName("EXAM_ENDED 기본 호출")
    void logExamEnded() {
        adminActivityLogService.logExamEnded(10L, 100L, "테스트");

        verify(adminActivityLogRepository).save(any(AdminActivityLog.class));
    }
}
