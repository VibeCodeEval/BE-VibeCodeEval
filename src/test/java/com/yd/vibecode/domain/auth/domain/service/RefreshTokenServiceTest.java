package com.yd.vibecode.domain.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("리프레시 토큰 저장")
    void saveRefreshToken_success() {
        // given
        String userId = "100";
        String refreshToken = "token";
        Duration duration = Duration.ofDays(7);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        refreshTokenService.saveRefreshToken(userId, refreshToken, duration);

        // then
        verify(valueOperations).set(eq("REFRESH_TOKEN:100"), eq(refreshToken), eq(duration));
    }

    @Test
    @DisplayName("리프레시 토큰 존재 확인")
    void isExist_success() {
        // given
        String userId = "100";
        String refreshToken = "token";
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("REFRESH_TOKEN:100")).willReturn(refreshToken);

        // when
        boolean result = refreshTokenService.isExist(refreshToken, userId);

        // then
        assertThat(result).isTrue();
    }
}
