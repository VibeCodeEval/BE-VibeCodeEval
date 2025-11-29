package com.yd.vibecode.global.sse;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE Emitter 관리 서비스
 * - submissionId별 SseEmitter 관리
 * - 채점 결과 실시간 스트리밍 (Admin/Master용)
 */
@Slf4j
@Service
public class SseEmitterService {

    // submissionId -> SseEmitter
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 새로운 SSE 연결 생성
     */
    public SseEmitter createEmitter(Long submissionId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30분 타임아웃
        
        emitters.put(submissionId, emitter);
        
        emitter.onCompletion(() -> {
            log.info("SSE completed for submissionId={}", submissionId);
            emitters.remove(submissionId);
        });
        
        emitter.onTimeout(() -> {
            log.info("SSE timeout for submissionId={}", submissionId);
            emitters.remove(submissionId);
        });
        
        emitter.onError((e) -> {
            log.error("SSE error for submissionId={}", submissionId, e);
            emitters.remove(submissionId);
        });
        
        log.info("SSE emitter created for submissionId={}", submissionId);
        return emitter;
    }

    /**
     * 특정 submission에 이벤트 전송
     */
    public void sendEvent(Long submissionId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(submissionId);
        if (emitter == null) {
            log.debug("No SSE emitter found for submissionId={}", submissionId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
            log.debug("SSE event sent: submissionId={}, eventName={}", submissionId, eventName);
        } catch (IOException e) {
            log.error("Failed to send SSE event for submissionId={}", submissionId, e);
            emitters.remove(submissionId);
        }
    }

    /**
     * SSE 연결 종료
     */
    public void complete(Long submissionId) {
        SseEmitter emitter = emitters.remove(submissionId);
        if (emitter != null) {
            emitter.complete();
            log.info("SSE emitter completed for submissionId={}", submissionId);
        }
    }
}
