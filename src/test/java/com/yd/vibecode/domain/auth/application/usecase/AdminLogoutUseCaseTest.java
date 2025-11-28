package com.yd.vibecode.domain.auth.application.usecase;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.yd.vibecode.domain.auth.domain.service.RefreshTokenService;
import com.yd.vibecode.domain.auth.domain.service.TokenBlacklistService;
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
class AdminLogoutUseCaseTest {

    @InjectMocks
    private AdminLogoutUseCase adminLogoutUseCase;

    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("관리자 로그아웃 성공")
    void logout_success() {
        // given
        String token = "accessToken";
        given(tokenProvider.getId(token)).willReturn(Optional.of("1"));
        given(tokenProvider.getRemainingDuration(token)).willReturn(Optional.of(Duration.ofMinutes(30)));

        // when
        adminLogoutUseCase.execute(token);

        // then
        verify(refreshTokenService).deleteRefreshToken("1");
        verify(tokenBlacklistService).blacklist(token, Duration.ofMinutes(30));
    }
}


