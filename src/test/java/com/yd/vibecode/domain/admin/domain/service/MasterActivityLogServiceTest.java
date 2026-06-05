package com.yd.vibecode.domain.admin.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLog;
import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLogType;
import com.yd.vibecode.domain.admin.domain.repository.MasterActivityLogRepository;

@ExtendWith(MockitoExtension.class)
class MasterActivityLogServiceTest {

    @InjectMocks
    private MasterActivityLogService masterActivityLogService;

    @Mock
    private MasterActivityLogRepository masterActivityLogRepository;

    @Test
    @DisplayName("ADMIN_SIGNUP_CODE_ISSUED 로그에 가입 번호 원문을 포함하지 않는다")
    void logSignupCodeIssued_doesNotIncludeSignupCode() {
        masterActivityLogService.logSignupCodeIssued(1L);

        ArgumentCaptor<MasterActivityLog> captor = forClass(MasterActivityLog.class);
        verify(masterActivityLogRepository).save(captor.capture());

        MasterActivityLog saved = captor.getValue();
        assertThat(saved.getMasterId()).isEqualTo(1L);
        assertThat(saved.getTargetAdminId()).isNull();
        assertThat(saved.getType()).isEqualTo(MasterActivityLogType.ADMIN_SIGNUP_CODE_ISSUED);
        assertThat(saved.getTitle()).isEqualTo("관리자 가입 번호가 발급되었습니다");
        assertThat(saved.getMessage()).isEqualTo("새 관리자가 가입할 수 있는 가입 번호가 발급되었습니다.");
        assertThat(saved.getMessage()).doesNotContain("ADM-");
    }

    @Test
    @DisplayName("연결된 관리자 계정 비활성화 시 계정 기준 message를 생성한다")
    void logSignupCodeDeactivated_withAssignedAdmin_usesAccountMessage() {
        masterActivityLogService.logSignupCodeDeactivated(1L, 10L, "홍길동");

        ArgumentCaptor<MasterActivityLog> captor = forClass(MasterActivityLog.class);
        verify(masterActivityLogRepository).save(captor.capture());

        MasterActivityLog saved = captor.getValue();
        assertThat(saved.getTargetAdminId()).isEqualTo(10L);
        assertThat(saved.getTitle()).isEqualTo("관리자 계정이 비활성화되었습니다");
        assertThat(saved.getMessage()).isEqualTo("'홍길동' 관리자 계정이 비활성화되었습니다.");
        assertThat(saved.getMessage()).doesNotContain("ADM-");
    }

    @Test
    @DisplayName("연결된 관리자 계정 재활성화 시 계정 기준 message를 생성한다")
    void logSignupCodeReactivated_withAssignedAdmin_usesAccountMessage() {
        masterActivityLogService.logSignupCodeReactivated(1L, 10L, "홍길동");

        ArgumentCaptor<MasterActivityLog> captor = forClass(MasterActivityLog.class);
        verify(masterActivityLogRepository).save(captor.capture());

        MasterActivityLog saved = captor.getValue();
        assertThat(saved.getTargetAdminId()).isEqualTo(10L);
        assertThat(saved.getTitle()).isEqualTo("관리자 계정이 재활성화되었습니다");
        assertThat(saved.getMessage()).isEqualTo("'홍길동' 관리자 계정이 재활성화되었습니다.");
        assertThat(saved.getMessage()).doesNotContain("ADM-");
    }

    @Test
    @DisplayName("연결된 관리자 없는 가입 번호 비활성화 시 가입 번호 기준 message를 생성한다")
    void logSignupCodeDeactivated_withoutAssignedAdmin_usesSignupCodeMessage() {
        masterActivityLogService.logSignupCodeDeactivated(1L, null, null);

        ArgumentCaptor<MasterActivityLog> captor = forClass(MasterActivityLog.class);
        verify(masterActivityLogRepository).save(captor.capture());

        MasterActivityLog saved = captor.getValue();
        assertThat(saved.getTargetAdminId()).isNull();
        assertThat(saved.getTitle()).isEqualTo("관리자 가입 번호가 비활성화되었습니다");
        assertThat(saved.getMessage()).isEqualTo("관리자 가입 번호가 비활성화되었습니다.");
    }

    @Test
    @DisplayName("연결된 관리자 없는 가입 번호 재활성화 시 가입 번호 기준 message를 생성한다")
    void logSignupCodeReactivated_withoutAssignedAdmin_usesSignupCodeMessage() {
        masterActivityLogService.logSignupCodeReactivated(1L, null, null);

        ArgumentCaptor<MasterActivityLog> captor = forClass(MasterActivityLog.class);
        verify(masterActivityLogRepository).save(captor.capture());

        MasterActivityLog saved = captor.getValue();
        assertThat(saved.getTargetAdminId()).isNull();
        assertThat(saved.getTitle()).isEqualTo("관리자 가입 번호가 재활성화되었습니다");
        assertThat(saved.getMessage()).isEqualTo("관리자 가입 번호가 다시 사용 가능 상태로 변경되었습니다.");
    }

    @Test
    @DisplayName("ADMIN_SIGNED_UP 로그에 displayName을 포함한다")
    void logAdminSignedUp_includesDisplayName() {
        masterActivityLogService.logAdminSignedUp(1L, 10L, "홍길동");

        ArgumentCaptor<MasterActivityLog> captor = forClass(MasterActivityLog.class);
        verify(masterActivityLogRepository).save(captor.capture());

        MasterActivityLog saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(MasterActivityLogType.ADMIN_SIGNED_UP);
        assertThat(saved.getTargetAdminId()).isEqualTo(10L);
        assertThat(saved.getMessage()).isEqualTo("'홍길동' 관리자가 가입을 완료했습니다.");
    }

    @Test
    @DisplayName("displayName이 없으면 관리자 ID fallback을 사용한다")
    void resolveTargetDisplayName_blankUsesAdminId() {
        assertThat(MasterActivityLogService.resolveTargetDisplayName(null, 42L)).isEqualTo("관리자 ID 42");
        assertThat(MasterActivityLogService.resolveTargetDisplayName("  ", 42L)).isEqualTo("관리자 ID 42");
    }

    @Test
    @DisplayName("ADMIN_PASSWORD_RESET 로그에 비밀번호를 포함하지 않는다")
    void logAdminPasswordReset_doesNotIncludePassword() {
        masterActivityLogService.logAdminPasswordReset(1L, 2L, "김관리");

        ArgumentCaptor<MasterActivityLog> captor = forClass(MasterActivityLog.class);
        verify(masterActivityLogRepository).save(captor.capture());

        MasterActivityLog saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(MasterActivityLogType.ADMIN_PASSWORD_RESET);
        assertThat(saved.getMessage()).isEqualTo("'김관리' 관리자 비밀번호가 재설정되었습니다.");
        assertThat(saved.getMessage()).doesNotContain("password");
        assertThat(saved.getMessage()).doesNotContain("token");
    }

    @Test
    @DisplayName("ADMIN_ACCOUNT_DELETED 로그를 기록한다")
    void logAdminAccountDeleted() {
        masterActivityLogService.logAdminAccountDeleted(1L, 5L, "이관리");

        ArgumentCaptor<MasterActivityLog> captor = forClass(MasterActivityLog.class);
        verify(masterActivityLogRepository).save(captor.capture());

        assertThat(captor.getValue().getType()).isEqualTo(MasterActivityLogType.ADMIN_ACCOUNT_DELETED);
        assertThat(captor.getValue().getMessage()).isEqualTo("'이관리' 관리자 계정이 삭제되었습니다.");
    }
}
