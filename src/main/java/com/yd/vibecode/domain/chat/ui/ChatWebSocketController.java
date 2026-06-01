package com.yd.vibecode.domain.chat.ui;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.yd.vibecode.domain.chat.application.dto.request.SaveChatMessageRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageAsyncDispatcher chatMessageAsyncDispatcher;

    /**
     * 채팅 메시지 전송 (WebSocket)
     * 클라이언트 전송 경로: /app/chat.send
     * 클라이언트 구독 경로: /user/queue/chat (응답), /user/queue/chat-error (에러)
     *
     * STOMP 인바운드 스레드를 블로킹하지 않도록 즉시 비동기 디스패치한다.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SaveChatMessageRequest request) {
        log.info("[WS Chat] Message received: userId={}", request.participantId());
        chatMessageAsyncDispatcher.dispatch(request);
    }
}
