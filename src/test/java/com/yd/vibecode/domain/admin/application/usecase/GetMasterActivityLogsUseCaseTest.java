package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.yd.vibecode.domain.admin.application.dto.response.MasterActivityLogPageResponse;
import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLog;
import com.yd.vibecode.domain.admin.domain.entity.MasterActivityLogType;
import com.yd.vibecode.domain.admin.domain.repository.MasterActivityLogRepository;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

@ExtendWith(MockitoExtension.class)
class GetMasterActivityLogsUseCaseTest {

    @InjectMocks
    private GetMasterActivityLogsUseCase getMasterActivityLogsUseCase;

    @Mock
    private AdminService adminService;

    @Mock
    private MasterActivityLogRepository masterActivityLogRepository;

    @Test
    @DisplayName("MASTER는 활동 로그를 조회할 수 있다")
    void execute_master_success() {
        Long masterId = 1L;
        Admin master = Admin.builder().role(AdminRole.MASTER).build();

        MasterActivityLog log = MasterActivityLog.builder()
                .masterId(masterId)
                .type(MasterActivityLogType.ADMIN_SIGNUP_CODE_ISSUED)
                .title("관리자 가입 번호가 발급되었습니다")
                .message("새 관리자가 가입할 수 있는 가입 번호가 발급되었습니다.")
                .build();
        ReflectionTestUtils.setField(log, "createdAt", LocalDateTime.now());

        Page<MasterActivityLog> page = new PageImpl<>(List.of(log));
        given(adminService.findById(masterId)).willReturn(master);
        given(masterActivityLogRepository.search(isNull(), isNull(), any(Pageable.class))).willReturn(page);

        MasterActivityLogPageResponse response = getMasterActivityLogsUseCase.execute(masterId, null, null, 0, 20);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).type()).isEqualTo(MasterActivityLogType.ADMIN_SIGNUP_CODE_ISSUED);
    }

    @Test
    @DisplayName("일반 ADMIN은 MASTER_ONLY 예외가 발생한다")
    void execute_admin_throwsMasterOnly() {
        Long adminId = 2L;
        Admin admin = Admin.builder().role(AdminRole.ADMIN).build();
        given(adminService.findById(adminId)).willReturn(admin);

        assertThatThrownBy(() -> getMasterActivityLogsUseCase.execute(adminId, null, null, 0, 20))
                .isInstanceOf(RestApiException.class)
                .extracting(ex -> ((RestApiException) ex).getErrorCode().getCode())
                .isEqualTo(AuthErrorStatus.MASTER_ONLY.getCode().getCode());
    }

    @Test
    @DisplayName("size는 최대 100으로 제한된다")
    void execute_capsPageSize() {
        Admin master = Admin.builder().role(AdminRole.MASTER).build();
        given(adminService.findById(1L)).willReturn(master);
        given(masterActivityLogRepository.search(isNull(), isNull(), org.mockito.ArgumentMatchers.argThat(p -> p.getPageSize() == 100)))
                .willReturn(new PageImpl<>(List.of()));

        MasterActivityLogPageResponse response = getMasterActivityLogsUseCase.execute(1L, null, null, 0, 500);

        assertThat(response.size()).isEqualTo(100);
    }

    @Test
    @DisplayName("type 필터를 repository에 전달한다")
    void execute_passesTypeFilter() {
        Admin master = Admin.builder().role(AdminRole.MASTER).build();
        given(adminService.findById(1L)).willReturn(master);
        given(masterActivityLogRepository.search(
                eq(MasterActivityLogType.ADMIN_ACCOUNT_DELETED),
                isNull(),
                any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of()));

        MasterActivityLogPageResponse response = getMasterActivityLogsUseCase.execute(
                1L, null, MasterActivityLogType.ADMIN_ACCOUNT_DELETED, 0, 20);

        assertThat(response.content()).isEmpty();
    }
}
