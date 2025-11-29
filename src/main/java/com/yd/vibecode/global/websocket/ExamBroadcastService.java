package com.yd.vibecode.global.websocket;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yd.vibecode.domain.exam.domain.entity.Exam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Exam 브로드캐스트 서비스
 * - 시험 상태 변경 시 WebSocket을 통해 모든 클라이언트에 알림
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamBroadcastService {

    private final ExamWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    /**
     * 시험 시작 알림
     */
    public void broadcastExamStarted(Exam exam) {
        try {
            String message = objectMapper.writeValueAsString(Map.of(
                "type", "EXAM_STARTED",
                "examId", exam.getId(),
                "state", exam.getState().name(),
                "startsAt", exam.getStartsAt().toString(),
                "endsAt", exam.getEndsAt().toString(),
                "version", exam.getVersion()
            ));
            
            webSocketHandler.broadcast(exam.getId(), message);
            log.info("Broadcasted EXAM_STARTED for examId={}", exam.getId());
        } catch (Exception e) {
            log.error("Failed to broadcast exam started", e);
        }
    }

    /**
     * 시험 종료 알림
     */
    public void broadcastExamEnded(Exam exam) {
        try {
            String message = objectMapper.writeValueAsString(Map.of(
                "type", "EXAM_ENDED",
                "examId", exam.getId(),
                "state", exam.getState().name(),
                "version", exam.getVersion()
            ));
            
            webSocketHandler.broadcast(exam.getId(), message);
            log.info("Broadcasted EXAM_ENDED for examId={}", exam.getId());
        } catch (Exception e) {
            log.error("Failed to broadcast exam ended", e);
        }
    }

    /**
     * 시험 시간 연장 알림
     */
    public void broadcastExamExtended(Exam exam, int addedMinutes) {
        try {
            String message = objectMapper.writeValueAsString(Map.of(
                "type", "EXAM_EXTENDED",
                "examId", exam.getId(),
                "endsAt", exam.getEndsAt().toString(),
                "addedMinutes", addedMinutes,
                "version", exam.getVersion()
            ));
            
            webSocketHandler.broadcast(exam.getId(), message);
            log.info("Broadcasted EXAM_EXTENDED for examId={}, addedMinutes={}", exam.getId(), addedMinutes);
        } catch (Exception e) {
            log.error("Failed to broadcast exam extended", e);
        }
    }
}
