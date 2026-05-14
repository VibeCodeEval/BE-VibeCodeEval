package com.yd.vibecode.domain.submission.application.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse;
import com.yd.vibecode.domain.submission.domain.entity.RunGroup;
import com.yd.vibecode.domain.submission.domain.entity.Score;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionRun;
import com.yd.vibecode.domain.submission.domain.entity.Verdict;

/**
 * 제출 상세 응답의 metrics / tc / score 집계 (공용 상세·관리자 상세 공통).
 */
@Component
public class SubmissionDetailAssembler {

    public SubmissionDetailResponse toResponse(Submission submission,
                                               List<SubmissionRun> runs,
                                               Score score) {
        SubmissionDetailResponse.MetricsInfo metrics = calculateMetrics(submission, runs);
        SubmissionDetailResponse.TestCaseInfo tc = calculateTestCaseInfo(runs);
        SubmissionDetailResponse.ScoreInfo scoreInfo = score != null
                ? new SubmissionDetailResponse.ScoreInfo(
                        score.getPromptScore(),
                        score.getPerfScore(),
                        score.getCorrectnessScore(),
                        score.getTotalScore())
                : new SubmissionDetailResponse.ScoreInfo(
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        return new SubmissionDetailResponse(
                submission.getId(),
                submission.getStatus(),
                submission.getLang(),
                metrics,
                tc,
                scoreInfo);
    }

    private SubmissionDetailResponse.MetricsInfo calculateMetrics(Submission submission,
                                                                  List<SubmissionRun> runs) {
        List<Integer> times = runs.stream()
                .map(SubmissionRun::getTimeMs)
                .filter(t -> t != null)
                .sorted()
                .toList();

        Integer timeMsMedian = times.isEmpty() ? null
                : times.get(times.size() / 2);

        Integer memKbPeak = runs.stream()
                .map(SubmissionRun::getMemKb)
                .filter(m -> m != null)
                .max(Integer::compareTo)
                .orElse(null);

        return new SubmissionDetailResponse.MetricsInfo(
                timeMsMedian,
                memKbPeak,
                submission.getCodeLoc());
    }

    private SubmissionDetailResponse.TestCaseInfo calculateTestCaseInfo(List<SubmissionRun> runs) {
        Map<RunGroup, List<SubmissionRun>> groupedRuns = runs.stream()
                .collect(Collectors.groupingBy(SubmissionRun::getGrp));

        List<SubmissionDetailResponse.GroupInfo> groups = new ArrayList<>();
        double totalWeightedPass = 0.0;
        double totalWeight = 0.0;

        Map<RunGroup, Double> weights = Map.of(
                RunGroup.SAMPLE, 0.1,
                RunGroup.PUBLIC, 0.3,
                RunGroup.PRIVATE, 0.6);

        for (RunGroup group : RunGroup.values()) {
            List<SubmissionRun> groupRuns = groupedRuns.getOrDefault(group, List.of());
            if (groupRuns.isEmpty()) {
                continue;
            }

            int total = groupRuns.size();
            int pass = (int) groupRuns.stream()
                    .filter(r -> r.getVerdict() == Verdict.AC)
                    .count();

            double weight = weights.getOrDefault(group, 0.0);
            double passRate = total > 0 ? (double) pass / total : 0.0;

            groups.add(new SubmissionDetailResponse.GroupInfo(group, pass, total, weight));

            totalWeightedPass += passRate * weight;
            totalWeight += weight;
        }

        double passRateWeighted = totalWeight > 0 ? totalWeightedPass / totalWeight : 0.0;

        return new SubmissionDetailResponse.TestCaseInfo(passRateWeighted, groups);
    }
}
