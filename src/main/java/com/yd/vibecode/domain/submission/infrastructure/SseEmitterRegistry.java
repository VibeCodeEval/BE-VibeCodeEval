package com.yd.vibecode.domain.submission.infrastructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

/**
 * SSE Emitter 레지스트리
 * submissionId별로 여러 {@link SseEmitter}를 둘 수 있다 (관리자·응시자 동시 구독).
 *
 * - 연결 수립: register(submissionId) → SseEmitter 반환 후 목록에 추가
 * - 이벤트 발행: send(submissionId, eventName, data) — 등록된 모든 에미터에 브로드캐스트
 * - 연결 종료: complete(submissionId) — 해당 제출의 모든 에미터 종료
 *
 * 주의: 인메모리 단일 서버 전제. 서버 재시작 시 연결 초기화됨.
 */
@Slf4j
@Component
public class SseEmitterRegistry {

    // SSE 타임아웃: 10분 (채점은 최대 수 분 소요 전제)
    private static final long SSE_TIMEOUT_MS = 10 * 60 * 1000L;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * submissionId에 대한 새 SseEmitter를 생성하고 등록한다.
     * 기존 구독이 있어도 유지하며, 동일 제출에 대해 여러 클라이언트(관리자/응시자)가 동시에 구독할 수 있다.
     */
    public SseEmitter register(Long submissionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        Runnable removeSelf = () -> removeEmitter(submissionId, emitter);
        emitter.onCompletion(removeSelf);
        emitter.onTimeout(removeSelf);
        emitter.onError(e -> removeSelf.run());

        CopyOnWriteArrayList<SseEmitter> list =
                emitters.computeIfAbsent(submissionId, k -> new CopyOnWriteArrayList<>());
        list.add(emitter);

        log.info("SSE emitter registered: submissionId={}, activeCount={}", submissionId, list.size());
        return emitter;
    }

    private void removeEmitter(Long submissionId, SseEmitter emitter) {
        emitters.computeIfPresent(submissionId, (id, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
        log.debug("SSE emitter removed: submissionId={}", submissionId);
    }

    /**
     * submissionId에 이벤트를 전송한다.
     *
     * @throws SseDeliveryException 연결된 모든 에미터에 대한 전송이 I/O 오류로 실패한 경우 (재시도 트리거)
     *         에미터가 없는 경우는 예외 없이 리턴
     */
    public void send(Long submissionId, String eventName, Object data) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(submissionId);
        if (list == null || list.isEmpty()) {
            log.debug("No SSE emitter for submissionId={}, client disconnected", submissionId);
            return;
        }

        List<SseEmitter> snapshot = new ArrayList<>(list);
        int attempted = 0;
        int failures = 0;
        IOException firstFailure = null;

        for (SseEmitter emitter : snapshot) {
            if (!list.contains(emitter)) {
                continue;
            }
            attempted++;
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                failures++;
                if (firstFailure == null) {
                    firstFailure = e;
                }
                list.remove(emitter);
                log.warn("SSE send failed, removing emitter: submissionId={}, event={}", submissionId, eventName, e);
            }
        }

        if (attempted > 0 && failures == attempted && firstFailure != null) {
            throw new SseDeliveryException(submissionId, eventName, firstFailure);
        }
    }

    /**
     * 채점 완료 후 해당 제출의 모든 스트림을 종료한다.
     */
    public void complete(Long submissionId) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.remove(submissionId);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : new ArrayList<>(list)) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.debug("SSE complete ignored: {}", e.getMessage());
            }
        }
        log.info("SSE all emitters completed: submissionId={}, count={}", submissionId, list.size());
    }
}
