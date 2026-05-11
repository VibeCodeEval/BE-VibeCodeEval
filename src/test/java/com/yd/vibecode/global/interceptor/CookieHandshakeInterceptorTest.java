package com.yd.vibecode.global.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.socket.WebSocketHandler;

import com.yd.vibecode.global.util.CookieUtils;

import jakarta.servlet.http.Cookie;

@ExtendWith(MockitoExtension.class)
class CookieHandshakeInterceptorTest {

    @InjectMocks
    private CookieHandshakeInterceptor cookieHandshakeInterceptor;

    @Mock
    private CookieUtils cookieUtils;

    @Mock
    private WebSocketHandler wsHandler;

    private MockHttpServletRequest mockRequest;
    private MockHttpServletResponse mockResponse;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        mockResponse = new MockHttpServletResponse();
        attributes = new HashMap<>();
    }

    @Test
    @DisplayName("access_token 쿠키 있으면 세션 attributes에 저장 후 true 반환")
    void beforeHandshake_stores_token_when_cookie_present() throws Exception {
        // given
        String token = "valid-jwt-token";
        mockRequest.setCookies(new Cookie(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, token));
        given(cookieUtils.getAccessTokenFromRequest(mockRequest)).willReturn(token);

        ServletServerHttpRequest request = new ServletServerHttpRequest(mockRequest);
        ServletServerHttpResponse response = new ServletServerHttpResponse(mockResponse);

        // when
        boolean result = cookieHandshakeInterceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        assertThat(result).isTrue();
        assertThat(attributes.get(CookieUtils.ACCESS_TOKEN_COOKIE_NAME)).isEqualTo(token);
    }

    @Test
    @DisplayName("access_token 쿠키 없으면 attributes 변경 없이 true 반환")
    void beforeHandshake_returns_true_without_modifying_attributes_when_no_cookie() throws Exception {
        // given
        given(cookieUtils.getAccessTokenFromRequest(mockRequest)).willReturn(null);

        ServletServerHttpRequest request = new ServletServerHttpRequest(mockRequest);
        ServletServerHttpResponse response = new ServletServerHttpResponse(mockResponse);

        // when
        boolean result = cookieHandshakeInterceptor.beforeHandshake(request, response, wsHandler, attributes);

        // then
        assertThat(result).isTrue();
        assertThat(attributes).doesNotContainKey(CookieUtils.ACCESS_TOKEN_COOKIE_NAME);
    }

    @Test
    @DisplayName("afterHandshake — 예외 없이 완료된다")
    void afterHandshake_completes_without_exception() {
        // given
        ServletServerHttpRequest request = new ServletServerHttpRequest(mockRequest);
        ServletServerHttpResponse response = new ServletServerHttpResponse(mockResponse);

        // when & then (예외 없이 완료되어야 함)
        cookieHandshakeInterceptor.afterHandshake(request, response, wsHandler, null);
    }
}
