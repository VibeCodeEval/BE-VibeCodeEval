package com.yd.vibecode.domain.submission.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yd.vibecode.domain.chat.infrastructure.AIChatService;
import com.yd.vibecode.domain.submission.application.dto.request.AISubmitEvaluationRequest;
import com.yd.vibecode.domain.submission.domain.entity.OutboxEvent;
import com.yd.vibecode.domain.submission.domain.entity.OutboxStatus;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OutboxPollerTest {

    @Mock
    private OutboxEventTxHelper txHelper;

    @Mock
    private AIChatService aiChatService;

    @InjectMocks
    private OutboxPoller outboxPoller;

    // ObjectMapper를 실제 인스턴스로 주입
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void injectObjectMapper() throws Exception {
        Field field = OutboxPoller.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(outboxPoller, objectMapper);
    }

    // ------------------------------------------------------------------ //
    // helpers
    // ------------------------------------------------------------------ //

    private OutboxEvent buildEventWithId(Long id, String eventType, String payload) throws Exception {
        OutboxEvent event = OutboxEvent.builder()
                .aggregateType("SUBMISSION")
                .aggregateId(id)
                .eventType(eventType)
                .payload(payload)
                .build();
        // id는 @GeneratedValue이므로 리플렉션으로 주입
        Field idField = OutboxEvent.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(event, id);
        return event;
    }

    private String toJson(AISubmitEvaluationRequest req) throws Exception {
        return objectMapper.writeValueAsString(req);
    }

    // ------------------------------------------------------------------ //
    // pollAndProcess - 이벤트 없음
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("PENDING 이벤트가 없으면 AIChatService를 호출하지 않는다")
    void pollAndProcess_noEvents_doesNotCallAI() {
        // given
        given(txHelper.fetchAndLock(eq(OutboxStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(List.of());

        // when
        outboxPoller.pollAndProcess();

        // then
        verify(aiChatService, never()).submitEvaluation(any());
    }

    // ------------------------------------------------------------------ //
    // pollAndProcess - AI_EVAL_REQUEST 처리 성공
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("AI_EVAL_REQUEST 이벤트 처리 성공 시 markProcessed 호출")
    void pollAndProcess_aiEvalRequest_success() throws Exception {
        // given
        AISubmitEvaluationRequest req = new AISubmitEvaluationRequest(
                1L, 2L, 3L, 4L, "def foo(): pass", "python", 10L);
        String payload = toJson(req);
        OutboxEvent event = buildEventWithId(10L, "AI_EVAL_REQUEST", payload);

        given(txHelper.fetchAndLock(eq(OutboxStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(List.of(event));

        // when
        outboxPoller.pollAndProcess();

        // then
        verify(aiChatService, times(1)).submitEvaluation(any(AISubmitEvaluationRequest.class));
        verify(txHelper, times(1)).markProcessed(10L);
        verify(txHelper, never()).markFailed(any(), anyInt());
    }

    // ------------------------------------------------------------------ //
    // pollAndProcess - AI 호출 실패 → markFailed 호출
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("AI 호출 실패 시 markFailed 호출 (markProcessed 미호출)")
    void pollAndProcess_aiThrowsException_markFailed() throws Exception {
        // given
        AISubmitEvaluationRequest req = new AISubmitEvaluationRequest(
                1L, 2L, 3L, 4L, "code", "python", 20L);
        OutboxEvent event = buildEventWithId(20L, "AI_EVAL_REQUEST", toJson(req));

        given(txHelper.fetchAndLock(eq(OutboxStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(List.of(event));
        willThrow(new RuntimeException("AI server error")).given(aiChatService).submitEvaluation(any());

        // when
        outboxPoller.pollAndProcess();

        // then
        verify(txHelper, times(1)).markFailed(eq(20L), eq(5)); // MAX_ATTEMPTS = 5
        verify(txHelper, never()).markProcessed(any());
    }

    // ------------------------------------------------------------------ //
    // pollAndProcess - 페이로드 파싱 실패 → markFailed
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("페이로드 JSON 파싱 실패 시 markFailed 호출")
    void pollAndProcess_invalidPayload_markFailed() throws Exception {
        // given
        OutboxEvent event = buildEventWithId(30L, "AI_EVAL_REQUEST", "NOT_VALID_JSON");

        given(txHelper.fetchAndLock(eq(OutboxStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(List.of(event));

        // when
        outboxPoller.pollAndProcess();

        // then
        verify(txHelper, times(1)).markFailed(eq(30L), eq(5));
        verify(aiChatService, never()).submitEvaluation(any());
    }

    // ------------------------------------------------------------------ //
    // pollAndProcess - 알 수 없는 eventType → markProcessed (무시)
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("알 수 없는 eventType은 경고 로그 후 markProcessed 처리")
    void pollAndProcess_unknownEventType_markProcessed() throws Exception {
        // given
        OutboxEvent event = buildEventWithId(40L, "UNKNOWN_TYPE", "{}");

        given(txHelper.fetchAndLock(eq(OutboxStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(List.of(event));

        // when
        outboxPoller.pollAndProcess();

        // then
        verify(txHelper, times(1)).markProcessed(40L);
        verify(aiChatService, never()).submitEvaluation(any());
    }

    // ------------------------------------------------------------------ //
    // pollAndProcess - 배치(복수 이벤트) 처리
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("여러 이벤트를 배치로 처리할 때 각각 올바르게 처리된다")
    void pollAndProcess_multipleMixedEvents() throws Exception {
        // given - 성공 이벤트
        AISubmitEvaluationRequest req1 = new AISubmitEvaluationRequest(
                1L, 1L, 1L, 1L, "code1", "python", 101L);
        OutboxEvent successEvent = buildEventWithId(101L, "AI_EVAL_REQUEST", toJson(req1));

        // given - 알 수 없는 타입 이벤트
        OutboxEvent unknownEvent = buildEventWithId(102L, "SOME_OTHER", "{}");

        given(txHelper.fetchAndLock(eq(OutboxStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(List.of(successEvent, unknownEvent));

        // when
        outboxPoller.pollAndProcess();

        // then
        verify(aiChatService, times(1)).submitEvaluation(any());
        verify(txHelper, times(1)).markProcessed(101L); // AI 성공
        verify(txHelper, times(1)).markProcessed(102L); // unknown → markProcessed
    }

    // ------------------------------------------------------------------ //
    // recoverStaleProcessingEvents
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("recoverStaleProcessingEvents 호출 시 txHelper.recoverStaleEvents에 PROCESSING 상태와 임계 시간을 전달")
    void recoverStaleProcessingEvents_delegatesToTxHelper() {
        // when
        outboxPoller.recoverStaleProcessingEvents();

        // then
        verify(txHelper, times(1)).recoverStaleEvents(
                eq(OutboxStatus.PROCESSING),
                any(LocalDateTime.class)
        );
    }

    // ------------------------------------------------------------------ //
    // fetchAndLock 결과가 Pageable 파라미터를 올바르게 전달하는지 확인
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("fetchAndLock 호출 시 PENDING 상태와 현재 시각을 함께 전달한다")
    void pollAndProcess_fetchAndLockReceivesCorrectStatus() {
        // given
        given(txHelper.fetchAndLock(eq(OutboxStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(List.of());

        // when
        outboxPoller.pollAndProcess();

        // then
        verify(txHelper, times(1)).fetchAndLock(
                eq(OutboxStatus.PENDING),
                any(LocalDateTime.class),
                any(Pageable.class)
        );
    }
}
