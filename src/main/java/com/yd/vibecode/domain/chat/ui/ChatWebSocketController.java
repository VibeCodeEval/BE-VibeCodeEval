package com.yd.vibecode.domain.chat.ui;

import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Controller;

import com.yd.vibecode.domain.chat.application.dto.request.SaveChatMessageRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageAsyncDispatcher chatMessageAsyncDispatcher;
    private final SimpMessagingTemplate messagingTemplate;

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
        try {
            chatMessageAsyncDispatcher.dispatch(request);
        } catch (TaskRejectedException e) {
            log.error("[WS Chat] Executor queue full, rejecting request: userId={}", request.participantId());
            if (request.participantId() != null) {
                messagingTemplate.convertAndSendToUser(
                        request.participantId().toString(), "/queue/chat-error",
                        Map.of("error", true, "message", "서버가 혼잡합니다. 잠시 후 다시 시도해주세요."));
            }
        }
    }
}
