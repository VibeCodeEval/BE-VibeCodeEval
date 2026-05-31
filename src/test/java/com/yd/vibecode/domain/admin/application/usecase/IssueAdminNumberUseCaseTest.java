package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.admin.application.dto.request.AdminNumberIssueRequest;
import com.yd.vibecode.domain.admin.domain.service.MasterActivityLogService;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminNumber;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;
import com.yd.vibecode.domain.auth.domain.service.AdminNumberService;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class IssueAdminNumberUseCaseTest {

    @InjectMocks
    private IssueAdminNumberUseCase issueAdminNumberUseCase;

    @Mock
    private AdminService adminService;

    @Mock
    private AdminNumberService adminNumberService;

    @Mock
    private MasterActivityLogService masterActivityLogService;

    @Test
    @DisplayName("MASTER가 관리자 가입 번호 발급 시 활동 로그를 기록한다")
    void execute_logsSignupCodeIssued() {
        Long masterId = 1L;
        Admin master = Admin.builder().role(AdminRole.MASTER).build();
        ReflectionTestUtils.setField(master, "id", masterId);

        AdminNumberIssueRequest request = new AdminNumberIssueRequest("운영팀", LocalDateTime.now().plusDays(7));
        AdminNumber issued = AdminNumber.builder()
                .adminNumber("ADM-SECRET-123")
                .issuedBy(masterId)
                .build();

        given(adminService.findById(masterId)).willReturn(master);
        given(adminNumberService.issue(masterId, request.label(), request.expiresAt())).willReturn(issued);

        issueAdminNumberUseCase.execute(masterId, request);

        verify(masterActivityLogService).logSignupCodeIssued(masterId);
    }

    @Test
    @DisplayName("MASTER가 아니면 발급할 수 없다")
    void execute_notMaster_throws() {
        Admin admin = Admin.builder().role(AdminRole.ADMIN).build();
        given(adminService.findById(2L)).willReturn(admin);

        assertThatThrownBy(() -> issueAdminNumberUseCase.execute(
                2L, new AdminNumberIssueRequest(null, null)))
                .isInstanceOf(RestApiException.class)
                .extracting(ex -> ((RestApiException) ex).getErrorCode().getCode())
                .isEqualTo(AuthErrorStatus.MASTER_ONLY.getCode().getCode());
    }
}
