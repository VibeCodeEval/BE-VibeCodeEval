package com.yd.vibecode.domain.auth.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
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
class TokenBlacklistServiceTest {

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("토큰 블랙리스트 등록")
    void blacklist_success() {
        // given
        String token = "token";
        Duration duration = Duration.ofMinutes(30);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        tokenBlacklistService.blacklist(token, duration);

        // then
        verify(valueOperations).set(eq("BLACKLIST:token"), eq(token), eq(duration));
    }

    @Test
    @DisplayName("토큰 블랙리스트 확인")
    void isBlacklistToken_success() {
        // given
        String token = "token";
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("BLACKLIST:token")).willReturn(token);

        // when
        boolean result = tokenBlacklistService.isBlacklistToken(token);

        // then
        assertThat(result).isTrue();
    }
}
