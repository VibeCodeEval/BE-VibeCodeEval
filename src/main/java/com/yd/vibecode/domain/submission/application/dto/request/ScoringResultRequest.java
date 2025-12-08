package com.yd.vibecode.domain.submission.application.dto.request;

import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.entity.Verdict;

import java.math.BigDecimal;
import java.util.List;

/**
 * FastAPI에서 채점 완료 후 전송하는 요청 DTO
 */
public record ScoringResultRequest(
    SubmissionStatus status,
    List<TestCaseResult> testCases,
    ScoreData score
) {
    public record TestCaseResult(
        Integer caseIndex,
        String group,  // SAMPLE, PUBLIC, PRIVATE
        Verdict verdict,
        Integer timeMs,
        Integer memKb,
        Integer stdoutBytes,
        Integer stderrBytes
    ) {
    }

    public record ScoreData(
        BigDecimal promptScore,
        BigDecimal perfScore,
        BigDecimal correctnessScore,
        String rubricJson
    ) {
    }
}
