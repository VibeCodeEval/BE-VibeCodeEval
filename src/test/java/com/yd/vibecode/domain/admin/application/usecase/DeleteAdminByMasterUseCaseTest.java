package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.admin.domain.service.MasterActivityLogService;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;
import com.yd.vibecode.domain.auth.domain.service.AdminAccountDeletionService;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

@ExtendWith(MockitoExtension.class)
class DeleteAdminByMasterUseCaseTest {

    @InjectMocks
    private DeleteAdminByMasterUseCase deleteAdminByMasterUseCase;

    @Mock
    private AdminService adminService;

    @Mock
    private AdminAccountDeletionService adminAccountDeletionService;

    @Mock
    private MasterActivityLogService masterActivityLogService;

    @Test
    @DisplayName("MASTER가 일반 관리자 계정을 삭제할 수 있다")
    void execute_success() {
        Long requesterId = 1L;
        String targetAdminNumber = "ADM-123456";

        Admin requester = Admin.builder().role(AdminRole.MASTER).build();
        Admin target = Admin.builder().adminNumber(targetAdminNumber).role(AdminRole.ADMIN).build();
        ReflectionTestUtils.setField(target, "id", 10L);

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminService.findByAdminNumber(targetAdminNumber)).willReturn(target);

        deleteAdminByMasterUseCase.execute(requesterId, targetAdminNumber);

        verify(adminAccountDeletionService).softDelete(
                eq(target),
                eq(requesterId),
                eq("DELETE_ADMIN_BY_MASTER")
        );
        verify(masterActivityLogService).logAdminAccountDeleted(requesterId, 10L, target.getDisplayName());
    }

    @Test
    @DisplayName("MASTER가 아니면 삭제할 수 없다")
    void execute_not_master_throws() {
        Long requesterId = 2L;
        Admin requester = Admin.builder().role(AdminRole.ADMIN).build();

        given(adminService.findById(requesterId)).willReturn(requester);

        assertThatThrownBy(() -> deleteAdminByMasterUseCase.execute(requesterId, "ADM-123456"))
                .isInstanceOf(RestApiException.class)
                .extracting(ex -> ((RestApiException) ex).getErrorCode().getCode())
                .isEqualTo(AuthErrorStatus.MASTER_ONLY.getCode().getCode());
    }
}
