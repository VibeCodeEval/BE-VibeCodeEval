package com.yd.vibecode.domain.submission.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.submission.application.dto.response.SubmissionDetailResponse;
import com.yd.vibecode.domain.submission.domain.entity.RunGroup;
import com.yd.vibecode.domain.submission.domain.entity.Score;
import com.yd.vibecode.domain.submission.domain.entity.Submission;
import com.yd.vibecode.domain.submission.domain.entity.SubmissionRun;
import com.yd.vibecode.domain.submission.domain.entity.Verdict;
import com.yd.vibecode.domain.submission.domain.repository.ScoreRepository;
import com.yd.vibecode.domain.submission.domain.repository.SubmissionRunRepository;
import com.yd.vibecode.domain.submission.domain.service.SubmissionService;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 제출 상세 조회 UseCase
 * - 제출 메타 정보
 * - 테스트 케이스 실행 결과 집계
 * - 점수 정보
 */
@Service
@RequiredArgsConstructor
public class GetSubmissionDetailUseCase {

    private final SubmissionService submissionService;
    private final SubmissionRunRepository submissionRunRepository;
    private final ScoreRepository scoreRepository;

    @Transactional(readOnly = true)
    public SubmissionDetailResponse execute(Long submissionId) {
        // 1. Submission 조회
        Submission submission = submissionService.findById(submissionId);

        // 2. SubmissionRun 조회 및 집계
        List<SubmissionRun> runs = submissionRunRepository.findBySubmissionId(submissionId);
        
        // 3. Score 조회
        Score score = scoreRepository.findBySubmissionId(submissionId).orElse(null);

        // 4. 응답 구성
        return buildResponse(submission, runs, score);
    }

    private SubmissionDetailResponse buildResponse(Submission submission, 
                                                   List<SubmissionRun> runs,
                                                   Score score) {
        // Metrics 계산
        SubmissionDetailResponse.MetricsInfo metrics = calculateMetrics(submission, runs);

        // TestCase 정보 계산
        SubmissionDetailResponse.TestCaseInfo tc = calculateTestCaseInfo(runs);

        // Score 정보
        SubmissionDetailResponse.ScoreInfo scoreInfo = score != null ?
            new SubmissionDetailResponse.ScoreInfo(
                score.getPromptScore(),
                score.getPerfScore(),
                score.getCorrectnessScore(),
                score.getTotalScore()
            ) :
            new SubmissionDetailResponse.ScoreInfo(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );

        return new SubmissionDetailResponse(
            submission.getId(),
            submission.getStatus(),
            submission.getLang(),
            metrics,
            tc,
            scoreInfo
        );
    }

    private SubmissionDetailResponse.MetricsInfo calculateMetrics(Submission submission,
                                                                  List<SubmissionRun> runs) {
        // 시간 중앙값 계산
        List<Integer> times = runs.stream()
            .map(SubmissionRun::getTimeMs)
            .filter(t -> t != null)
            .sorted()
            .toList();
        
        Integer timeMsMedian = times.isEmpty() ? null : 
            times.get(times.size() / 2);

        // 메모리 peak 계산
        Integer memKbPeak = runs.stream()
            .map(SubmissionRun::getMemKb)
            .filter(m -> m != null)
            .max(Integer::compareTo)
            .orElse(null);

        return new SubmissionDetailResponse.MetricsInfo(
            timeMsMedian,
            memKbPeak,
            submission.getCodeLoc()
        );
    }

    private SubmissionDetailResponse.TestCaseInfo calculateTestCaseInfo(List<SubmissionRun> runs) {
        // 그룹별 집계
        Map<RunGroup, List<SubmissionRun>> groupedRuns = runs.stream()
            .collect(Collectors.groupingBy(SubmissionRun::getGrp));

        List<SubmissionDetailResponse.GroupInfo> groups = new ArrayList<>();
        double totalWeightedPass = 0.0;
        double totalWeight = 0.0;

        // 가중치 정의 (예시)
        Map<RunGroup, Double> weights = Map.of(
            RunGroup.SAMPLE, 0.1,
            RunGroup.PUBLIC, 0.3,
            RunGroup.PRIVATE, 0.6
        );

        for (RunGroup group : RunGroup.values()) {
            List<SubmissionRun> groupRuns = groupedRuns.getOrDefault(group, List.of());
            if (groupRuns.isEmpty()) continue;

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
