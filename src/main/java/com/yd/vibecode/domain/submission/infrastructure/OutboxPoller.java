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
        List<OutboxEvent> events = outboxEventRepository.findPendingEvents(
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

                // AI 서버 호출
                aiChatService.submitEvaluation(request);

                // 성공 시 상태 업데이트
                updateEventAsProcessed(event.getId());
                log.info("Outbox event processed successfully: id={}", event.getId());
            } catch (Exception e) {
                log.error("Failed to process outbox event: id={}, error={}", event.getId(), e.getMessage());
                handleProcessingFailure(event.getId());
            }
        } else {
            log.warn("Unknown event type: {}", event.getEventType());
            updateEventAsProcessed(event.getId()); // 알 수 없는 타입은 처리 완료로 간주
        }
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
