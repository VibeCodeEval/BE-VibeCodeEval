package com.yd.vibecode.domain.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.yd.vibecode.domain.auth.domain.entity.EntryCode;
import com.yd.vibecode.domain.auth.domain.repository.EntryCodeRepository;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntryCodeServiceTest {

    @InjectMocks
    private EntryCodeService entryCodeService;

    @Mock
    private EntryCodeRepository entryCodeRepository;

    @Test
    @DisplayName("입장코드 검증 성공")
    void validateEntryCode_success() {
        // given
        EntryCode entryCode = EntryCode.builder()
                .code("CODE")
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .maxUses(0)
                .usedCount(0)
                .build();

        // when & then
        assertDoesNotThrow(() -> entryCodeService.validateEntryCode(entryCode));
    }

    @Test
    @DisplayName("입장코드 검증 실패 - 만료됨")
    void validateEntryCode_fail_expired() {
        // given
        EntryCode entryCode = EntryCode.builder()
                .code("CODE")
                .isActive(true)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        // when & then
        RestApiException exception = assertThrows(RestApiException.class, () -> entryCodeService.validateEntryCode(entryCode));
        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorStatus.CODE_EXPIRED.getCode());
    }
}
