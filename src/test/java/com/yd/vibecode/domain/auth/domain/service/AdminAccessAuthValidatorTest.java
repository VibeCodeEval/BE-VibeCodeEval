package com.yd.vibecode.domain.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.yd.vibecode.domain.auth.domain.entity.Admin;
import com.yd.vibecode.domain.auth.domain.repository.AdminRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;

@ExtendWith(MockitoExtension.class)
class AdminAccessAuthValidatorTest {

    @InjectMocks
    private AdminAccessAuthValidator adminAccessAuthValidator;

    @Mock
    private AdminRepository adminRepository;

    @Test
    @DisplayName("삭제된 관리자는 access token 인증에서 차단된다")
    void validateActiveForAuthentication_deletedAdmin_throws() {
        Admin admin = Admin.builder()
                .adminNumber("admin123")
                .email("admin@example.com")
                .passwordHash("encoded")
                .isActive(false)
                .build();
        ReflectionTestUtils.setField(admin, "id", 1L);
        admin.delete();

        given(adminRepository.findById(1L)).willReturn(Optional.of(admin));

        RestApiException exception = assertThrows(
                RestApiException.class,
                () -> adminAccessAuthValidator.validateActiveForAuthentication(1L));

        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorStatus.ADMIN_ACCOUNT_INACTIVE.getCode());
    }

    @Test
    @DisplayName("비활성 관리자는 access token 인증에서 차단된다")
    void validateActiveForAuthentication_inactiveAdmin_throws() {
        Admin admin = Admin.builder()
                .adminNumber("admin123")
                .email("admin@example.com")
                .passwordHash("encoded")
                .isActive(false)
                .build();
        ReflectionTestUtils.setField(admin, "id", 2L);

        given(adminRepository.findById(2L)).willReturn(Optional.of(admin));

        RestApiException exception = assertThrows(
                RestApiException.class,
                () -> adminAccessAuthValidator.validateActiveForAuthentication(2L));

        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorStatus.ADMIN_ACCOUNT_INACTIVE.getCode());
    }

    @Test
    @DisplayName("활성 관리자는 access token 인증 검증을 통과한다")
    void validateActiveForAuthentication_activeAdmin_passes() {
        Admin admin = Admin.builder()
                .adminNumber("admin123")
                .email("admin@example.com")
                .passwordHash("encoded")
                .isActive(true)
                .build();
        ReflectionTestUtils.setField(admin, "id", 3L);

        given(adminRepository.findById(3L)).willReturn(Optional.of(admin));

        assertDoesNotThrow(() -> adminAccessAuthValidator.validateActiveForAuthentication(3L));
    }
}
