package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.admin.application.dto.request.AdminNumberUpdateRequest;
import com.yd.vibecode.domain.admin.application.dto.response.AdminNumberResponse;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminNumber;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;
import com.yd.vibecode.domain.auth.domain.service.AdminNumberService;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

@ExtendWith(MockitoExtension.class)
class UpdateAdminNumberUseCaseTest {

    @InjectMocks
    private UpdateAdminNumberUseCase updateAdminNumberUseCase;

    @Mock
    private AdminService adminService;

    @Mock
    private AdminNumberService adminNumberService;

    @Test
    @DisplayName("관리자 번호 활성화 시 Admin 계정도 활성화되어야 한다")
    void execute_activate_success() {
        // given
        Long requesterId = 1L;
        String adminNumberStr = "ADM-123456";
        Long assignedAdminId = 2L;
        
        Admin requester = Admin.builder()
                .role(AdminRole.MASTER)
                .build();
        
        AdminNumberUpdateRequest request = new AdminNumberUpdateRequest(
                "Updated Label",
                true, // 활성화 요청
                LocalDateTime.now().plusDays(30)
        );

        AdminNumber updatedAdminNumber = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .assignedAdminId(assignedAdminId)
                .isActive(true)
                .build();

        Admin assignedAdmin = Admin.builder()
                .isActive(false) // 현재 비활성 상태
                .role(AdminRole.ADMIN)
                .build();

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminNumberService.update(any(), any(), any(), any())).willReturn(updatedAdminNumber);
        given(adminService.findById(assignedAdminId)).willReturn(assignedAdmin);

        // when
        AdminNumberResponse response = updateAdminNumberUseCase.execute(requesterId, adminNumberStr, request);

        // then
        assertThat(response.active()).isTrue();
        assertThat(assignedAdmin.getIsActive()).isTrue(); // 계정도 활성화되었는지 확인
    }

    @Test
    @DisplayName("관리자 번호 비활성화 시 Admin 계정도 비활성화되어야 한다")
    void execute_deactivate_success() {
        // given
        Long requesterId = 1L;
        String adminNumberStr = "ADM-123456";
        Long assignedAdminId = 2L;
        
        Admin requester = Admin.builder()
                .role(AdminRole.MASTER)
                .build();
        
        AdminNumberUpdateRequest request = new AdminNumberUpdateRequest(
                "Updated Label",
                false, // 비활성화 요청
                LocalDateTime.now().plusDays(30)
        );

        AdminNumber updatedAdminNumber = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .assignedAdminId(assignedAdminId)
                .isActive(false)
                .build();

        Admin assignedAdmin = Admin.builder()
                .isActive(true) // 현재 활성 상태
                .role(AdminRole.ADMIN)
                .build();

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminNumberService.update(any(), any(), any(), any())).willReturn(updatedAdminNumber);
        given(adminService.findById(assignedAdminId)).willReturn(assignedAdmin);

        // when
        updateAdminNumberUseCase.execute(requesterId, adminNumberStr, request);

        // then
        assertThat(assignedAdmin.getIsActive()).isFalse(); // 계정이 비활성화되었는지 확인
    }

    @Test
    @DisplayName("MASTER 계정은 비활성화할 수 없다")
    void execute_deactivate_master_fail() {
        // given
        Long requesterId = 1L;
        String adminNumberStr = "ADM-MASTER";
        Long assignedAdminId = 2L;
        
        Admin requester = Admin.builder()
                .role(AdminRole.MASTER)
                .build();
        
        AdminNumberUpdateRequest request = new AdminNumberUpdateRequest(
                "Updated Label",
                false, // 비활성화 요청
                LocalDateTime.now().plusDays(30)
        );

        AdminNumber updatedAdminNumber = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .assignedAdminId(assignedAdminId)
                .isActive(false)
                .build();

        Admin assignedAdmin = Admin.builder()
                .isActive(true)
                .role(AdminRole.MASTER) // 대상이 MASTER
                .build();

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminNumberService.update(any(), any(), any(), any())).willReturn(updatedAdminNumber);
        given(adminService.findById(assignedAdminId)).willReturn(assignedAdmin);

        // when & then
        assertThatThrownBy(() -> updateAdminNumberUseCase.execute(requesterId, adminNumberStr, request))
                .satisfies(e -> {
                    RestApiException exception = (RestApiException) e;
                    assertThat(exception.getErrorCode().getCode()).isEqualTo(AuthErrorStatus.MASTER_ACCOUNT_CANNOT_BE_DEACTIVATED.getCode().getCode());
                });
    }
}
