package com.yd.vibecode.domain.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.yd.vibecode.domain.admin.domain.service.AdminAuditLogService;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminNumber;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;
import com.yd.vibecode.domain.auth.domain.repository.AdminNumberRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.exception.code.status.GlobalErrorStatus;

@ExtendWith(MockitoExtension.class)
class AdminAccountDeletionServiceTest {

    private static final String ADMIN_NUMBER = "ADM-000001";
    private static final Long TARGET_ADMIN_ID = 10L;
    private static final Long ACTOR_ADMIN_ID = 1L;
    private static final String AUDIT_ACTION = "DELETE_ADMIN_BY_MASTER";

    @InjectMocks
    private AdminAccountDeletionService adminAccountDeletionService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AdminNumberRepository adminNumberRepository;

    @Mock
    private AdminAuditLogService adminAuditLogService;

    @Test
    @DisplayName("softDelete는 관리자 삭제의 모든 side effect를 한 번에 수행한다")
    void softDelete_appliesAllSideEffects() {
        Admin admin = activeAdmin();
        AdminNumber adminNumber = linkedAdminNumber();

        given(adminNumberRepository.findByAdminNumber(ADMIN_NUMBER)).willReturn(Optional.of(adminNumber));

        adminAccountDeletionService.softDelete(admin, ACTOR_ADMIN_ID, AUDIT_ACTION);

        assertThat(admin.isDeleted()).isTrue();
        assertThat(admin.getDeletedAt()).isNotNull();
        assertThat(admin.getIsActive()).isFalse();

        verify(refreshTokenService).deleteRefreshToken(eq(TARGET_ADMIN_ID.toString()));
        verify(adminNumberRepository).findByAdminNumber(ADMIN_NUMBER);
        assertThat(adminNumber.getIsActive()).isFalse();

        ArgumentCaptor<Map<String, Object>> auditPayloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(adminAuditLogService).log(eq(ACTOR_ADMIN_ID), eq(AUDIT_ACTION), auditPayloadCaptor.capture());
        Map<String, Object> auditPayload = auditPayloadCaptor.getValue();
        assertThat(auditPayload)
                .containsEntry("targetAdminId", TARGET_ADMIN_ID)
                .containsEntry("targetAdminNumber", ADMIN_NUMBER);
    }

    @Test
    @DisplayName("연결된 admin number가 없어도 관리자 soft delete side effect는 수행된다")
    void softDelete_withoutLinkedAdminNumber_stillSoftDeletesAdmin() {
        Admin admin = activeAdmin();

        given(adminNumberRepository.findByAdminNumber(ADMIN_NUMBER)).willReturn(Optional.empty());

        adminAccountDeletionService.softDelete(admin, ACTOR_ADMIN_ID, AUDIT_ACTION);

        assertThat(admin.isDeleted()).isTrue();
        assertThat(admin.getIsActive()).isFalse();
        verify(refreshTokenService).deleteRefreshToken(eq(TARGET_ADMIN_ID.toString()));
        verify(adminAuditLogService).log(eq(ACTOR_ADMIN_ID), eq(AUDIT_ACTION), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("actorAdminId 또는 auditAction이 null이면 audit log는 기록하지 않는다")
    void softDelete_withoutActorOrAction_skipsAuditLog() {
        Admin admin = activeAdmin();

        given(adminNumberRepository.findByAdminNumber(ADMIN_NUMBER)).willReturn(Optional.empty());

        adminAccountDeletionService.softDelete(admin, null, AUDIT_ACTION);
        adminAccountDeletionService.softDelete(activeAdmin(), ACTOR_ADMIN_ID, null);

        verify(adminAuditLogService, never()).log(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    @DisplayName("이미 삭제된 관리자는 soft delete할 수 없다")
    void softDelete_alreadyDeleted_throwsNotFound() {
        Admin admin = activeAdmin();
        admin.delete();

        assertThatThrownBy(() -> adminAccountDeletionService.softDelete(admin, ACTOR_ADMIN_ID, AUDIT_ACTION))
                .isInstanceOf(RestApiException.class)
                .extracting(ex -> ((RestApiException) ex).getErrorCode())
                .isEqualTo(GlobalErrorStatus._NOT_FOUND.getCode());

        verify(refreshTokenService, never()).deleteRefreshToken(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("MASTER 계정은 soft delete할 수 없다")
    void softDelete_masterAccount_throws() {
        Admin master = Admin.builder()
                .adminNumber("MASTER-001")
                .email("master@example.com")
                .passwordHash("hash")
                .role(AdminRole.MASTER)
                .isActive(true)
                .build();
        ReflectionTestUtils.setField(master, "id", 99L);

        assertThatThrownBy(() -> adminAccountDeletionService.softDelete(master, ACTOR_ADMIN_ID, AUDIT_ACTION))
                .isInstanceOf(RestApiException.class)
                .extracting(ex -> ((RestApiException) ex).getErrorCode())
                .isEqualTo(AuthErrorStatus.MASTER_ACCOUNT_CANNOT_BE_DEACTIVATED.getCode());

        assertThat(master.isDeleted()).isFalse();
        assertThat(master.getIsActive()).isTrue();
        verify(refreshTokenService, never()).deleteRefreshToken(org.mockito.ArgumentMatchers.anyString());
    }

    private Admin activeAdmin() {
        Admin admin = Admin.builder()
                .adminNumber(ADMIN_NUMBER)
                .email("target@example.com")
                .passwordHash("hash")
                .role(AdminRole.ADMIN)
                .isActive(true)
                .build();
        ReflectionTestUtils.setField(admin, "id", TARGET_ADMIN_ID);
        return admin;
    }

    private AdminNumber linkedAdminNumber() {
        return AdminNumber.builder()
                .adminNumber(ADMIN_NUMBER)
                .label("Test Admin")
                .isActive(true)
                .issuedBy(ACTOR_ADMIN_ID)
                .assignedAdminId(TARGET_ADMIN_ID)
                .usedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }
}
