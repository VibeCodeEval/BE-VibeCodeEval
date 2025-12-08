package com.yd.vibecode.domain.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.yd.vibecode.domain.auth.application.dto.request.AdminLoginRequest;
import com.yd.vibecode.domain.auth.application.dto.response.AdminLoginResponse;
import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.service.AdminService;
import com.yd.vibecode.global.security.TokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminLoginUseCaseTest {

    @InjectMocks
    private AdminLoginUseCase adminLoginUseCase;

    @Mock
    private AdminService adminService;

    @Mock
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("관리자 로그인 성공 - 이메일")
    void login_success_email() {
        // given
        AdminLoginRequest request = new AdminLoginRequest("admin@example.com", "password");
        Admin admin = Admin.builder()
                .adminNumber("admin123")
                .email("admin@example.com")
                .passwordHash("encodedPassword")
                .build();
        ReflectionTestUtils.setField(admin, "id", 1L);

        given(adminService.findByEmail(request.identifier())).willReturn(admin);
        given(tokenProvider.createAccessToken(anyString(), anyString())).willReturn("accessToken");

        // when
        AdminLoginResponse response = adminLoginUseCase.execute(request);

        // then
        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.role()).isEqualTo("ADMIN");
        assertThat(response.admin().email()).isEqualTo("admin@example.com");
        verify(adminService).validatePassword(admin, "password");
    }

    @Test
    @DisplayName("관리자 로그인 성공 - 관리자 번호")
    void login_success_admin_number() {
        // given
        AdminLoginRequest request = new AdminLoginRequest("admin123", "password");
        Admin admin = Admin.builder()
                .adminNumber("admin123")
                .email("admin@example.com")
                .passwordHash("encodedPassword")
                .build();
        ReflectionTestUtils.setField(admin, "id", 1L);

        given(adminService.findByAdminNumber(request.identifier())).willReturn(admin);
        given(tokenProvider.createAccessToken(anyString(), anyString())).willReturn("accessToken");

        // when
        AdminLoginResponse response = adminLoginUseCase.execute(request);

        // then
        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.role()).isEqualTo("ADMIN");
        assertThat(response.admin().adminNumber()).isEqualTo("admin123");
        verify(adminService).validatePassword(admin, "password");
    }
}
