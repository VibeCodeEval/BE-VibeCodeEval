package com.yd.vibecode.domain.submission.application.dto.response;

import com.yd.vibecode.domain.submission.domain.entity.RunGroup;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionStatus;
import com.yd.vibecode.domain.submission.domain.entity.Verdict;

import java.math.BigDecimal;
import java.util.List;

public record SubmissionDetailResponse(
    Long id,
    SubmissionStatus status,
    String lang,
    MetricsInfo metrics,
    TestCaseInfo tc,
    ScoreInfo score
) {
    public record MetricsInfo(
        Integer timeMsMedian,
        Integer memKbPeak,
        Integer loc
    ) {
    }

    public record TestCaseInfo(
        Double passRateWeighted,
        List<GroupInfo> groups
    ) {
    }

    public record GroupInfo(
        RunGroup name,
        Integer pass,
        Integer total,
        Double weight
    ) {
    }

    public record ScoreInfo(
        BigDecimal prompt,
        BigDecimal perf,
        BigDecimal correctness,
        BigDecimal total
    ) {
    }
}
