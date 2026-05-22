package com.yd.vibecode.domain.submission.application.dto.request;

import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.entity.Verdict;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * FastAPI에서 채점 완료 후 전송하는 요청 DTO
 */
public record ScoringResultRequest(
    @NotNull(message = "status는 필수입니다")
    SubmissionStatus status,
    @NotNull(message = "testCases는 필수입니다")
    @Valid
    List<TestCaseResult> testCases,
    ScoreData score
) {
    public record TestCaseResult(
        @NotNull(message = "caseIndex는 필수입니다")
        Integer caseIndex,
        @NotBlank(message = "group은 필수입니다")
        String group,  // SAMPLE, PUBLIC, PRIVATE
        @NotNull(message = "verdict는 필수입니다")
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
