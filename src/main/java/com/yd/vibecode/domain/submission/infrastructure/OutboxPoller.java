package com.yd.vibecode.domain.submission.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yd.vibecode.domain.chat.infrastructure.AIChatService;
import com.yd.vibecode.domain.submission.application.dto.request.AISubmitEvaluationRequest;
import com.yd.vibecode.domain.submission.domain.entity.OutboxEvent;
import com.yd.vibecode.domain.submission.domain.entity.OutboxStatus;
import com.yd.vibecode.domain.submission.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final AIChatService aiChatService;
    private final ObjectMapper objectMapper;

    private static final int MAX_ATTEMPTS = 5;
    private static final int BATCH_SIZE = 10;

    @Scheduled(fixedDelay = 5000)
    public void pollAndProcess() {
        // 1. 선점 처리: 상태를 PROCESSING으로 변경 (Lock과 함께 수행)
        List<OutboxEvent> events = fetchAndLockEvents();

        if (events.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox events", events.size());
        for (OutboxEvent event : events) {
            processEvent(event);
        }
    }

    @Transactional
    public List<OutboxEvent> fetchAndLockEvents() {
        List<OutboxEvent> events = outboxEventRepository.findPendingEvents(
                OutboxStatus.PENDING,
                LocalDateTime.now(),
                PageRequest.of(0, BATCH_SIZE, Sort.by("nextRetryAt").ascending())
        );

        events.forEach(OutboxEvent::markAsProcessing);
        return events;
    }

    private void processEvent(OutboxEvent event) {
        if ("AI_EVAL_REQUEST".equals(event.getEventType())) {
            try {
                AISubmitEvaluationRequest request = objectMapper.readValue(
                        event.getPayload(), AISubmitEvaluationRequest.class);

                // AI 서버 호출 (개별 트랜잭션 내에서 처리)
                executeProcessing(event.getId(), request);
                log.info("Outbox event processed successfully: id={}", event.getId());
            } catch (Exception e) {
                log.error("Failed to process outbox event: id={}, error={}", event.getId(), e.getMessage(), e);
                handleProcessingFailure(event.getId());
            }
        } else {
            log.warn("Unknown event type: {}", event.getEventType());
            updateEventAsProcessed(event.getId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeProcessing(Long eventId, AISubmitEvaluationRequest request) {
        aiChatService.submitEvaluation(request);
        updateEventAsProcessed(eventId);
    }

    @Transactional
    public void updateEventAsProcessed(Long eventId) {
        outboxEventRepository.findById(eventId).ifPresent(OutboxEvent::markAsProcessed);
    }

    @Transactional
    public void handleProcessingFailure(Long eventId) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.incrementAttempts(MAX_ATTEMPTS);
            outboxEventRepository.save(event);
        });
    }
}
