package com.yd.vibecode.domain.submission.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.yd.vibecode.domain.submission.domain.entity.OutboxEvent;
import com.yd.vibecode.domain.submission.domain.entity.OutboxStatus;
import com.yd.vibecode.domain.submission.domain.repository.OutboxEventRepository;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class OutboxEventTxHelperTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private OutboxEventTxHelper outboxEventTxHelper;

    // ------------------------------------------------------------------ //
    // helpers
    // ------------------------------------------------------------------ //

    private OutboxEvent buildPendingEvent(Long id) throws Exception {
        OutboxEvent event = OutboxEvent.builder()
                .aggregateType("SUBMISSION")
                .aggregateId(id)
                .eventType("AI_EVAL_REQUEST")
                .payload("{}")
                .build();
        setId(event, id);
        return event;
    }

    private void setId(OutboxEvent event, Long id) throws Exception {
        Field idField = OutboxEvent.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(event, id);
    }

    // ------------------------------------------------------------------ //
    // fetchAndLock
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("fetchAndLock은 조회된 이벤트를 PROCESSING 상태로 변경한다")
    void fetchAndLock_marksPendingEventsAsProcessing() throws Exception {
        // given
        OutboxEvent event = buildPendingEvent(1L);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("nextRetryAt").ascending());
        given(outboxEventRepository.findPendingEvents(OutboxStatus.PENDING, LocalDateTime.MIN, pageable))
                .willReturn(List.of(event));

        // when
        List<OutboxEvent> result = outboxEventTxHelper.fetchAndLock(
                OutboxStatus.PENDING, LocalDateTime.MIN, pageable);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(OutboxStatus.PROCESSING);
    }

    @Test
    @DisplayName("fetchAndLock은 이벤트가 없으면 빈 리스트를 반환한다")
    void fetchAndLock_noEvents_returnsEmptyList() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        given(outboxEventRepository.findPendingEvents(any(), any(), any()))
                .willReturn(List.of());

        // when
        List<OutboxEvent> result = outboxEventTxHelper.fetchAndLock(
                OutboxStatus.PENDING, LocalDateTime.now(), pageable);

        // then
        assertThat(result).isEmpty();
    }

    // ------------------------------------------------------------------ //
    // markProcessed
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("markProcessed는 이벤트를 PROCESSED 상태로 변경한다")
    void markProcessed_setsStatusToProcessed() throws Exception {
        // given
        OutboxEvent event = buildPendingEvent(2L);
        given(outboxEventRepository.findById(2L)).willReturn(Optional.of(event));

        // when
        outboxEventTxHelper.markProcessed(2L);

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PROCESSED);
        assertThat(event.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("markProcessed는 이벤트가 없으면 아무것도 하지 않는다")
    void markProcessed_eventNotFound_doesNothing() {
        // given
        given(outboxEventRepository.findById(999L)).willReturn(Optional.empty());

        // when
        outboxEventTxHelper.markProcessed(999L);

        // then - exception 없이 정상 완료
    }

    // ------------------------------------------------------------------ //
    // markFailed - 재시도 횟수 < maxAttempts
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("attempts < maxAttempts 시 PENDING으로 복귀하고 nextRetryAt을 미래로 설정한다")
    void markFailed_belowMaxAttempts_setPendingWithBackoff() throws Exception {
        // given
        OutboxEvent event = buildPendingEvent(3L);
        given(outboxEventRepository.findById(3L)).willReturn(Optional.of(event));

        // when
        outboxEventTxHelper.markFailed(3L, 5);

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(event.getAttempts()).isEqualTo(1);
        assertThat(event.getNextRetryAt()).isAfter(LocalDateTime.now().minusSeconds(1));
        verify(outboxEventRepository).save(event);
    }

    // ------------------------------------------------------------------ //
    // markFailed - 재시도 횟수 >= maxAttempts
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("attempts >= maxAttempts 시 FAILED 상태로 변경된다")
    void markFailed_atMaxAttempts_setFailed() throws Exception {
        // given
        OutboxEvent event = buildPendingEvent(4L);
        // attempts를 maxAttempts-1 로 설정 (4)
        for (int i = 0; i < 4; i++) {
            event.incrementAttempts(5);
        }
        assertThat(event.getAttempts()).isEqualTo(4);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);

        given(outboxEventRepository.findById(4L)).willReturn(Optional.of(event));

        // when — 5번째 실패 시 FAILED로 전환
        outboxEventTxHelper.markFailed(4L, 5);

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.getAttempts()).isEqualTo(5);
        verify(outboxEventRepository).save(event);
    }

    // ------------------------------------------------------------------ //
    // markFailed - 이벤트 없음
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("markFailed는 이벤트가 없으면 아무것도 하지 않는다")
    void markFailed_eventNotFound_doesNothing() {
        // given
        given(outboxEventRepository.findById(999L)).willReturn(Optional.empty());

        // when
        outboxEventTxHelper.markFailed(999L, 5);

        // then - exception 없이 정상 완료
        verify(outboxEventRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ //
    // recoverStaleEvents - 고착 이벤트 복구
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("recoverStaleEvents는 고착 이벤트를 PENDING 상태로 복구한다")
    void recoverStaleEvents_resetsStaleEventsToRetry() throws Exception {
        // given
        OutboxEvent staleEvent = buildPendingEvent(5L);
        // 강제로 PROCESSING 상태로 변경
        staleEvent.markAsProcessing();
        assertThat(staleEvent.getStatus()).isEqualTo(OutboxStatus.PROCESSING);

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(2);
        given(outboxEventRepository.findStaleProcessingEvents(OutboxStatus.PROCESSING, threshold))
                .willReturn(List.of(staleEvent));

        // when
        outboxEventTxHelper.recoverStaleEvents(OutboxStatus.PROCESSING, threshold);

        // then
        assertThat(staleEvent.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(staleEvent.getNextRetryAt()).isNotNull();
    }

    @Test
    @DisplayName("recoverStaleEvents는 고착 이벤트가 없으면 아무것도 하지 않는다")
    void recoverStaleEvents_noStaleEvents_doesNothing() {
        // given
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(2);
        given(outboxEventRepository.findStaleProcessingEvents(any(), any()))
                .willReturn(List.of());

        // when
        outboxEventTxHelper.recoverStaleEvents(OutboxStatus.PROCESSING, threshold);

        // then - exception 없이 정상 완료
    }
}
