package com.yd.vibecode.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenProviderTest {

    @Test
    @DisplayName("refresh token은 같은 초에 연속 발급해도 서로 달라야 한다")
    void create_refresh_token_generates_unique_token() {
        TokenProvider tokenProvider = new TokenProvider(jwtProperties());

        String first = tokenProvider.createRefreshToken("1");
        String second = tokenProvider.createRefreshToken("1");

        assertThat(second).isNotEqualTo(first);
    }

    private JwtProperties jwtProperties() {
        JwtProperties props = new JwtProperties();
        props.setKey("test-secret-key-32-characters-long!!");
        props.setAccessTokenExpirationPeriodDay(3_600_000L);
        props.setRefreshTokenExpirationPeriodDay(86_400_000L);
        return props;
    }
}
