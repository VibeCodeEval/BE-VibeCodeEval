package com.yd.vibecode.domain.statistics.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yd.vibecode.domain.statistics.application.dto.response.ExamStatisticResponse;
import com.yd.vibecode.domain.statistics.domain.entity.ExamStatistic;
import com.yd.vibecode.domain.statistics.domain.service.ExamStatisticService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetLatestStatisticsUseCase {

    private final ExamStatisticService examStatisticService;

    @Transactional(readOnly = true)
    public ExamStatisticResponse execute(Long examId) {
        ExamStatistic statistic = examStatisticService.findLatestByExamId(examId);
        
        if (statistic == null) {
            return null;
        }

        return toResponse(statistic);
    }

    private ExamStatisticResponse toResponse(ExamStatistic stat) {
        return new ExamStatisticResponse(
                stat.getId(),
                stat.getExamId(),
                stat.getBucketStart(),
                stat.getBucketSec(),
                stat.getActiveExaminees(),
                stat.getJudgeQueueDepth(),
                stat.getAvgWaitSec(),
                stat.getTotalSubmissions(),
                stat.getPassedSubmissions(),
                stat.getPassRate(),
                stat.getAvgTotalScore(),
                stat.getAvgPromptScore(),
                stat.getAvgPerfScore(),
                stat.getTotalTokensUsed(),
                stat.getAvgTokensPerUser()
        );
    }
}
