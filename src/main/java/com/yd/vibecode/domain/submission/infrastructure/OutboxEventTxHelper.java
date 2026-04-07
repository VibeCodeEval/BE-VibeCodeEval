package com.yd.vibecode.domain.submission.infrastructure;

import com.yd.vibecode.domain.submission.domain.entity.OutboxEvent;
import com.yd.vibecode.domain.submission.domain.entity.OutboxStatus;
import com.yd.vibecode.domain.submission.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OutboxPoller에서 사용하는 트랜잭션 처리 헬퍼.
 *
 * Spring AOP 프록시는 동일 빈(self-call)을 통과하지 못하므로,
 * @Transactional 메서드를 별도 컴포넌트로 분리해 프록시가 정상 동작하도록 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventTxHelper {

    private final OutboxEventRepository outboxEventRepository;

    /** PENDING 이벤트를 조회 후 PROCESSING 으로 선점 마킹 */
    @Transactional
    public List<OutboxEvent> fetchAndLock(OutboxStatus pendingStatus, LocalDateTime now, Pageable pageable) {
        List<OutboxEvent> events = outboxEventRepository.findPendingEvents(pendingStatus, now, pageable);
        events.forEach(OutboxEvent::markAsProcessing);
        return events;
    }

    /** 이벤트를 PROCESSED 로 완료 처리 */
    @Transactional
    public void markProcessed(Long eventId) {
        outboxEventRepository.findById(eventId).ifPresent(OutboxEvent::markAsProcessed);
    }

    /** 이벤트 처리 실패 — 재시도 횟수 증가 */
    @Transactional
    public void markFailed(Long eventId, int maxAttempts) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.incrementAttempts(maxAttempts);
            outboxEventRepository.save(event);
        });
    }

    /** PROCESSING 고착 이벤트 복구 */
    @Transactional
    public void recoverStaleEvents(OutboxStatus processingStatus, LocalDateTime threshold) {
        List<OutboxEvent> staleEvents = outboxEventRepository.findStaleProcessingEvents(processingStatus, threshold);
        if (!staleEvents.isEmpty()) {
            log.warn("Recovering {} stale PROCESSING outbox events", staleEvents.size());
            staleEvents.forEach(OutboxEvent::resetToRetry);
        }
    }
}
