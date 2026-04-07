package com.yd.vibecode.domain.chat.ui;

import com.yd.vibecode.domain.chat.application.dto.request.SaveChatMessageRequest;
import com.yd.vibecode.domain.chat.application.dto.response.SendMessageResponse;
import com.yd.vibecode.domain.chat.application.usecase.SaveChatMessageUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SaveChatMessageUseCase saveChatMessageUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅 메시지 전송 (WebSocket)
     * 클라이언트 전송 경로: /app/chat.send
     * 클라이언트 구독 경로: /user/queue/chat
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SaveChatMessageRequest request) {
        log.info("[WS Chat] Message received: userId={}, content={}", 
                request.participantId(), request.content());
        
        try {
            // 메시지 저장 및 AI 응답 생성 (기존 REST UseCase 재사용)
            SendMessageResponse response = saveChatMessageUseCase.execute(request);
            
            // 응답을 보낸 사용자에게만 전송 (/user/queue/chat)
            // Spring STOMP의 convertAndSendToUser는 사용자 식별자가 필요함.
            // 여기서는 participantId를 String으로 변환하여 사용하거나, 
            // SecurityContext가 연동되어 있으면 Principal을 사용함.
            // 일단 participantId를 명시적으로 사용하여 특정 큐로 보냄.
            
            String destination = "/queue/chat";
            messagingTemplate.convertAndSendToUser(
                    request.participantId().toString(), 
                    destination, 
                    response
            );
            
            log.info("[WS Chat] AI response sent: userId={}, turnId={}", 
                    request.participantId(), response.turnId());
            
        } catch (Exception e) {
            log.error("[WS Chat] Failed to process message: {}", e.getMessage(), e);
            // 에러 응답을 FE에 전송하여 무한 로딩 방지
            if (request.participantId() != null) {
                try {
                    messagingTemplate.convertAndSendToUser(
                            request.participantId().toString(),
                            "/queue/chat-error",
                            Map.of("error", true, "message", e.getMessage() != null ? e.getMessage() : "AI 응답 처리 실패")
                    );
                } catch (Exception sendError) {
                    log.error("[WS Chat] 에러 응답 전송 실패: {}", sendError.getMessage());
                }
            }
        }
    }
}
