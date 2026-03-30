package com.yd.vibecode.domain.submission.infrastructure;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

/**
 * SSE Emitter 레지스트리
 * submissionId별 SseEmitter를 관리한다.
 *
 * - 연결 수립: register(submissionId) → SseEmitter 반환
 * - 이벤트 발행: send(submissionId, eventName, data) — IOException 시 SseDeliveryException throw
 * - 연결 종료: complete(submissionId)
 *
 * 주의: 인메모리 단일 서버 전제. 서버 재시작 시 연결 초기화됨.
 */
@Slf4j
@Component
public class SseEmitterRegistry {

    // SSE 타임아웃: 10분 (채점은 최대 수 분 소요 전제)
    private static final long SSE_TIMEOUT_MS = 10 * 60 * 1000L;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * submissionId에 대한 새 SseEmitter를 생성하고 등록한다.
     * 콜백에는 2-arg remove(submissionId, emitter)를 사용하여
     * 교체 직후 새 에미터가 의도치 않게 제거되는 race condition을 방지한다.
     */
    public SseEmitter register(Long submissionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitter.onCompletion(() -> {
            emitters.remove(submissionId, emitter);
            log.debug("SSE emitter completed: submissionId={}", submissionId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(submissionId, emitter);
            log.warn("SSE emitter timed out: submissionId={}", submissionId);
        });
        emitter.onError(e -> {
            emitters.remove(submissionId, emitter);
            log.error("SSE emitter error: submissionId={}", submissionId, e);
        });

        SseEmitter old = emitters.put(submissionId, emitter);
        if (old != null) {
            old.complete();
        }

        log.info("SSE emitter registered: submissionId={}", submissionId);
        return emitter;
    }

    /**
     * submissionId에 이벤트를 전송한다.
     *
     * @throws SseDeliveryException 에미터가 존재하지만 I/O 오류로 전송 실패 시 (재시도 트리거)
     *         에미터가 없는 경우(클라이언트 연결 없음)는 예외 없이 리턴 (재시도 불필요)
     */
    public void send(Long submissionId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(submissionId);
        if (emitter == null) {
            log.debug("No SSE emitter for submissionId={}, client disconnected", submissionId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException e) {
            emitters.remove(submissionId, emitter);
            // 재시도 가능한 실패 → SseDeliveryException throw
            throw new SseDeliveryException(submissionId, eventName, e);
        }
    }

    /**
     * 채점 완료 후 스트림을 종료한다.
     */
    public void complete(Long submissionId) {
        SseEmitter emitter = emitters.remove(submissionId);
        if (emitter != null) {
            emitter.complete();
            log.info("SSE emitter completed by server: submissionId={}", submissionId);
        }
    }
}
