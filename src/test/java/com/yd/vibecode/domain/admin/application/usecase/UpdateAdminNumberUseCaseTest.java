package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import com.yd.vibecode.domain.admin.application.dto.request.AdminNumberUpdateRequest;
import com.yd.vibecode.domain.admin.application.dto.response.AdminNumberResponse;
import com.yd.vibecode.domain.admin.domain.service.MasterActivityLogService;
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

    @Mock
    private MasterActivityLogService masterActivityLogService;

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

        AdminNumber beforeUpdate = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .assignedAdminId(assignedAdminId)
                .isActive(false)
                .build();

        AdminNumber updatedAdminNumber = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .assignedAdminId(assignedAdminId)
                .isActive(true)
                .build();

        Admin assignedAdmin = Admin.builder()
                .displayName("김관리")
                .isActive(false) // 현재 비활성 상태
                .role(AdminRole.ADMIN)
                .build();

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminNumberService.getByAdminNumber(adminNumberStr)).willReturn(beforeUpdate);
        given(adminNumberService.update(any(), any(), any(), any())).willReturn(updatedAdminNumber);
        given(adminService.findById(assignedAdminId)).willReturn(assignedAdmin);

        // when
        AdminNumberResponse response = updateAdminNumberUseCase.execute(requesterId, adminNumberStr, request);

        // then
        assertThat(response.active()).isTrue();
        assertThat(assignedAdmin.getIsActive()).isTrue(); // 계정도 활성화되었는지 확인
        verify(masterActivityLogService).logSignupCodeReactivated(requesterId, assignedAdminId, "김관리");
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

        AdminNumber beforeUpdate = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .assignedAdminId(assignedAdminId)
                .isActive(true)
                .build();

        AdminNumber updatedAdminNumber = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .assignedAdminId(assignedAdminId)
                .isActive(false)
                .build();

        Admin assignedAdmin = Admin.builder()
                .displayName("이관리")
                .isActive(true) // 현재 활성 상태
                .role(AdminRole.ADMIN)
                .build();

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminNumberService.getByAdminNumber(adminNumberStr)).willReturn(beforeUpdate);
        given(adminNumberService.update(any(), any(), any(), any())).willReturn(updatedAdminNumber);
        given(adminService.findById(assignedAdminId)).willReturn(assignedAdmin);

        // when
        updateAdminNumberUseCase.execute(requesterId, adminNumberStr, request);

        // then
        assertThat(assignedAdmin.getIsActive()).isFalse(); // 계정이 비활성화되었는지 확인
        verify(masterActivityLogService).logSignupCodeDeactivated(requesterId, assignedAdminId, "이관리");
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
        given(adminNumberService.getByAdminNumber(adminNumberStr)).willReturn(AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .assignedAdminId(assignedAdminId)
                .isActive(true)
                .build());
        given(adminNumberService.update(any(), any(), any(), any())).willReturn(updatedAdminNumber);
        given(adminService.findById(assignedAdminId)).willReturn(assignedAdmin);

        // when & then
        assertThatThrownBy(() -> updateAdminNumberUseCase.execute(requesterId, adminNumberStr, request))
                .satisfies(e -> {
                    RestApiException exception = (RestApiException) e;
                    assertThat(exception.getErrorCode().getCode()).isEqualTo(AuthErrorStatus.MASTER_ACCOUNT_CANNOT_BE_DEACTIVATED.getCode().getCode());
                });
        verify(masterActivityLogService, never()).logSignupCodeDeactivated(any(), any(), any());
    }

    @Test
    @DisplayName("active 값이 변하지 않으면 활동 로그를 기록하지 않는다")
    void execute_noActiveChange_doesNotLog() {
        Long requesterId = 1L;
        String adminNumberStr = "ADM-123456";

        Admin requester = Admin.builder().role(AdminRole.MASTER).build();
        AdminNumberUpdateRequest request = new AdminNumberUpdateRequest(
                "Updated Label",
                true,
                LocalDateTime.now().plusDays(30)
        );

        AdminNumber beforeUpdate = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .isActive(true)
                .build();
        AdminNumber updatedAdminNumber = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .isActive(true)
                .build();

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminNumberService.getByAdminNumber(adminNumberStr)).willReturn(beforeUpdate);
        given(adminNumberService.update(any(), any(), any(), any())).willReturn(updatedAdminNumber);

        updateAdminNumberUseCase.execute(requesterId, adminNumberStr, request);

        verify(masterActivityLogService, never()).logSignupCodeDeactivated(any(), any(), any());
        verify(masterActivityLogService, never()).logSignupCodeReactivated(any(), any(), any());
    }

    @Test
    @DisplayName("연결된 관리자 없는 가입 번호 비활성화 시 가입 번호 기준 로그를 기록한다")
    void execute_deactivate_unassignedSignupCode_logsSignupCodeMessage() {
        Long requesterId = 1L;
        String adminNumberStr = "ADM-UNASSIGNED";

        Admin requester = Admin.builder().role(AdminRole.MASTER).build();
        AdminNumberUpdateRequest request = new AdminNumberUpdateRequest(
                null,
                false,
                LocalDateTime.now().plusDays(30)
        );

        AdminNumber beforeUpdate = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .isActive(true)
                .build();
        AdminNumber updatedAdminNumber = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .isActive(false)
                .build();

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminNumberService.getByAdminNumber(adminNumberStr)).willReturn(beforeUpdate);
        given(adminNumberService.update(any(), any(), any(), any())).willReturn(updatedAdminNumber);

        updateAdminNumberUseCase.execute(requesterId, adminNumberStr, request);

        verify(masterActivityLogService).logSignupCodeDeactivated(requesterId, null, null);
    }

    @Test
    @DisplayName("연결된 관리자 없는 가입 번호 재활성화 시 가입 번호 기준 로그를 기록한다")
    void execute_reactivate_unassignedSignupCode_logsSignupCodeMessage() {
        Long requesterId = 1L;
        String adminNumberStr = "ADM-UNASSIGNED";

        Admin requester = Admin.builder().role(AdminRole.MASTER).build();
        AdminNumberUpdateRequest request = new AdminNumberUpdateRequest(
                null,
                true,
                LocalDateTime.now().plusDays(30)
        );

        AdminNumber beforeUpdate = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .isActive(false)
                .build();
        AdminNumber updatedAdminNumber = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .isActive(true)
                .build();

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminNumberService.getByAdminNumber(adminNumberStr)).willReturn(beforeUpdate);
        given(adminNumberService.update(any(), any(), any(), any())).willReturn(updatedAdminNumber);

        updateAdminNumberUseCase.execute(requesterId, adminNumberStr, request);

        verify(masterActivityLogService).logSignupCodeReactivated(requesterId, null, null);
    }

    @Test
    @DisplayName("기본 active=true(null) 상태에서 최초 true → false 변경 시 DEACTIVATED 로그를 기록한다")
    void execute_firstDeactivate_withNullIsActive_logsDeactivated() {
        Long requesterId = 1L;
        String adminNumberStr = "ADM-NEW";

        Admin requester = Admin.builder().role(AdminRole.MASTER).build();
        AdminNumberUpdateRequest request = new AdminNumberUpdateRequest(null, false, null);

        AdminNumber sharedEntity = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .isActive(null)
                .build();

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminNumberService.getByAdminNumber(adminNumberStr)).willReturn(sharedEntity);
        given(adminNumberService.update(eq(adminNumberStr), eq(null), eq(false), eq(null)))
                .willAnswer((Answer<AdminNumber>) invocation -> {
                    sharedEntity.update(null, false, null);
                    return sharedEntity;
                });

        updateAdminNumberUseCase.execute(requesterId, adminNumberStr, request);

        verify(masterActivityLogService).logSignupCodeDeactivated(requesterId, null, null);
    }

    @Test
    @DisplayName("동일 JPA 엔티티 참조에서도 최초 true → false 변경 시 DEACTIVATED 로그를 기록한다")
    void execute_firstDeactivate_sameEntityReference_logsDeactivated() {
        Long requesterId = 1L;
        String adminNumberStr = "ADM-SHARED";
        Long assignedAdminId = 2L;

        Admin requester = Admin.builder().role(AdminRole.MASTER).build();
        AdminNumberUpdateRequest request = new AdminNumberUpdateRequest(null, false, null);

        AdminNumber sharedEntity = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .assignedAdminId(assignedAdminId)
                .isActive(true)
                .build();

        Admin assignedAdmin = Admin.builder()
                .displayName("박관리")
                .isActive(true)
                .role(AdminRole.ADMIN)
                .build();

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminNumberService.getByAdminNumber(adminNumberStr)).willReturn(sharedEntity);
        given(adminNumberService.update(eq(adminNumberStr), eq(null), eq(false), eq(null)))
                .willAnswer((Answer<AdminNumber>) invocation -> {
                    sharedEntity.update(null, false, null);
                    return sharedEntity;
                });
        given(adminService.findById(assignedAdminId)).willReturn(assignedAdmin);

        updateAdminNumberUseCase.execute(requesterId, adminNumberStr, request);

        verify(masterActivityLogService).logSignupCodeDeactivated(requesterId, assignedAdminId, "박관리");
        assertThat(sharedEntity.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("false → false 요청 시 로그를 기록하지 않는다")
    void execute_falseToFalse_doesNotLog() {
        Long requesterId = 1L;
        String adminNumberStr = "ADM-INACTIVE";

        Admin requester = Admin.builder().role(AdminRole.MASTER).build();
        AdminNumberUpdateRequest request = new AdminNumberUpdateRequest(null, false, null);

        AdminNumber sharedEntity = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .isActive(false)
                .build();

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminNumberService.getByAdminNumber(adminNumberStr)).willReturn(sharedEntity);
        given(adminNumberService.update(eq(adminNumberStr), eq(null), eq(false), eq(null)))
                .willAnswer((Answer<AdminNumber>) invocation -> {
                    sharedEntity.update(null, false, null);
                    return sharedEntity;
                });

        updateAdminNumberUseCase.execute(requesterId, adminNumberStr, request);

        verify(masterActivityLogService, never()).logSignupCodeDeactivated(any(), any(), any());
        verify(masterActivityLogService, never()).logSignupCodeReactivated(any(), any(), any());
    }

    @Test
    @DisplayName("가입 완료 후 AdminNumber=false·Admin=true 상태에서 최초 비활성화 시 DEACTIVATED 로그를 기록한다")
    void execute_deactivate_afterSignup_adminActiveAdminNumberInactive_logsDeactivated() {
        Long requesterId = 1L;
        String adminNumberStr = "ADM-SIGNED-UP";
        Long assignedAdminId = 2L;

        Admin requester = Admin.builder().role(AdminRole.MASTER).build();
        AdminNumberUpdateRequest request = new AdminNumberUpdateRequest(null, false, null);

        // assign() 직후: 가입 번호는 false, 관리자 계정은 true
        AdminNumber beforeUpdate = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .assignedAdminId(assignedAdminId)
                .isActive(false)
                .build();
        AdminNumber updatedAdminNumber = AdminNumber.builder()
                .adminNumber(adminNumberStr)
                .assignedAdminId(assignedAdminId)
                .isActive(false)
                .build();
        Admin assignedAdmin = Admin.builder()
                .displayName("최관리")
                .isActive(true)
                .role(AdminRole.ADMIN)
                .build();

        given(adminService.findById(requesterId)).willReturn(requester);
        given(adminNumberService.getByAdminNumber(adminNumberStr)).willReturn(beforeUpdate);
        given(adminNumberService.update(any(), any(), any(), any())).willReturn(updatedAdminNumber);
        given(adminService.findById(assignedAdminId)).willReturn(assignedAdmin);

        updateAdminNumberUseCase.execute(requesterId, adminNumberStr, request);

        assertThat(assignedAdmin.getIsActive()).isFalse();
        verify(masterActivityLogService).logSignupCodeDeactivated(requesterId, assignedAdminId, "최관리");
    }

    @Test
    @DisplayName("resolveActiveState는 null을 활성(true)으로 간주한다")
    void resolveActiveState_nullMeansActive() {
        assertThat(UpdateAdminNumberUseCase.resolveActiveState(null)).isTrue();
        assertThat(UpdateAdminNumberUseCase.resolveActiveState(true)).isTrue();
        assertThat(UpdateAdminNumberUseCase.resolveActiveState(false)).isFalse();
    }
}
