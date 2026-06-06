package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.yd.vibecode.domain.admin.domain.service.MasterActivityLogService;
import com.yd.vibecode.domain.auth.application.usecase.AdminLogoutUseCase;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminRole;
import com.yd.vibecode.domain.auth.domain.service.AdminAccountDeletionService;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.util.CookieUtils;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class DeleteOwnAdminAccountUseCaseTest {

  private static final Long ADMIN_ID = 10L;
  private static final String ACCESS_TOKEN = "access-token";

  @InjectMocks
  private DeleteOwnAdminAccountUseCase deleteOwnAdminAccountUseCase;

  @Mock
  private AdminService adminService;

  @Mock
  private AdminAccountDeletionService adminAccountDeletionService;

  @Mock
  private MasterActivityLogService masterActivityLogService;

  @Mock
  private AdminLogoutUseCase adminLogoutUseCase;

  @Mock
  private CookieUtils cookieUtils;

  @Mock
  private HttpServletResponse httpResponse;

  @Test
  @DisplayName("일반 ADMIN 셀프 삭제 시 master_activity_logs에 ADMIN_ACCOUNT_DELETED를 기록한다")
  void execute_success_logsMasterActivity() {
    Admin admin = Admin.builder()
        .adminNumber("ADM-123456")
        .displayName("이관리")
        .role(AdminRole.ADMIN)
        .build();
    ReflectionTestUtils.setField(admin, "id", ADMIN_ID);

    given(adminService.findById(ADMIN_ID)).willReturn(admin);

    deleteOwnAdminAccountUseCase.execute(ADMIN_ID, ACCESS_TOKEN, httpResponse);

    verify(adminAccountDeletionService).softDelete(
        eq(admin),
        eq(ADMIN_ID),
        eq("DELETE_OWN_ACCOUNT"));
    verify(masterActivityLogService).logAdminAccountDeleted(isNull(), eq(ADMIN_ID), eq("이관리"));
    verify(adminLogoutUseCase).execute(ACCESS_TOKEN);
    verify(cookieUtils).clearAccessTokenCookie(httpResponse);
    verify(cookieUtils).clearRefreshTokenCookie(httpResponse);
  }

  @Test
  @DisplayName("MASTER 셀프 삭제 차단 시 master_activity_logs와 logout은 수행하지 않는다")
  void execute_masterAccount_throwsWithoutLogging() {
    Admin master = Admin.builder()
        .adminNumber("MASTER-0001")
        .displayName("마스터")
        .role(AdminRole.MASTER)
        .build();
    ReflectionTestUtils.setField(master, "id", ADMIN_ID);

    given(adminService.findById(ADMIN_ID)).willReturn(master);
    doThrow(new RestApiException(AuthErrorStatus.MASTER_ACCOUNT_CANNOT_BE_DEACTIVATED))
        .when(adminAccountDeletionService)
        .softDelete(eq(master), eq(ADMIN_ID), eq("DELETE_OWN_ACCOUNT"));

    assertThatThrownBy(() -> deleteOwnAdminAccountUseCase.execute(ADMIN_ID, ACCESS_TOKEN, httpResponse))
        .isInstanceOf(RestApiException.class)
        .extracting(ex -> ((RestApiException) ex).getErrorCode())
        .isEqualTo(AuthErrorStatus.MASTER_ACCOUNT_CANNOT_BE_DEACTIVATED.getCode());

    verify(masterActivityLogService, never()).logAdminAccountDeleted(
        org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.any());
    verify(adminLogoutUseCase, never()).execute(org.mockito.ArgumentMatchers.anyString());
    verify(cookieUtils, never()).clearAccessTokenCookie(httpResponse);
    verify(cookieUtils, never()).clearRefreshTokenCookie(httpResponse);
  }
}
