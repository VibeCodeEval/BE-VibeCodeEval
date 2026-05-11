package com.yd.vibecode.global.interceptor;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.yd.vibecode.global.util.CookieUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CookieHandshakeInterceptor implements HandshakeInterceptor {

    private final CookieUtils cookieUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = cookieUtils.getAccessTokenFromRequest(servletRequest.getServletRequest());
            if (token != null) {
                attributes.put(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, token);
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
    }
}
