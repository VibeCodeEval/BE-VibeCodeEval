package com.yd.vibecode.domain.submission.infrastructure;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.yd.vibecode.domain.submission.application.event.ScoringResultSseEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채점 결과 SSE 이벤트 리스너
 *
 * 실행 시점: 트랜잭션 커밋 완료 후 (AFTER_COMMIT)
 * → DB에 채점 결과가 완전히 저장된 후 SSE 전송 시작
 * → 클라이언트가 SSE 이벤트 수신 후 REST API로 DB 조회 시 데이터 가시성 보장
 *
 * @Async: SSE 전송이 메인 스레드를 블록하지 않도록 비동기 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseScoringEventListener {

    private final SseRetryExecutor sseRetryExecutor;

    @Async("sseAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ScoringResultSseEvent event) {
        log.info("[SSE Outbox] Event received after TX commit: submissionId={}", event.submissionId());
        sseRetryExecutor.deliverWithRetry(event);
    }
}
