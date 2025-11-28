package com.yd.vibecode.domain.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.yd.vibecode.domain.auth.application.dto.request.AdminSignupRequest;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.entity.AdminNumber;
import com.yd.vibecode.domain.auth.domain.service.AdminNumberService;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminSignupUseCaseTest {

    @InjectMocks
    private AdminSignupUseCase adminSignupUseCase;

    @Mock
    private AdminService adminService;

    @Mock
    private AdminNumberService adminNumberService;

    @Test
    @DisplayName("관리자 회원가입 성공")
    void signup_success() {
        // given
        AdminSignupRequest request = new AdminSignupRequest(
                "admin123", "admin@example.com", "password");
        AdminNumber adminNumber = AdminNumber.builder()
                .adminNumber("admin123")
                .issuedBy(1L)
                .build();
        Admin savedAdmin = Admin.builder()
                .adminNumber("admin123")
                .email("admin@example.com")
                .passwordHash("encodedPassword")
                .build();
        given(adminNumberService.validateUsable(request.adminNumber())).willReturn(adminNumber);
        given(adminService.existsByAdminNumber(request.adminNumber())).willReturn(false);
        given(adminService.existsByEmail(request.email())).willReturn(false);
        given(adminService.encodePassword(request.password())).willReturn("encodedPassword");
        given(adminService.create(request.adminNumber(), request.email(), "encodedPassword"))
                .willReturn(savedAdmin);

        // when
        adminSignupUseCase.execute(request);

        // then
        verify(adminNumberService).assign(adminNumber, savedAdmin.getId());
        verify(adminService).create(request.adminNumber(), request.email(), "encodedPassword");
    }

    @Test
    @DisplayName("관리자 회원가입 실패 - 이미 존재하는 관리자 번호")
    void signup_fail_duplicate_admin_number() {
        // given
        AdminSignupRequest request = new AdminSignupRequest(
                "admin123", "admin@example.com", "password");
        AdminNumber adminNumber = AdminNumber.builder()
                .adminNumber("admin123")
                .issuedBy(1L)
                .build();
        given(adminNumberService.validateUsable(request.adminNumber())).willReturn(adminNumber);
        given(adminService.existsByAdminNumber(request.adminNumber())).willReturn(true);

        // when & then
        RestApiException exception = assertThrows(
                RestApiException.class,
                () -> adminSignupUseCase.execute(request)
        );
        assertThat(exception.getErrorCode())
                .isEqualTo(AuthErrorStatus.ALREADY_REGISTERED_ADMIN_NUMBER.getCode());
    }

    @Test
    @DisplayName("관리자 회원가입 실패 - 이미 존재하는 이메일")
    void signup_fail_duplicate_email() {
        // given
        AdminSignupRequest request = new AdminSignupRequest(
                "admin123", "admin@example.com", "password");
        AdminNumber adminNumber = AdminNumber.builder()
                .adminNumber("admin123")
                .issuedBy(1L)
                .build();
        given(adminNumberService.validateUsable(request.adminNumber())).willReturn(adminNumber);
        given(adminService.existsByAdminNumber(request.adminNumber())).willReturn(false);
        given(adminService.existsByEmail(request.email())).willReturn(true);

        // when & then
        RestApiException exception = assertThrows(
                RestApiException.class,
                () -> adminSignupUseCase.execute(request)
        );
        assertThat(exception.getErrorCode())
                .isEqualTo(AuthErrorStatus.ALREADY_REGISTERED_EMAIL.getCode());
    }
}
