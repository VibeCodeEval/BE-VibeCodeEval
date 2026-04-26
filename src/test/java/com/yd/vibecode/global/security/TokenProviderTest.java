package com.yd.vibecode.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.TimeZone;

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

    @Test
    @DisplayName("UTC 환경에서 생성한 access token도 즉시 만료되지 않아야 한다")
    void create_access_token_does_not_expire_immediately_in_utc_environment() {
        TimeZone originalTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            TokenProvider tokenProvider = new TokenProvider(jwtProperties());

            String accessToken = tokenProvider.createAccessToken("1", "ADMIN");

            assertThat(tokenProvider.getId(accessToken)).contains("1");
        } finally {
            TimeZone.setDefault(originalTimeZone);
        }
    }

    private JwtProperties jwtProperties() {
        JwtProperties props = new JwtProperties();
        props.setKey("test-secret-key-32-characters-long!!");
        props.setAccessTokenExpirationPeriodDay(3_600_000L);
        props.setRefreshTokenExpirationPeriodDay(86_400_000L);
        return props;
    }
}
