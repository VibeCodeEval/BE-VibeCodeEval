package com.yd.vibecode.domain.submission.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yd.vibecode.domain.chat.infrastructure.AIChatService;
import com.yd.vibecode.domain.submission.application.dto.request.AISubmitEvaluationRequest;
import com.yd.vibecode.domain.submission.domain.entity.OutboxEvent;
import com.yd.vibecode.domain.submission.domain.entity.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxEventTxHelper txHelper;
    private final AIChatService aiChatService;
    private final ObjectMapper objectMapper;

    private static final int MAX_ATTEMPTS = 5;
    private static final int BATCH_SIZE = 10;
    private static final long STALE_PROCESSING_MINUTES = 2;

    /**
     * 서버 크래시 등으로 PROCESSING에 고착된 이벤트를 PENDING으로 복구한다.
     */
    @Scheduled(fixedDelay = 60_000)
    public void recoverStaleProcessingEvents() {
        LocalDateTime staleThreshold = LocalDateTime.now().minusMinutes(STALE_PROCESSING_MINUTES);
        txHelper.recoverStaleEvents(OutboxStatus.PROCESSING, staleThreshold);
    }

    @Scheduled(fixedDelay = 5000)
    public void pollAndProcess() {
        List<OutboxEvent> events = txHelper.fetchAndLock(
                OutboxStatus.PENDING,
                LocalDateTime.now(),
                PageRequest.of(0, BATCH_SIZE, Sort.by("nextRetryAt").ascending())
        );

        if (events.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox events", events.size());
        for (OutboxEvent event : events) {
            processEvent(event);
        }
    }

    private void processEvent(OutboxEvent event) {
        if ("AI_EVAL_REQUEST".equals(event.getEventType())) {
            try {
                AISubmitEvaluationRequest request = objectMapper.readValue(
                        event.getPayload(), AISubmitEvaluationRequest.class);

                aiChatService.submitEvaluation(request);
                txHelper.markProcessed(event.getId());
                log.info("Outbox event processed successfully: id={}", event.getId());
            } catch (Exception e) {
                log.error("Failed to process outbox event: id={}, error={}", event.getId(), e.getMessage(), e);
                txHelper.markFailed(event.getId(), MAX_ATTEMPTS);
            }
        } else {
            log.warn("Unknown event type: {}", event.getEventType());
            txHelper.markProcessed(event.getId());
        }
    }
}
