package com.yd.vibecode.domain.auth.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Admin 엔티티 테스트")
class AdminTest {

    @Test
    @DisplayName("Admin 생성 성공")
    void createAdmin_success() {
        // given & when
        Admin admin = Admin.builder()
            .email("admin@test.com")
            .passwordHash("encryptedPassword")
            .role(AdminRole.MASTER)
            .build();

        // then
        assertThat(admin.getEmail()).isEqualTo("admin@test.com");
        assertThat(admin.getPasswordHash()).isEqualTo("encryptedPassword");
        assertThat(admin.getRole()).isEqualTo(AdminRole.MASTER);
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePassword_success() {
        // given
        Admin admin = Admin.builder()
            .email("admin@test.com")
            .passwordHash("oldPassword")
            .role(AdminRole.ADMIN)
            .build();

        // when
        admin.updatePassword("newPassword");

        // then
        assertThat(admin.getPasswordHash()).isEqualTo("newPassword");
    }

    @Test
    @DisplayName("role이 null일 때 ADMIN으로 기본 설정")
    void createAdmin_nullRole_defaultsToAdmin() {
        // given & when
        Admin admin = Admin.builder()
            .email("admin@test.com")
            .passwordHash("password")
            .role(null)
            .build();

        // then
        assertThat(admin.getRole()).isEqualTo(AdminRole.ADMIN);
    }

    @Test
    @DisplayName("MASTER 역할 확인")
    void isMaster_returnsTrueForMaster() {
        // given
        Admin admin = Admin.builder()
            .email("master@test.com")
            .passwordHash("password")
            .role(AdminRole.MASTER)
            .build();

        // when & then
        assertThat(admin.isMaster()).isTrue();
        assertThat(admin.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("ADMIN 역할 확인")
    void isAdmin_returnsTrueForAdmin() {
        // given
        Admin admin = Admin.builder()
            .email("admin@test.com")
            .passwordHash("password")
            .role(AdminRole.ADMIN)
            .build();

        // when & then
        assertThat(admin.isAdmin()).isTrue();
        assertThat(admin.isMaster()).isFalse();
    }
}
