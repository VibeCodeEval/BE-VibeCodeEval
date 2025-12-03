package com.yd.vibecode.domain.auth.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AdminNumber 엔티티 테스트")
class AdminNumberTest {

    @Test
    @DisplayName("AdminNumber 생성 성공")
    void createAdminNumber_success() {
        // given & when
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        AdminNumber adminNumber = AdminNumber.builder()
            .adminNumber("ADM-123456")
            .label("Test Admin")
            .isActive(true)
            .issuedBy(1L)
            .expiresAt(expiresAt)
            .build();

        // then
        assertThat(adminNumber.getAdminNumber()).isEqualTo("ADM-123456");
        assertThat(adminNumber.getLabel()).isEqualTo("Test Admin");
        assertThat(adminNumber.getIsActive()).isTrue();
        assertThat(adminNumber.getIssuedBy()).isEqualTo(1L);
        assertThat(adminNumber.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("관리자 번호 할당 처리 성공")
    void assign_success() {
        // given
        AdminNumber adminNumber = AdminNumber.builder()
            .adminNumber("ADM-123456")
            .label("Test Admin")
            .isActive(true)
            .issuedBy(1L)
            .build();

        LocalDateTime now = LocalDateTime.now();

        // when
        adminNumber.assign(10L, now);

        // then
        assertThat(adminNumber.getAssignedAdminId()).isEqualTo(10L);
        assertThat(adminNumber.getUsedAt()).isEqualTo(now);
        assertThat(adminNumber.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("관리자 번호 업데이트 성공")
    void update_success() {
        // given
        AdminNumber adminNumber = AdminNumber.builder()
            .adminNumber("ADM-123456")
            .label("Old Label")
            .isActive(true)
            .issuedBy(1L)
            .build();

        LocalDateTime newExpiry = LocalDateTime.now().plusDays(60);

        // when
        adminNumber.update("New Label", false, newExpiry);

        // then
        assertThat(adminNumber.getLabel()).isEqualTo("New Label");
        assertThat(adminNumber.getIsActive()).isFalse();
        assertThat(adminNumber.getExpiresAt()).isEqualTo(newExpiry);
    }

    @Test
    @DisplayName("사용 가능 여부 확인 - 사용 가능")
    void isUsable_usable_returnsTrue() {
        // given
        AdminNumber adminNumber = AdminNumber.builder()
            .adminNumber("ADM-123456")
            .label("Test Admin")
            .isActive(true)
            .issuedBy(1L)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .build();

        // when & then
        assertThat(adminNumber.isUsable()).isTrue();
    }

    @Test
    @DisplayName("사용 가능 여부 확인 - 비활성 상태")
    void isUsable_inactive_returnsFalse() {
        // given
        AdminNumber adminNumber = AdminNumber.builder()
            .adminNumber("ADM-123456")
            .label("Test Admin")
            .isActive(false)
            .issuedBy(1L)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .build();

        // when & then
        assertThat(adminNumber.isUsable()).isFalse();
    }

    @Test
    @DisplayName("사용 가능 여부 확인 - 이미 할당됨")
    void isUsable_assigned_returnsFalse() {
        // given
        AdminNumber adminNumber = AdminNumber.builder()
            .adminNumber("ADM-123456")
            .label("Test Admin")
            .isActive(true)
            .issuedBy(1L)
            .assignedAdminId(10L)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .build();

        // when & then
        assertThat(adminNumber.isUsable()).isFalse();
    }

    @Test
    @DisplayName("사용 가능 여부 확인 - 만료됨")
    void isUsable_expired_returnsFalse() {
        // given
        AdminNumber adminNumber = AdminNumber.builder()
            .adminNumber("ADM-123456")
            .label("Test Admin")
            .isActive(true)
            .issuedBy(1L)
            .expiresAt(LocalDateTime.now().minusDays(1))
            .build();

        // when & then
        assertThat(adminNumber.isUsable()).isFalse();
    }

    @Test
    @DisplayName("기본값 설정 - isActive가 null일 때 true로 설정")
    void createAdminNumber_nullIsActive_defaultsToTrue() {
        // given & when
        AdminNumber adminNumber = AdminNumber.builder()
            .adminNumber("ADM-123456")
            .label("Test Admin")
            .isActive(null)
            .issuedBy(1L)
            .build();

        // then
        assertThat(adminNumber.getIsActive()).isTrue();
    }
}
