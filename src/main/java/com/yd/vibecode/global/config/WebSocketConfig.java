package com.yd.vibecode.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.yd.vibecode.global.websocket.ExamWebSocketHandler;

import lombok.RequiredArgsConstructor;

/**
 * WebSocket 설정
 * - /ws/exam 엔드포인트로 WebSocket 연결
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ExamWebSocketHandler examWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(examWebSocketHandler, "/ws/exam")
                .setAllowedOrigins("*");  // CORS 설정 (프로덕션에서는 특정 도메인으로 제한)
    }
}
