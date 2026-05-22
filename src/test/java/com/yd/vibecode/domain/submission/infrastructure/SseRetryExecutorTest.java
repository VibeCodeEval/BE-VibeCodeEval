package com.yd.vibecode.domain.submission.infrastructure;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yd.vibecode.domain.submission.application.event.ScoringResultSseEvent;
import com.yd.vibecode.domain.submission.domain.entity.RunGroup;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.entity.Verdict;

@ExtendWith(MockitoExtension.class)
class SseRetryExecutorTest {

    @InjectMocks
    private SseRetryExecutor sseRetryExecutor;

    @Mock
    private SseEmitterRegistry sseEmitterRegistry;

    @Test
    @DisplayName("score 없을 때 case_result → scoring_complete → complete 순서로 전송")
    void deliverWithRetry_withoutScore_sendsScoringCompleteBeforeComplete() {
        Long submissionId = 1L;
        ScoringResultSseEvent event = new ScoringResultSseEvent(
                submissionId,
                SubmissionStatus.DONE,
                List.of(new ScoringResultSseEvent.CaseResultPayload(
                        0, RunGroup.SAMPLE, Verdict.AC, 100, 1024)),
                new ScoringResultSseEvent.CompletionPayload(1, 1),
                null);

        sseRetryExecutor.deliverWithRetry(event);

        InOrder order = inOrder(sseEmitterRegistry);
        order.verify(sseEmitterRegistry).send(eq(submissionId), eq("case_result"), any());
        order.verify(sseEmitterRegistry).send(eq(submissionId), eq("scoring_complete"), any());
        order.verify(sseEmitterRegistry).complete(submissionId);
        verify(sseEmitterRegistry, never()).send(eq(submissionId), eq("final_score"), any());
    }

    @Test
    @DisplayName("score 있을 때 final_score는 scoring_complete 다음에 전송")
    void deliverWithRetry_withScore_sendsFinalScoreAfterScoringComplete() {
        Long submissionId = 2L;
        ScoringResultSseEvent event = new ScoringResultSseEvent(
                submissionId,
                SubmissionStatus.DONE,
                List.of(new ScoringResultSseEvent.CaseResultPayload(
                        1, RunGroup.PUBLIC, Verdict.WA, 50, 256)),
                new ScoringResultSseEvent.CompletionPayload(1, 0),
                new ScoringResultSseEvent.FinalScorePayload(
                        BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("30")));

        sseRetryExecutor.deliverWithRetry(event);

        InOrder order = inOrder(sseEmitterRegistry);
        order.verify(sseEmitterRegistry).send(eq(submissionId), eq("case_result"), any());
        order.verify(sseEmitterRegistry).send(eq(submissionId), eq("scoring_complete"), any());
        order.verify(sseEmitterRegistry).send(eq(submissionId), eq("final_score"), any());
        order.verify(sseEmitterRegistry).complete(submissionId);
    }
}
