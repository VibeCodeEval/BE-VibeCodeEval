package com.yd.vibecode.domain.submission.infrastructure;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import com.yd.vibecode.domain.submission.application.event.ScoringResultSseEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SSE 이벤트 전송 재시도 실행기 (In-Memory Outbox + Exponential Backoff)
 *
 * 설계 원칙:
 * - 진정한 outbox 패턴은 DB 영속화가 필요하지만 SSE 커넥션은 인메모리 상태이므로
 *   in-memory outbox + retry로 구현 (서버 재시작 시 연결 자체가 끊기므로 DB 영속화 불필요)
 * - 전송 실패(IOException) 시 지수 백오프로 재시도
 * - 에미터가 없는 경우(클라이언트 연결 없음)는 재시도 없이 종료
 *
 * 재시도 스케줄 (최대 5회):
 *   attempt 0 → 즉시 (초기 시도)
 *   attempt 1 → 1초 후
 *   attempt 2 → 2초 후
 *   attempt 3 → 4초 후
 *   attempt 4 → 8초 후
 *   --- 최대 대기 합계: 15초 ---
 *
 * 최대 재시도 초과 시 대안 (REST fallback):
 *   GET /api/submissions/{submissionId} 로 채점 결과 조회 가능
 *   (채점 데이터는 이미 DB에 커밋된 상태)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseRetryExecutor {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BASE_DELAY_MS = 1000L;  // 1초 기준 (2^n * BASE_DELAY_MS)

    private final SseEmitterRegistry sseEmitterRegistry;

    /**
     * 백그라운드 재시도 전용 스케줄러
     * daemon 스레드: 앱 종료 시 강제 종료됨 (미완료 이벤트 로그로 확인 가능)
     */
    private final ScheduledExecutorService retryScheduler = Executors.newScheduledThreadPool(
            2,
            r -> {
                Thread t = new Thread(r, "sse-retry");
                t.setDaemon(true);
                return t;
            }
    );

    /**
     * 애플리케이션 종료 시 retryScheduler를 graceful shutdown한다.
     * 실행 중인 재시도 작업이 완료될 수 있도록 최대 5초 대기 후 강제 종료.
     */
    @PreDestroy
    public void shutdown() {
        retryScheduler.shutdown();
        try {
            if (!retryScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                retryScheduler.shutdownNow();
                log.warn("[SSE Outbox] retryScheduler force-shutdown after 5s");
            }
        } catch (InterruptedException e) {
            retryScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * SSE 이벤트를 전송한다. 실패 시 지수 백오프로 재시도.
     * attempt=0 으로 호출 (첫 시도는 즉시 실행).
     */
    public void deliverWithRetry(ScoringResultSseEvent event) {
        sendEvents(event, 0);
    }

    private void sendEvents(ScoringResultSseEvent event, int attempt) {
        Long submissionId = event.submissionId();

        try {
            // 1. 테스트 케이스별 결과 전송
            for (ScoringResultSseEvent.CaseResultPayload caseResult : event.caseResults()) {
                sseEmitterRegistry.send(submissionId, "case_result", Map.of(
                        "caseIndex", caseResult.caseIndex(),
                        "verdict", caseResult.verdict(),
                        "timeMs", caseResult.timeMs(),
                        "memKb", caseResult.memKb()
                ));
            }

            // 2. 최종 점수 전송
            if (event.finalScore() != null) {
                sseEmitterRegistry.send(submissionId, "final_score", Map.of(
                        "submissionId", submissionId,
                        "status", event.status(),
                        "promptScore", event.finalScore().promptScore(),
                        "perfScore", event.finalScore().perfScore(),
                        "correctnessScore", event.finalScore().correctnessScore(),
                        "total", event.finalScore().total()
                ));
            }

            // 3. 스트림 종료
            sseEmitterRegistry.complete(submissionId);
            log.info("[SSE Outbox] Delivery succeeded: submissionId={}, attempt={}", submissionId, attempt + 1);

        } catch (SseDeliveryException e) {
            scheduleRetry(event, attempt, e);
        }
    }

    /**
     * 지수 백오프로 재시도를 스케줄링한다.
     * 최대 재시도 초과 시 REST fallback 경로를 로그로 안내한다.
     */
    private void scheduleRetry(ScoringResultSseEvent event, int attempt, SseDeliveryException cause) {
        int nextAttempt = attempt + 1;
        Long submissionId = event.submissionId();

        if (nextAttempt >= MAX_ATTEMPTS) {
            // 무한 루프 방지: 최대 재시도 초과 → REST fallback 안내
            log.error(
                    "[SSE Outbox] Max retry ({}) exhausted for submissionId={}. " +
                    "채점 결과는 DB에 저장되었으므로 REST API로 조회 가능: " +
                    "GET /api/submissions/{} | cause: {}",
                    MAX_ATTEMPTS, submissionId, submissionId, cause.getMessage()
            );
            return;
        }

        // 지수 백오프: 2^attempt * BASE_DELAY_MS (1s, 2s, 4s, 8s)
        long delayMs = (long) Math.pow(2, attempt) * BASE_DELAY_MS;

        log.warn(
                "[SSE Outbox] Delivery failed (attempt {}/{}), retry in {}ms: submissionId={} | {}",
                nextAttempt, MAX_ATTEMPTS, delayMs, submissionId, cause.getMessage()
        );

        retryScheduler.schedule(
                () -> sendEvents(event, nextAttempt),
                delayMs,
                TimeUnit.MILLISECONDS
        );
    }
}
