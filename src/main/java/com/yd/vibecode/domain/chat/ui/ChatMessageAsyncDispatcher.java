package com.yd.vibecode.domain.chat.ui;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.yd.vibecode.domain.chat.application.dto.request.SaveChatMessageRequest;
import com.yd.vibecode.domain.chat.application.dto.response.SendMessageResponse;
import com.yd.vibecode.domain.chat.application.usecase.SaveChatMessageUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 메시지 비동기 처리기.
 * STOMP 인바운드 스레드를 즉시 반환시키기 위해 AI 처리를 별도 스레드 풀에서 수행하고,
 * 결과(또는 에러)를 사용자 큐로 push 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageAsyncDispatcher {

    private final SaveChatMessageUseCase saveChatMessageUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    @Async("chatAsyncExecutor")
    public void dispatch(SaveChatMessageRequest request) {
        if (request == null || request.participantId() == null) {
            log.warn("[WS Chat] Invalid chat request: participantId is null");
            return;
        }
        String userId = request.participantId().toString();
        try {
            SendMessageResponse response = saveChatMessageUseCase.execute(request);
            messagingTemplate.convertAndSendToUser(userId, "/queue/chat", response);
            log.info("[WS Chat] AI response sent: userId={}, turnId={}", userId, response.turnId());
        } catch (Exception e) {
            log.error("[WS Chat] Failed to process message: userId={}, error={}", userId, e.getMessage(), e);
            messagingTemplate.convertAndSendToUser(
                    userId, "/queue/chat-error",
                    Map.of("error", true, "message", "AI 응답 처리 실패"));
        }
    }
}
