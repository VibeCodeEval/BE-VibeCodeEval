package com.yd.vibecode.domain.admin.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.yd.vibecode.domain.admin.application.dto.request.UpdateEntryCodeRequest;
import com.yd.vibecode.domain.admin.application.dto.response.EntryCodeResponse;
import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.service.EntryCodeService;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateEntryCodeUseCaseTest {

    @Mock
    private EntryCodeService entryCodeService;

    @InjectMocks
    private UpdateEntryCodeUseCase updateEntryCodeUseCase;

    // ------------------------------------------------------------------ //
    // helpers
    // ------------------------------------------------------------------ //

    private EntryCode buildActiveEntryCode(String code) {
        return EntryCode.builder()
                .code(code)
                .examId(1L)
                .problemSetId(100L)
                .createdBy(1L)
                .label("Test Label")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .maxUses(10)
                .usedCount(0)
                .isActive(true)
                .build();
    }

    private EntryCode buildInactiveEntryCode(String code) {
        return EntryCode.builder()
                .code(code)
                .examId(1L)
                .problemSetId(100L)
                .createdBy(1L)
                .label("Test Label")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .maxUses(10)
                .usedCount(0)
                .isActive(false)
                .build();
    }

    // ------------------------------------------------------------------ //
    // 활성 → 비활성 (deactivate)
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("활성 코드를 비활성으로 변경 시 isActive=false 반환")
    void execute_deactivate_activeCode_success() {
        // given
        String code = "ACTIVE-CODE";
        EntryCode entryCode = buildActiveEntryCode(code);
        given(entryCodeService.findByCode(code)).willReturn(entryCode);

        UpdateEntryCodeRequest request = new UpdateEntryCodeRequest(false);

        // when
        EntryCodeResponse response = updateEntryCodeUseCase.execute(code, request);

        // then
        assertThat(response.isActive()).isFalse();
        assertThat(response.code()).isEqualTo(code);
        verify(entryCodeService).findByCode(code);
    }

    // ------------------------------------------------------------------ //
    // 비활성 → 활성 (reactivate)
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("비활성 코드를 활성으로 변경 시 isActive=true 반환")
    void execute_activate_inactiveCode_success() {
        // given
        String code = "INACTIVE-CODE";
        EntryCode entryCode = buildInactiveEntryCode(code);
        given(entryCodeService.findByCode(code)).willReturn(entryCode);

        UpdateEntryCodeRequest request = new UpdateEntryCodeRequest(true);

        // when
        EntryCodeResponse response = updateEntryCodeUseCase.execute(code, request);

        // then
        assertThat(response.isActive()).isTrue();
        assertThat(response.code()).isEqualTo(code);
        verify(entryCodeService).findByCode(code);
    }

    // ------------------------------------------------------------------ //
    // 활성 상태 유지 (no-op)
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("활성 코드에 isActive=true 요청 시 코드가 변경되지 않고 isActive=true 유지")
    void execute_keepActive_noChange() {
        // given
        String code = "STAY-ACTIVE";
        EntryCode entryCode = buildActiveEntryCode(code);
        given(entryCodeService.findByCode(code)).willReturn(entryCode);

        UpdateEntryCodeRequest request = new UpdateEntryCodeRequest(true);

        // when
        EntryCodeResponse response = updateEntryCodeUseCase.execute(code, request);

        // then
        assertThat(response.isActive()).isTrue();
        assertThat(response.code()).isEqualTo(code);
    }

    // ------------------------------------------------------------------ //
    // isActive=null → 상태 불변
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("isActive=null 요청 시 기존 상태(true)를 유지")
    void execute_nullIsActive_retainsCurrentState() {
        // given
        String code = "UNCHANGED";
        EntryCode entryCode = buildActiveEntryCode(code);
        given(entryCodeService.findByCode(code)).willReturn(entryCode);

        UpdateEntryCodeRequest request = new UpdateEntryCodeRequest(null);

        // when
        EntryCodeResponse response = updateEntryCodeUseCase.execute(code, request);

        // then
        // EntryCode.update(null) → 변경 없음 → 기존 true 유지
        assertThat(response.isActive()).isTrue();
    }

    // ------------------------------------------------------------------ //
    // 존재하지 않는 코드 → 예외
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("존재하지 않는 코드 조회 시 RestApiException(INVALID_CODE) 발생")
    void execute_notFoundCode_throwsRestApiException() {
        // given
        String unknownCode = "NOT-EXIST";
        given(entryCodeService.findByCode(unknownCode))
                .willThrow(new RestApiException(AuthErrorStatus.INVALID_CODE));

        UpdateEntryCodeRequest request = new UpdateEntryCodeRequest(false);

        // when & then
        RestApiException ex = assertThrows(RestApiException.class,
                () -> updateEntryCodeUseCase.execute(unknownCode, request));

        assertThat(ex.getErrorCode()).isEqualTo(AuthErrorStatus.INVALID_CODE.getCode());
    }

    // ------------------------------------------------------------------ //
    // 응답 필드 전체 검증
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("응답 DTO에 EntryCode의 모든 필드가 올바르게 매핑된다")
    void execute_responseFieldMapping_correct() {
        // given
        String code = "MAPPED-CODE";
        LocalDateTime expiresAt = LocalDateTime.of(2026, 12, 31, 23, 59);
        EntryCode entryCode = EntryCode.builder()
                .code(code)
                .examId(42L)
                .problemSetId(7L)
                .createdBy(3L)
                .label("My Label")
                .expiresAt(expiresAt)
                .maxUses(5)
                .usedCount(2)
                .isActive(true)
                .build();
        given(entryCodeService.findByCode(code)).willReturn(entryCode);

        UpdateEntryCodeRequest request = new UpdateEntryCodeRequest(false);

        // when
        EntryCodeResponse response = updateEntryCodeUseCase.execute(code, request);

        // then
        assertThat(response.code()).isEqualTo(code);
        assertThat(response.examId()).isEqualTo(42L);
        assertThat(response.problemSetId()).isEqualTo(7L);
        assertThat(response.label()).isEqualTo("My Label");
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
        assertThat(response.maxUses()).isEqualTo(5);
        assertThat(response.usedCount()).isEqualTo(2);
        assertThat(response.isActive()).isFalse();
    }
}
