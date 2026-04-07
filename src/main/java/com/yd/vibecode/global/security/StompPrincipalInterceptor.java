package com.yd.vibecode.global.security;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * STOMP CONNECT 프레임에서 JWT 토큰을 추출해 Principal을 설정하는 인터셉터.
 *
 * 동작 흐름:
 * 1. 클라이언트가 STOMP CONNECT 시 헤더에 Authorization: Bearer {token} 전달
 * 2. 이 인터셉터가 토큰을 파싱해 userId(participantId 또는 adminId) 추출
 * 3. 추출된 userId를 Principal name으로 설정
 * 4. Spring STOMP 브로커가 convertAndSendToUser(userId, ...) 호출 시
 *    해당 세션을 올바르게 찾아 메시지를 라우팅
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompPrincipalInterceptor implements ChannelInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (tokenProvider.validateToken(token)) {
                    tokenProvider.getId(token).ifPresent(userId -> {
                        Principal principal = () -> userId;
                        accessor.setUser(principal);
                        log.debug("[STOMP] Principal 설정: userId={}", userId);
                    });
                } else {
                    log.warn("[STOMP] 유효하지 않은 JWT 토큰으로 CONNECT 시도 - 연결 거부");
                    // Principal 미설정 시 convertAndSendToUser가 무음 실패하므로 명시적 거부
                    throw new org.springframework.messaging.MessageDeliveryException(
                            message, "Invalid or expired JWT token");
                }
            }
        }

        return message;
    }
}
