package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.yd.vibecode.domain.admin.domain.service.AdminAuditLogService;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.util.TemporaryPasswordGenerator;

@ExtendWith(MockitoExtension.class)
class ResetAdminPasswordByMasterUseCaseTest {

    @InjectMocks
    private ResetAdminPasswordByMasterUseCase resetAdminPasswordByMasterUseCase;

    @Mock
    private AdminService adminService;

    @Mock
    private AdminAuditLogService adminAuditLogService;

    @Mock
    private TemporaryPasswordGenerator temporaryPasswordGenerator;

    @Test
    @DisplayName("MASTER가 타 관리자 임시 비밀번호 재설정 성공")
    void execute_success() {
        Long requesterId = 1L;
        String adminNumber = "ADM-123456";
        String temporaryPassword = "TempPass1!xYz";

        Admin requester = Admin.builder()
                .role(AdminRole.MASTER)
                .build();
        Admin target = Admin.builder()
                .adminNumber(adminNumber)
                .passwordHash("oldHash")
                .role(AdminRole.ADMIN)
                .build();
        ReflectionTestUtils.setField(target, "id", 2L);

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminService.findByAdminNumber(adminNumber)).willReturn(target);
        given(temporaryPasswordGenerator.generate()).willReturn(temporaryPassword);
        given(adminService.encodePassword(temporaryPassword)).willReturn("encodedNew");

        var response = resetAdminPasswordByMasterUseCase.execute(requesterId, adminNumber);

        assertThat(response.temporaryPassword()).isEqualTo(temporaryPassword);
        assertThat(target.getPasswordHash()).isEqualTo("encodedNew");
        verify(adminService).encodePassword(temporaryPassword);
        verify(adminAuditLogService).log(eq(requesterId), eq("RESET_PASSWORD_BY_MASTER"), any());
    }

    @Test
    @DisplayName("MASTER가 아니면 비밀번호 재설정 불가")
    void execute_fail_not_master() {
        Long requesterId = 1L;
        Admin requester = Admin.builder().role(AdminRole.ADMIN).build();

        given(adminService.findById(requesterId)).willReturn(requester);

        assertThatThrownBy(() -> resetAdminPasswordByMasterUseCase.execute(requesterId, "ADM-1"))
                .isInstanceOf(RestApiException.class)
                .satisfies(ex -> {
                    RestApiException exception = (RestApiException) ex;
                    assertThat(exception.getErrorCode().getCode())
                            .isEqualTo(AuthErrorStatus.MASTER_ONLY.getCode().getCode());
                });
    }

    @Test
    @DisplayName("마스터 계정 비밀번호 재설정 불가")
    void execute_fail_target_is_master() {
        Long requesterId = 1L;
        String adminNumber = "MASTER-0001";

        Admin requester = Admin.builder().role(AdminRole.MASTER).build();
        Admin target = Admin.builder()
                .adminNumber(adminNumber)
                .role(AdminRole.MASTER)
                .build();

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminService.findByAdminNumber(adminNumber)).willReturn(target);

        assertThatThrownBy(() -> resetAdminPasswordByMasterUseCase.execute(requesterId, adminNumber))
                .isInstanceOf(RestApiException.class)
                .satisfies(ex -> {
                    RestApiException exception = (RestApiException) ex;
                    assertThat(exception.getErrorCode().getCode())
                            .isEqualTo(AuthErrorStatus.MASTER_ACCOUNT_PASSWORD_CANNOT_BE_RESET.getCode().getCode());
                });
    }
}
