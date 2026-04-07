package com.yd.vibecode.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.yd.vibecode.global.config.properties.CorsProperties;
import com.yd.vibecode.global.security.StompPrincipalInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * WebSocket STOMP 설정
 *
 * 연결 흐름:
 * 1. 클라이언트가 ws://host/ws 에 SockJS/STOMP 연결 (Authorization 헤더에 JWT 포함)
 * 2. StompPrincipalInterceptor가 JWT를 파싱해 userId를 Principal로 설정
 * 3. /topic/exam/{examId} 토픽 구독 (시험 상태 변경 브로드캐스트)
 * 4. /user/queue/chat 구독 (개인 AI 채팅 응답)
 * 5. 시험 시작/종료/연장 시 서버가 /topic/exam/{examId}로 ExamStateEvent 브로드캐스트
 * 6. 채팅 메시지 응답은 convertAndSendToUser(userId, "/queue/chat", response)로 라우팅
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final CorsProperties corsProperties;
    private final StompPrincipalInterceptor stompPrincipalInterceptor;

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
        String[] allowedOrigins = corsProperties.getAllowedOrigins().split(",");
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS();
    }

    /**
     * 인바운드 채널 인터셉터 등록
     * STOMP CONNECT 시 JWT를 파싱해 Principal(userId)을 설정
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompPrincipalInterceptor);
    }
}
