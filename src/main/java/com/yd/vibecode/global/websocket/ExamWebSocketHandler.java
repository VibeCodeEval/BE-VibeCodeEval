package com.yd.vibecode.global.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Handler
 * - 클라이언트 연결 관리
 * - 메시지 브로드캐스트
 */
@Slf4j
@Component
public class ExamWebSocketHandler extends TextWebSocketHandler {

    // examId -> Set<WebSocketSession>
    private final Map<Long, Map<String, WebSocketSession>> examSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 쿼리 파라미터에서 examId 추출
        String query = session.getUri().getQuery();
        Long examId = extractExamId(query);
        
        if (examId != null) {
            examSessions.computeIfAbsent(examId, k -> new ConcurrentHashMap<>())
                       .put(session.getId(), session);
            log.info("WebSocket connected: sessionId={}, examId={}", session.getId(), examId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 모든 examId에서 해당 세션 제거
        examSessions.values().forEach(sessions -> sessions.remove(session.getId()));
        log.info("WebSocket disconnected: sessionId={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 클라이언트로부터 메시지 수신 (필요시 구현)
        log.debug("Received message: {}", message.getPayload());
    }

    /**
     * 특정 시험의 모든 클라이언트에게 메시지 브로드캐스트
     */
    public void broadcast(Long examId, String message) {
        Map<String, WebSocketSession> sessions = examSessions.get(examId);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("No active sessions for examId={}", examId);
            return;
        }

        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                log.error("Failed to send message to session: {}", session.getId(), e);
            }
        });

        log.info("Broadcasted message to {} sessions for examId={}", sessions.size(), examId);
    }

    /**
     * 쿼리 파라미터에서 examId 추출
     */
    private Long extractExamId(String query) {
        if (query == null || !query.contains("examId=")) {
            return null;
        }
        
        try {
            String examIdStr = query.split("examId=")[1].split("&")[0];
            return Long.parseLong(examIdStr);
        } catch (Exception e) {
            log.error("Failed to extract examId from query: {}", query, e);
            return null;
        }
    }
}
