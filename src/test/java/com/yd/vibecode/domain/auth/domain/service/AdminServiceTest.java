package com.yd.vibecode.domain.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.repository.AdminRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private AdminRepository adminRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("관리자 생성 성공")
    void create_success() {
        // given
        String adminNumber = "admin123";
        String email = "admin@example.com";
        String passwordHash = "encodedPassword";
        
        Admin admin = Admin.builder()
                .adminNumber(adminNumber)
                .email(email)
                .passwordHash(passwordHash)
                .build();

        given(adminRepository.save(any(Admin.class))).willReturn(admin);

        // when
        Admin result = adminService.create(adminNumber, "테스트 관리자", email, passwordHash);

        // then
        assertThat(result.getAdminNumber()).isEqualTo(adminNumber);
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    @DisplayName("비밀번호 검증 성공")
    void validatePassword_success() {
        // given
        Admin admin = Admin.builder().passwordHash("encoded").build();
        given(passwordEncoder.matches("raw", "encoded")).willReturn(true);

        // when & then
        assertDoesNotThrow(() -> adminService.validatePassword(admin, "raw"));
    }

    @Test
    @DisplayName("최근 로그인 시각 갱신 - save 없이 dirty checking")
    void recordLastLogin_updatesLastLoginAt_withoutSave() {
        Admin admin = Admin.builder()
                .adminNumber("admin123")
                .email("admin@example.com")
                .passwordHash("encoded")
                .build();

        adminService.recordLastLogin(admin);

        assertThat(admin.getLastLoginAt()).isNotNull();
        verify(adminRepository, never()).save(any(Admin.class));
    }

    @Test
    @DisplayName("비밀번호 검증 실패")
    void validatePassword_fail() {
        // given
        Admin admin = Admin.builder().passwordHash("encoded").build();
        given(passwordEncoder.matches("raw", "encoded")).willReturn(false);

        // when & then
        RestApiException exception = assertThrows(RestApiException.class, () -> adminService.validatePassword(admin, "raw"));
        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorStatus.LOGIN_ERROR.getCode());
    }
}
