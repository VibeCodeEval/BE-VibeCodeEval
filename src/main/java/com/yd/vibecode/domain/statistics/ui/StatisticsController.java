package com.yd.vibecode.domain.statistics.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yd.vibecode.domain.statistics.application.dto.response.ExamStatisticResponse;
import com.yd.vibecode.domain.statistics.application.usecase.GetLatestStatisticsUseCase;
import com.yd.vibecode.global.common.BaseResponse;
import com.yd.vibecode.global.swagger.StatisticsApi;

import lombok.RequiredArgsConstructor;

/**
 * Statistics Controller
 * - 시험 통계 조회
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
public class StatisticsController implements StatisticsApi {

    private final GetLatestStatisticsUseCase getLatestStatisticsUseCase;

    @GetMapping("/exams/{examId}/latest")
    public BaseResponse<ExamStatisticResponse> getLatestStatistics(@PathVariable Long examId) {
        ExamStatisticResponse response = getLatestStatisticsUseCase.execute(examId);
        return BaseResponse.onSuccess(response);
    }
}
