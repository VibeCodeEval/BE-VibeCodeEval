package com.yd.vibecode.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP 설정
 *
 * 연결 흐름:
 * 1. 클라이언트가 ws://host/ws 에 SockJS/STOMP 연결
 * 2. /topic/exam/{examId} 토픽 구독
 * 3. 시험 시작/종료/연장 시 서버가 해당 토픽으로 ExamStateEvent 브로드캐스트
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독할 브로커 주소: /topic (공통), /queue (개인)
        registry.enableSimpleBroker("/topic", "/queue");
        // 클라이언트 → 서버 메시지 전송 prefix
        registry.setApplicationDestinationPrefixes("/app");
        // 특정 사용자에게 메시지 보낼 때 prefix
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
