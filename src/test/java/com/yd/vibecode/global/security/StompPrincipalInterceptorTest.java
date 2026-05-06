package com.yd.vibecode.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import com.yd.vibecode.global.util.CookieUtils;

@ExtendWith(MockitoExtension.class)
class StompPrincipalInterceptorTest {

    @InjectMocks
    private StompPrincipalInterceptor interceptor;

    @Mock
    private TokenProvider tokenProvider;

    private Message<?> buildConnectMessage(String authHeader, Map<String, Object> sessionAttributes) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (authHeader != null) {
            accessor.addNativeHeader("Authorization", authHeader);
        }
        if (sessionAttributes != null) {
            accessor.setSessionAttributes(sessionAttributes);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    // -------------------------------------------------------------------------
    // 1. 쿠키 기반 인증 (세션 attribute)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("세션 attribute의 쿠키 토큰으로 Principal 설정 성공")
    void preSend_sets_principal_from_cookie_session_attribute() {
        // given
        String token = "valid-cookie-token";
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, token);

        Message<?> message = buildConnectMessage(null, sessionAttrs);

        given(tokenProvider.validateToken(token)).willReturn(true);
        given(tokenProvider.getId(token)).willReturn(Optional.of("100"));

        // when
        Message<?> result = interceptor.preSend(message, null);

        // then
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNotNull();
        assertThat(resultAccessor.getUser().getName()).isEqualTo("100");
    }

    // -------------------------------------------------------------------------
    // 2. Authorization 헤더 기반 인증 (폴백)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Authorization Bearer 헤더로 Principal 설정 성공 (쿠키 없는 경우)")
    void preSend_sets_principal_from_bearer_header_when_no_cookie() {
        // given
        String token = "valid-header-token";
        Message<?> message = buildConnectMessage("Bearer " + token, new HashMap<>());

        given(tokenProvider.validateToken(token)).willReturn(true);
        given(tokenProvider.getId(token)).willReturn(Optional.of("200"));

        // when
        Message<?> result = interceptor.preSend(message, null);

        // then
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNotNull();
        assertThat(resultAccessor.getUser().getName()).isEqualTo("200");
    }

    // -------------------------------------------------------------------------
    // 3. 쿠키 우선 순위
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("쿠키와 헤더 둘 다 있으면 쿠키 토큰 우선 사용")
    void preSend_prefers_cookie_over_bearer_header() {
        // given
        String cookieToken = "cookie-token";
        String headerToken = "header-token";

        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, cookieToken);

        Message<?> message = buildConnectMessage("Bearer " + headerToken, sessionAttrs);

        given(tokenProvider.validateToken(cookieToken)).willReturn(true);
        given(tokenProvider.getId(cookieToken)).willReturn(Optional.of("300"));

        // when
        Message<?> result = interceptor.preSend(message, null);

        // then — headerToken은 검증되지 않아야 함, cookieToken으로 설정
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser().getName()).isEqualTo("300");
    }

    // -------------------------------------------------------------------------
    // 4. 유효하지 않은 토큰 — 연결 거부
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("유효하지 않은 토큰이면 MessageDeliveryException 발생")
    void preSend_throws_when_token_is_invalid() {
        // given
        String invalidToken = "invalid-token";
        Message<?> message = buildConnectMessage("Bearer " + invalidToken, new HashMap<>());

        given(tokenProvider.validateToken(invalidToken)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(MessageDeliveryException.class);
    }

    // -------------------------------------------------------------------------
    // 5. 토큰 없는 경우 — 통과 (Principal 미설정)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("토큰이 전혀 없으면 Principal 미설정 상태로 통과")
    void preSend_passes_through_when_no_token() {
        // given — sessionAttributes 없음, Authorization 헤더 없음
        Message<?> message = buildConnectMessage(null, new HashMap<>());

        // when
        Message<?> result = interceptor.preSend(message, null);

        // then — 예외 없이 반환, Principal null
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNull();
    }

    // -------------------------------------------------------------------------
    // 6. CONNECT 아닌 프레임은 그대로 통과
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("SUBSCRIBE 프레임은 토큰 검사 없이 그대로 통과")
    void preSend_passes_non_connect_frames_without_processing() {
        // given
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setLeaveMutable(true);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when — tokenProvider 호출 없이 통과되어야 함
        Message<?> result = interceptor.preSend(message, null);

        // then
        assertThat(result).isNotNull();
    }
}
