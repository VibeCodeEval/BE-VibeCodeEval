package com.yd.vibecode.domain.submission.application.event;

import java.math.BigDecimal;
import java.util.List;

import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.entity.Verdict;

/**
 * 채점 결과 SSE 전송을 위한 Spring ApplicationEvent
 *
 * - ReceiveScoringResultUseCase에서 publish
 * - SseScoringEventListener가 @TransactionalEventListener(AFTER_COMMIT)으로 처리
 * - 이벤트가 자체적으로 전송에 필요한 모든 데이터를 보유 (재시도 시 DB 재조회 불필요)
 */
public record ScoringResultSseEvent(
    Long submissionId,
    SubmissionStatus status,
    List<CaseResultPayload> caseResults,
    FinalScorePayload finalScore  // nullable (score 없는 경우)
) {
    public record CaseResultPayload(
        int caseIndex,
        Verdict verdict,
        int timeMs,
        int memKb
    ) {}

    public record FinalScorePayload(
        BigDecimal promptScore,
        BigDecimal perfScore,
        BigDecimal correctnessScore,
        BigDecimal total
    ) {}
}
