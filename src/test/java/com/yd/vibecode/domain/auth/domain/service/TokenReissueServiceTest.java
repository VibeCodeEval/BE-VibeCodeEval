package com.yd.vibecode.domain.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.yd.vibecode.domain.auth.application.dto.response.TokenReissueResponse;
import com.yd.vibecode.global.exception.RestApiException;
import com.yd.vibecode.global.exception.code.status.AuthErrorStatus;
import com.yd.vibecode.global.security.TokenProvider;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenReissueServiceTest {

    @InjectMocks
    private TokenReissueService tokenReissueService;

    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissue_success() {
        // given
        String refreshToken = "refreshToken";
        String userId = "100";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";
        Duration duration = Duration.ofDays(7);

        given(refreshTokenService.isExist(refreshToken, userId)).willReturn(true);
        given(tokenProvider.createAccessToken(userId)).willReturn(newAccessToken);
        given(tokenProvider.createRefreshToken(userId)).willReturn(newRefreshToken);
        given(tokenProvider.getRemainingDuration(refreshToken)).willReturn(Optional.of(duration));

        // when
        TokenReissueResponse response = tokenReissueService.reissue(refreshToken, userId);

        // then
        assertThat(response.accessToken()).isEqualTo(newAccessToken);
        assertThat(response.refreshToken()).isEqualTo(newRefreshToken);
        verify(refreshTokenService).deleteRefreshToken(userId);
        verify(refreshTokenService).saveRefreshToken(userId, newRefreshToken, duration);
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 리프레시 토큰")
    void reissue_fail_invalid_token() {
        // given
        String refreshToken = "invalidToken";
        String userId = "100";

        given(refreshTokenService.isExist(refreshToken, userId)).willReturn(false);

        // when & then
        RestApiException exception = assertThrows(RestApiException.class, () -> tokenReissueService.reissue(refreshToken, userId));
        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorStatus.INVALID_REFRESH_TOKEN.getCode());
    }
}
